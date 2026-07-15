package com.nikolaevskii.lyte.core.workout.domain.model

data class WorkoutEntity(
    val id: String,
    val name: String,
    val description: String?,
    val exercises: List<WorkoutExerciseWithRepsEntity>
)
