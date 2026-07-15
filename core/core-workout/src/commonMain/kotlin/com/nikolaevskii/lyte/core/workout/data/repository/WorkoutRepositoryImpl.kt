package com.nikolaevskii.lyte.core.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import com.nikolaevskii.lyte.core.workout.data.mapper.toDomainEntity
import com.nikolaevskii.lyte.core.workout.data.mapper.toItemEntity
import com.nikolaevskii.lyte.core.workout.data.mapper.toRows
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository

internal class WorkoutRepositoryImpl(
    private val workoutDao: WorkoutDao,
) : WorkoutRepository {

    override suspend fun getWorkouts(): List<WorkoutItemEntity> =
        workoutDao.getItems().map { workout -> workout.toItemEntity() }

    override suspend fun getWorkout(id: String): WorkoutEntity? =
        workoutDao.getWithExercises(id)?.toDomainEntity()

    override suspend fun createWorkout(workoutEntity: WorkoutEntity) {
        save(workoutEntity)
    }

    override suspend fun editWorkout(workoutEntity: WorkoutEntity) {
        save(workoutEntity)
    }

    override suspend fun deleteWorkout(id: String) {
        // Программу, на которую ссылаются сессии трекера, архивируем (soft delete), а не удаляем —
        // иначе история потеряет ссылку на программу. Решение принимает DAO в одной транзакции.
        workoutDao.deleteOrArchiveWorkout(id)
    }

    private suspend fun save(workoutEntity: WorkoutEntity) {
        val rows = workoutEntity.toRows()
        workoutDao.saveWorkoutGraph(
            workout = rows.workout,
            exercises = rows.exercises,
            crossRefs = rows.crossRefs,
            sets = rows.sets,
        )
    }
}
