package com.nikolaevskii.lyte.core.db.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

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

    // Только строка сессии: снапшот программы лежит в ней самой, а подходы для трека исходов
    // забирает getFinishedSessionSets() отдельным запросом — по одному на весь список, не на карточку.
    @Query("SELECT * FROM workout_session WHERE finished_at IS NOT NULL ORDER BY finished_at DESC")
    abstract suspend fun getFinishedSessions(): List<WorkoutSessionDatabaseEntity>

    // Реактивная версия getFinishedSessions: тот же запрос, эмитит при изменении сессий.
    @Query("SELECT * FROM workout_session WHERE finished_at IS NOT NULL ORDER BY finished_at DESC")
    abstract fun observeFinishedSessions(): Flow<List<WorkoutSessionDatabaseEntity>>

    // Подходы всех завершённых сессий разом: потребитель раскладывает их по session_id и считает
    // исход каждого. Порядок трека (упражнение → подход) задаёт ORDER BY, а не потребитель.
    @Query(
        """
        SELECT session_exercise.session_id AS session_id, session_set.*
        FROM workout_session
        JOIN session_exercise ON session_exercise.session_id = workout_session.id
        JOIN session_set ON session_set.session_exercise_id = session_exercise.id
        WHERE workout_session.finished_at IS NOT NULL
        ORDER BY workout_session.finished_at DESC, session_exercise.position, session_set.position
        """,
    )
    abstract suspend fun getFinishedSessionSets(): List<FinishedSessionSetRow>

    // Реактивная версия getFinishedSessionSets: эмитит при изменении подходов.
    @Query(
        """
        SELECT session_exercise.session_id AS session_id, session_set.*
        FROM workout_session
        JOIN session_exercise ON session_exercise.session_id = workout_session.id
        JOIN session_set ON session_set.session_exercise_id = session_exercise.id
        WHERE workout_session.finished_at IS NOT NULL
        ORDER BY workout_session.finished_at DESC, session_exercise.position, session_set.position
        """,
    )
    abstract fun observeFinishedSessionSets(): Flow<List<FinishedSessionSetRow>>

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
