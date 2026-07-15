package com.nikolaevskii.lyte.core.workout.domain.model

data class WorkoutExerciseEntity(
    val id: String,
    val name: String,
    val description: String? = null,
)
