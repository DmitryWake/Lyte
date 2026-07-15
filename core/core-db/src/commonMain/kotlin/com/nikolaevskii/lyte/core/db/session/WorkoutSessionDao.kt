package com.nikolaevskii.lyte.core.db.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class WorkoutSessionDao {

    @Transaction
    @Query("SELECT * FROM workout_session WHERE finished_at IS NULL ORDER BY started_at DESC LIMIT 1")
    abstract suspend fun getActiveSession(): SessionWithExercises?

    @Transaction
    @Query("SELECT * FROM workout_session WHERE id = :id LIMIT 1")
    abstract suspend fun getSession(id: String): SessionWithExercises?

    @Query("SELECT COUNT(*) FROM workout_session WHERE finished_at IS NULL")
    abstract suspend fun countActiveSessions(): Int

    // Литерал 'COMPLETED' обязан совпадать с SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED —
    // Room не подставляет const в @Query. Покрыто тестом на счётчики.
    @Query(
        """
        SELECT workout_session.id AS id,
               workout_session.program_id AS programId,
               workout_session.program_name AS programName,
               workout_session.started_at AS startedAt,
               workout_session.finished_at AS finishedAt,
               COUNT(session_set.id) AS totalSetCount,
               COUNT(CASE WHEN session_set.result_status = 'COMPLETED' THEN 1 END) AS completedSetCount
        FROM workout_session
        LEFT JOIN session_exercise ON session_exercise.session_id = workout_session.id
        LEFT JOIN session_set ON session_set.session_exercise_id = session_exercise.id
        WHERE workout_session.finished_at IS NOT NULL
        GROUP BY workout_session.id
        ORDER BY workout_session.finished_at DESC
        """,
    )
    abstract suspend fun getFinishedItems(): List<SessionItemWithSetCounts>

    @Insert
    abstract suspend fun insertSession(session: WorkoutSessionDatabaseEntity)

    @Insert
    abstract suspend fun insertExercises(rows: List<SessionExerciseDatabaseEntity>)

    @Insert
    abstract suspend fun insertSets(rows: List<SessionSetDatabaseEntity>)

    @Query(
        "UPDATE session_set SET result_status = :status, result_count = :count, result_weight = :weight WHERE id = :id",
    )
    abstract suspend fun updateSetResult(id: String, status: String?, count: Int?, weight: Double?)

    @Query("UPDATE session_set SET note = :note WHERE id = :id")
    abstract suspend fun updateSetNote(id: String, note: String)

    @Query("UPDATE workout_session SET current_exercise_id = :exerciseId WHERE id = :id")
    abstract suspend fun updateCurrentExercise(id: String, exerciseId: String)

    @Query("UPDATE workout_session SET finished_at = :finishedAt WHERE id = :id")
    abstract suspend fun updateFinishedAt(id: String, finishedAt: Long)

    @Query(
        """
        UPDATE session_set SET result_status = :status
        WHERE result_status IS NULL
          AND session_exercise_id IN (SELECT id FROM session_exercise WHERE session_id = :sessionId)
        """,
    )
    abstract suspend fun markPendingSets(sessionId: String, status: String)

    /**
     * Вставляет граф сессии одной транзакцией. Здесь же держится инвариант «не более одной активной
     * сессии»: `check` внутри транзакции — проверка и вставка атомарны.
     *
     * @throws IllegalStateException если активная сессия уже существует.
     */
    @Transaction
    open suspend fun insertSessionGraph(
        session: WorkoutSessionDatabaseEntity,
        exercises: List<SessionExerciseDatabaseEntity>,
        sets: List<SessionSetDatabaseEntity>,
    ) {
        check(countActiveSessions() == 0) { ACTIVE_SESSION_EXISTS_MESSAGE }
        insertSession(session)
        insertExercises(exercises)
        insertSets(sets)
    }

    /**
     * Завершение сессии (обычное и досрочное) одной транзакцией: все ещё не выполненные подходы
     * помечаются пропущенными, затем проставляется `finished_at`.
     */
    @Transaction
    open suspend fun finishSession(id: String, finishedAt: Long) {
        markPendingSets(sessionId = id, status = SessionSetDatabaseEntity.RESULT_STATUS_SKIPPED)
        updateFinishedAt(id = id, finishedAt = finishedAt)
    }

    companion object {
        const val ACTIVE_SESSION_EXISTS_MESSAGE = "Активная сессия уже существует"
    }
}
