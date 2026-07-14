package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.feature.tracker.domain.model.LastSessionDateEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity

/**
 * Минимальный дубль репозитория сессий для тестов Истории: настраиваемый [finishedSessions] и
 * инъекция ошибки чтения ([getFinishedSessionsError]). Остальные методы Истории не нужны — no-op.
 */
internal class FakeWorkoutSessionRepository(
    var finishedSessions: List<WorkoutSessionItemEntity> = emptyList(),
    var session: WorkoutSessionEntity? = null,
) : WorkoutSessionRepository {

    var getFinishedSessionsError: Throwable? = null
    var getSessionError: Throwable? = null

    override suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity> {
        getFinishedSessionsError?.let { error -> throw error }
        return finishedSessions
    }

    override suspend fun getActiveSession(): WorkoutSessionEntity? = null

    override suspend fun getSession(id: String): WorkoutSessionEntity? {
        getSessionError?.let { error -> throw error }
        return session?.takeIf { candidate -> candidate.id == id }
    }

    override suspend fun getLastSessionDates(): List<LastSessionDateEntity> = emptyList()

    override suspend fun startSession(workout: WorkoutEntity): String = ""

    override suspend fun completeSet(setId: String, count: Int, weight: Double?) = Unit

    override suspend fun skipSet(setId: String) = Unit

    override suspend fun saveSetNote(setId: String, note: String) = Unit

    override suspend fun setCurrentExercise(sessionId: String, sessionExerciseId: String) = Unit

    override suspend fun finishSession(id: String) = Unit
}
