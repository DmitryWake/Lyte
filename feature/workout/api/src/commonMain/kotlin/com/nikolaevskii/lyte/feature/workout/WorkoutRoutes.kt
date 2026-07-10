package com.nikolaevskii.lyte.feature.workout

import kotlinx.serialization.Serializable

/**
 * Граф вкладки «Тренировки» — собственный back stack вкладки. Живёт в `:api`, чтобы шелл (`:shared`) и
 * другие фичи могли переключаться на вкладку через `LyteNavigator.switchTab(WorkoutTabGraph)`.
 */
@Serializable
data object WorkoutTabGraph

@Serializable
data object WorkoutListRoute

@Serializable
data class WorkoutDetailsRoute(val id: String? = null)
