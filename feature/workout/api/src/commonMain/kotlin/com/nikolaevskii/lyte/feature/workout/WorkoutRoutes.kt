package com.nikolaevskii.lyte.feature.workout

import kotlinx.serialization.Serializable

@Serializable
data object WorkoutListRoute

@Serializable
data class WorkoutDetailsRoute(val id: String? = null)
