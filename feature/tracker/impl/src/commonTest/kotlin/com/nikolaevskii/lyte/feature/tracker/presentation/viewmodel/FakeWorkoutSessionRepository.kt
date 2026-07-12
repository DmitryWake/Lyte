package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.feature.tracker.domain.model.LastSessionDateEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import kotlin.time.Instant

/**
 * In-memory реализация с семантикой настоящего репозитория: мутации меняют [session] так же, как
 * настоящие — БД (результат/заметка подхода, ручной выбор упражнения, финализация с пропуском
 * незакрытых). Ошибки инъектируются полями `*Error`, вызовы пишутся в `*Calls`.
 */
internal class FakeWorkoutSessionRepository(
    initialSession: WorkoutSessionEntity? = null,
) : WorkoutSessionRepository {

    var session: WorkoutSessionEntity? = initialSession
    var activeSession: WorkoutSessionEntity? = null
    var getSessionError: Throwable? = null
    var getActiveSessionError: Throwable? = null
    var startSessionError: Throwable? = null
    var completeSetError: Throwable? = null
    var finishSessionError: Throwable? = null
    var startedSessionId: String = "started-session"

    val startSessionCalls = mutableListOf<WorkoutEntity>()
    val completeSetCalls = mutableListOf<Triple<String, Int, Double?>>()
    val skipSetCalls = mutableListOf<String>()
    val saveNoteCalls = mutableListOf<Pair<String, String>>()
    val setCurrentExerciseCalls = mutableListOf<Pair<String, String>>()
    val finishSessionCalls = mutableListOf<String>()

    override suspend fun getActiveSession(): WorkoutSessionEntity? {
        getActiveSessionError?.let { error -> throw error }
        return activeSession
    }

    override suspend fun getSession(id: String): WorkoutSessionEntity? {
        getSessionError?.let { error -> throw error }
        return session?.takeIf { candidate -> candidate.id == id }
    }

    override suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity> = emptyList()

    override suspend fun getLastSessionDates(): List<LastSessionDateEntity> = emptyList()

    override suspend fun startSession(workout: WorkoutEntity): String {
        startSessionError?.let { error -> throw error }
        startSessionCalls += workout
        return startedSessionId
    }

    override suspend fun completeSet(setId: String, count: Int, weight: Double?) {
        completeSetError?.let { error -> throw error }
        completeSetCalls += Triple(setId, count, weight)
        mutateSet(setId) { set ->
            set.copy(result = SessionSetResultEntity.Completed(SessionSetValueEntity(count = count, weight = weight)))
        }
    }

    override suspend fun skipSet(setId: String) {
        skipSetCalls += setId
        mutateSet(setId) { set -> set.copy(result = SessionSetResultEntity.Skipped) }
    }

    override suspend fun saveSetNote(setId: String, note: String) {
        saveNoteCalls += setId to note
        mutateSet(setId) { set -> set.copy(note = note) }
    }

    override suspend fun setCurrentExercise(sessionId: String, sessionExerciseId: String) {
        setCurrentExerciseCalls += sessionId to sessionExerciseId
        session = session?.let { current ->
            if (current.id == sessionId) current.copy(currentExerciseId = sessionExerciseId) else current
        }
    }

    override suspend fun finishSession(id: String) {
        finishSessionError?.let { error -> throw error }
        finishSessionCalls += id
        session = session?.let { current ->
            if (current.id != id) {
                return@let current
            }
            current.copy(
                finishedAt = Instant.fromEpochMilliseconds(0),
                exercises = current.exercises.map { exercise ->
                    exercise.copy(
                        sets = exercise.sets.map { set ->
                            if (set.result == null) set.copy(result = SessionSetResultEntity.Skipped) else set
                        },
                    )
                },
            )
        }
    }

    private fun mutateSet(setId: String, transform: (SessionSetEntity) -> SessionSetEntity) {
        session = session?.let { current ->
            current.copy(
                exercises = current.exercises.map { exercise ->
                    exercise.copy(
                        sets = exercise.sets.map { set -> if (set.id == setId) transform(set) else set },
                    )
                },
            )
        }
    }
}
