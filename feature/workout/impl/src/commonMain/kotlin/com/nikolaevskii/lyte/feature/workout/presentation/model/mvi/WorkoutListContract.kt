package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.domain.Workout

data class WorkoutListUiState(
    val isLoading: Boolean = true,
    val items: List<Workout> = emptyList(),
    val errorMessage: String? = null,
) : UiState

sealed interface WorkoutListIntent : UiIntent {

    /** Открыть детали тренировки [id]. */
    data class OpenDetails(val id: Long) : WorkoutListIntent
}
