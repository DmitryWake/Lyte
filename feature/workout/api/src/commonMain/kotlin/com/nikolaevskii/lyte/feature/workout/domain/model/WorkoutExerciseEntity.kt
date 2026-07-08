package com.nikolaevskii.lyte.feature.workout.domain.model

data class WorkoutExerciseEntity(
    val id: String,
    val name: String,
    val description: String? = null,
)
