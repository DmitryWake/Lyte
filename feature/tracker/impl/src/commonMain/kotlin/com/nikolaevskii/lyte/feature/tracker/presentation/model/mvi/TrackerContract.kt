package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

data class TrackerUiState(
    val completedWorkoutsToday: Int = 0,
) : UiState

sealed interface TrackerIntent : UiIntent {

    /** Перейти к списку тренировок. */
    data object OpenWorkouts : TrackerIntent
}
