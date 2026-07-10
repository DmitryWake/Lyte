package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity

data class WorkoutPickerUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val programs: List<WorkoutItemEntity> = emptyList(),
) : UiState

sealed interface WorkoutPickerIntent : UiIntent {

    /** Тап по программе: начать по ней тренировку. Экран превью (спека 4.2) ещё не реализован. */
    data class OnProgramClicked(val id: String) : WorkoutPickerIntent

    /** Пустой список: уйти на вкладку «Тренировки», где программу можно создать. */
    data object OnCreateProgramClicked : WorkoutPickerIntent

    data object OnBack : WorkoutPickerIntent
}
