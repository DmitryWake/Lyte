package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDao
import com.nikolaevskii.lyte.core.session.data.mapper.toDomainEntity
import com.nikolaevskii.lyte.core.session.data.mapper.toItemEntity
import com.nikolaevskii.lyte.core.session.data.mapper.toSessionRows
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class WorkoutSessionRepositoryImpl(
    private val workoutSessionDao: WorkoutSessionDao,
    private val clock: Clock,
) : WorkoutSessionRepository {

    override suspend fun getActiveSession(): WorkoutSessionEntity? =
        workoutSessionDao.getActiveSession()?.toDomainEntity()

    override suspend fun getSession(id: String): WorkoutSessionEntity? =
        workoutSessionDao.getSession(id)?.toDomainEntity()

    override suspend fun getFinishedSessions(): List<WorkoutSessionItemEntity> =
        workoutSessionDao.getFinishedItems().map { it.toItemEntity() }

    override suspend fun startSession(workout: WorkoutEntity): String {
        val rows = workout.toSessionRows(sessionId = Uuid.random().toString(), startedAt = clock.now())
        workoutSessionDao.insertSessionGraph(
            session = rows.session,
            exercises = rows.exercises,
            sets = rows.sets,
        )
        return rows.session.id
    }

    override suspend fun completeSet(setId: String, count: Int, weight: Double?) {
        workoutSessionDao.updateSetResult(
            id = setId,
            status = SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED,
            count = count,
            weight = weight,
        )
    }

    override suspend fun skipSet(setId: String) {
        workoutSessionDao.updateSetResult(
            id = setId,
            status = SessionSetDatabaseEntity.RESULT_STATUS_SKIPPED,
            count = null,
            weight = null,
        )
    }

    override suspend fun saveSetNote(setId: String, note: String) {
        workoutSessionDao.updateSetNote(id = setId, note = note)
    }

    override suspend fun setCurrentExercise(sessionId: String, sessionExerciseId: String) {
        workoutSessionDao.updateCurrentExercise(id = sessionId, exerciseId = sessionExerciseId)
    }

    override suspend fun finishSession(id: String) {
        workoutSessionDao.finishSession(id = id, finishedAt = clock.now().toEpochMilliseconds())
    }
}
