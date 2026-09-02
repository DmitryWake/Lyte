package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.db.session.FinishedSessionSetRow
import com.nikolaevskii.lyte.core.db.session.ProgramSetHistoryRow
import com.nikolaevskii.lyte.core.db.session.SessionExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionExerciseWithSets
import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionWithExercises
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDao
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In-memory реализация [WorkoutSessionDao] для тестов репозитория. `insertSessionGraph`/`finishSession`
 * наследуют транзакционные тела базового класса — проверяется и оркестрация с инвариантом.
 * Каскад (`ON DELETE CASCADE`) для сессий тут не нужен: тесты не удаляют сессии.
 */
internal class FakeWorkoutSessionDao : WorkoutSessionDao() {

    /** Библиотека упражнений — имитирует таблицу `exercise`, к которой session_exercise делает join. */
    val exerciseLibrary = mutableMapOf<String, ExerciseDatabaseEntity>()

    private val sessions = mutableMapOf<String, WorkoutSessionDatabaseEntity>()
    private val exercises = mutableListOf<SessionExerciseDatabaseEntity>()
    private val sets = mutableListOf<SessionSetDatabaseEntity>()

    override suspend fun getActiveSession(): SessionWithExercises? =
        sessions.values
            .filter { it.finishedAt == null }
            .maxByOrNull { it.startedAt }
            ?.let { sessionGraph(it) }

    override suspend fun getSession(id: String): SessionWithExercises? =
        sessions[id]?.let { sessionGraph(it) }

    override suspend fun countActiveSessions(): Int =
        sessions.values.count { it.finishedAt == null }

    override suspend fun getFinishedSessions(): List<WorkoutSessionDatabaseEntity> =
        sessions.values
            .filter { it.finishedAt != null }
            .sortedByDescending { it.finishedAt }

    override fun observeFinishedSessions(): Flow<List<WorkoutSessionDatabaseEntity>> =
        flow { emit(getFinishedSessions()) }

    override suspend fun getFinishedSessionSets(): List<FinishedSessionSetRow> =
        getFinishedSessions().flatMap { session ->
            // Порядок строк — как в ORDER BY настоящего запроса: упражнение, затем подход.
            exercises
                .filter { it.sessionId == session.id }
                .sortedBy { it.position }
                .flatMap { sessionExercise ->
                    sets
                        .filter { it.sessionExerciseId == sessionExercise.id }
                        .sortedBy { it.position }
                        .map { set -> FinishedSessionSetRow(sessionId = session.id, set = set) }
                }
        }

    override fun observeFinishedSessionSets(): Flow<List<FinishedSessionSetRow>> =
        flow { emit(getFinishedSessionSets()) }

    // Тот же порядок, что у настоящего ORDER BY: сессия (свежие первыми) → упражнение → подход.
    override suspend fun getProgramSetHistory(programId: String): List<ProgramSetHistoryRow> =
        getFinishedSessions()
            .filter { session -> session.programId == programId }
            .flatMap { session ->
                exercises
                    .filter { it.sessionId == session.id }
                    .sortedBy { it.position }
                    .flatMap { sessionExercise ->
                        sets
                            .filter { it.sessionExerciseId == sessionExercise.id }
                            .sortedBy { it.position }
                            .map { set ->
                                ProgramSetHistoryRow(
                                    sessionId = session.id,
                                    exerciseId = sessionExercise.exerciseId,
                                    set = set,
                                )
                            }
                    }
            }

    override suspend fun insertSession(session: WorkoutSessionDatabaseEntity) {
        sessions[session.id] = session
    }

    override suspend fun insertExercises(rows: List<SessionExerciseDatabaseEntity>) {
        exercises += rows
    }

    override suspend fun insertSets(rows: List<SessionSetDatabaseEntity>) {
        sets += rows
    }

    override suspend fun updateSetResult(id: String, status: String?, count: Int?, weight: Double?) {
        updateSet(id) { it.copy(resultStatus = status, resultCount = count, resultWeight = weight) }
    }

    override suspend fun updateSetNote(id: String, note: String) {
        updateSet(id) { it.copy(note = note) }
    }

    override suspend fun updateCurrentExercise(id: String, exerciseId: String) {
        sessions[id]?.let { sessions[id] = it.copy(currentExerciseId = exerciseId) }
    }

    override suspend fun updateFinishedAt(id: String, finishedAt: Long) {
        sessions[id]?.let { sessions[id] = it.copy(finishedAt = finishedAt) }
    }

    // Каскад настоящей БД воспроизведён руками: упражнения сессии и их подходы уходят вместе с ней.
    override suspend fun deleteSession(id: String) {
        val exerciseIds = exercises.filter { it.sessionId == id }.map { it.id }.toSet()
        sets.removeAll { set -> set.sessionExerciseId in exerciseIds }
        exercises.removeAll { exercise -> exercise.sessionId == id }
        sessions.remove(id)
    }

    override suspend fun markPendingSets(sessionId: String, status: String) {
        val exerciseIds = exercises.filter { it.sessionId == sessionId }.map { it.id }.toSet()
        // Индексный проход вместо MutableList.replaceAll: последний на Kotlin/Native требует opt-in
        // (@ExperimentalNativeApi), а обновление по индексу — обычный stdlib (как в updateSet ниже).
        for (index in sets.indices) {
            val set = sets[index]
            if (set.sessionExerciseId in exerciseIds && set.resultStatus == null) {
                sets[index] = set.copy(resultStatus = status)
            }
        }
    }

    private fun sessionGraph(session: WorkoutSessionDatabaseEntity): SessionWithExercises =
        SessionWithExercises(
            session = session,
            // Порядок намеренно перемешан: репозиторий обязан восстановить его по position.
            exercises = exercises
                .filter { it.sessionId == session.id }
                .reversed()
                .map { sessionExercise ->
                    SessionExerciseWithSets(
                        sessionExercise = sessionExercise,
                        exercise = exerciseLibrary.getValue(sessionExercise.exerciseId),
                        sets = sets.filter { it.sessionExerciseId == sessionExercise.id }.reversed(),
                    )
                },
        )

    private fun updateSet(id: String, transform: (SessionSetDatabaseEntity) -> SessionSetDatabaseEntity) {
        val index = sets.indexOfFirst { it.id == id }
        if (index >= 0) {
            sets[index] = transform(sets[index])
        }
    }
}
