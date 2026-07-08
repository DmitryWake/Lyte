package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

data class WorkoutListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface WorkoutListIntent : UiIntent {

    /** Открыть детали тренировки [id]. */
    data class OpenDetails(val id: Long) : WorkoutListIntent
}
