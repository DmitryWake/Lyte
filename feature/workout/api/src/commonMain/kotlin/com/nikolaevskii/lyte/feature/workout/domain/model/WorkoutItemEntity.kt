package com.nikolaevskii.lyte.feature.workout.domain.model

data class WorkoutItemEntity(
    val id: String,
    val name: String,
    val description: String?,
    val exerciseCount: Int,
)
