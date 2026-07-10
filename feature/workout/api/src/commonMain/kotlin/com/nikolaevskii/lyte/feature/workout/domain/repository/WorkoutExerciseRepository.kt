package com.nikolaevskii.lyte.feature.workout.domain.repository

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity

interface WorkoutExerciseRepository {

    /**
     * Библиотека упражнений, отсортированная по названию. [query] — подстрока названия;
     * регистр и обрамляющие пробелы не важны, пустой запрос отдаёт всю библиотеку.
     */
    suspend fun getExercises(query: String = ""): List<WorkoutExerciseEntity>

    suspend fun getExercise(id: String): WorkoutExerciseEntity?

    suspend fun createExercise(workoutExerciseEntity: WorkoutExerciseEntity)

    suspend fun editExercises(workoutExerciseEntity: WorkoutExerciseEntity)

    suspend fun deleteExercise(id: String)
}