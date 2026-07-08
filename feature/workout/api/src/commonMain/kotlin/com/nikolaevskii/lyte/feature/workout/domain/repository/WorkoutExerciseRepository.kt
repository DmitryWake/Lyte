package com.nikolaevskii.lyte.feature.workout.domain.repository

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity

interface WorkoutExerciseRepository {

    suspend fun getExercises(): List<WorkoutExerciseEntity>

    suspend fun getExercise(id: String): WorkoutExerciseEntity?

    suspend fun createExercise(workoutExerciseEntity: WorkoutExerciseEntity)

    suspend fun editExercises(workoutExerciseEntity: WorkoutExerciseEntity)

    suspend fun deleteExercise(id: String)
}