package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.feature.workout.data.mapper.toDomainEntity
import com.nikolaevskii.lyte.feature.workout.data.mapper.toDatabaseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutExerciseRepository

internal class WorkoutExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
) : WorkoutExerciseRepository {

    override suspend fun getExercises(): List<WorkoutExerciseEntity> =
        exerciseDao.getAll().map { exercise -> exercise.toDomainEntity() }

    override suspend fun getExercise(id: String): WorkoutExerciseEntity? =
        exerciseDao.getById(id)?.toDomainEntity()

    override suspend fun createExercise(workoutExerciseEntity: WorkoutExerciseEntity) {
        exerciseDao.upsert(workoutExerciseEntity.toDatabaseEntity())
    }

    override suspend fun editExercises(workoutExerciseEntity: WorkoutExerciseEntity) {
        exerciseDao.upsert(workoutExerciseEntity.toDatabaseEntity())
    }

    override suspend fun deleteExercise(id: String) {
        exerciseDao.deleteById(id)
    }
}
