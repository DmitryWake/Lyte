package com.nikolaevskii.lyte.feature.workout.domain.model

data class WorkoutExerciseWithRepsEntity(
    val exercise: WorkoutExerciseEntity,
    val reps: List<WorkoutRepEntity>
)
