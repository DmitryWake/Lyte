package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

data class WorkoutDetailsUiState(
    val id: Long,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface WorkoutDetailsIntent : UiIntent {

    /** Вернуться назад. */
    data object Back : WorkoutDetailsIntent
}
