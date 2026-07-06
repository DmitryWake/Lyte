package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.domain.Workout

data class WorkoutDetailsUiState(
    val id: Long,
    val isLoading: Boolean = true,
    val workout: Workout? = null,
    val errorMessage: String? = null,
) : UiState

sealed interface WorkoutDetailsIntent : UiIntent {

    /** Вернуться назад. */
    data object Back : WorkoutDetailsIntent
}
