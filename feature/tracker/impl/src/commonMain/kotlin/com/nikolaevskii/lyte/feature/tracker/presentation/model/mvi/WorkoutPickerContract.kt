package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutProgramUiModel

sealed interface WorkoutPickerUiState : UiState {

    /** Экран всегда открывается с загрузки списка программ. */
    data object Loading : WorkoutPickerUiState

    data class Error(val error: LyteError) : WorkoutPickerUiState

    /** Программ нет — пустое состояние с CTA «Новая программа» (уводит на вкладку «Тренировки»). */
    data object Empty : WorkoutPickerUiState

    data class Content(val programs: List<WorkoutProgramUiModel>) : WorkoutPickerUiState
}

sealed interface WorkoutPickerIntent : UiIntent {

    /** Тап по программе: открыть её превью перед стартом тренировки (спека 4.2). */
    data class OnProgramClicked(val id: String) : WorkoutPickerIntent

    /** Пустой список: уйти на вкладку «Тренировки», где программу можно создать. */
    data object OnCreateProgramClicked : WorkoutPickerIntent

    data object OnBack : WorkoutPickerIntent
}
