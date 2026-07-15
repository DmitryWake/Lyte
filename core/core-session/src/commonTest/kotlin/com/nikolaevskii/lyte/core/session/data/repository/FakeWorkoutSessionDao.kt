package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.db.session.SessionExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionExerciseWithSets
import com.nikolaevskii.lyte.core.db.session.SessionItemWithSetCounts
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

    override suspend fun getFinishedItems(): List<SessionItemWithSetCounts> =
        sessions.values
            .filter { it.finishedAt != null }
            .sortedByDescending { it.finishedAt }
            .map { session ->
                val sessionSets = setsOf(session.id)
                SessionItemWithSetCounts(
                    id = session.id,
                    programId = session.programId,
                    programName = session.programName,
                    startedAt = session.startedAt,
                    finishedAt = session.finishedAt ?: 0L,
                    totalSetCount = sessionSets.size,
                    completedSetCount = sessionSets.count {
                        it.resultStatus == SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED
                    },
                )
            }

    override fun observeFinishedItems(): Flow<List<SessionItemWithSetCounts>> = flow { emit(getFinishedItems()) }

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

    private fun setsOf(sessionId: String): List<SessionSetDatabaseEntity> {
        val exerciseIds = exercises.filter { it.sessionId == sessionId }.map { it.id }.toSet()
        return sets.filter { it.sessionExerciseId in exerciseIds }
    }

    private fun updateSet(id: String, transform: (SessionSetDatabaseEntity) -> SessionSetDatabaseEntity) {
        val index = sets.indexOfFirst { it.id == id }
        if (index >= 0) {
            sets[index] = transform(sets[index])
        }
    }
}
