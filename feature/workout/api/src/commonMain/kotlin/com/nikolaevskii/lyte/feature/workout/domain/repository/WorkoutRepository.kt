package com.nikolaevskii.lyte.feature.workout.domain.repository

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity

interface WorkoutRepository {

    suspend fun getWorkouts(): List<WorkoutItemEntity>

    suspend fun getWorkout(id: String): WorkoutEntity?

    suspend fun createWorkout(workoutEntity: WorkoutEntity)

    suspend fun editWorkout(workoutEntity: WorkoutEntity)

    suspend fun deleteWorkout(id: String)
}