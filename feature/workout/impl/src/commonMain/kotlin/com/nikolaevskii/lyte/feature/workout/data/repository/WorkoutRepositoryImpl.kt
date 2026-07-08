package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import com.nikolaevskii.lyte.feature.workout.data.mapper.toDomainEntity
import com.nikolaevskii.lyte.feature.workout.data.mapper.toItemEntity
import com.nikolaevskii.lyte.feature.workout.data.mapper.toRows
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository

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
        workoutDao.deleteWorkout(id)
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
