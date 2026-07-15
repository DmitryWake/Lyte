package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutProgramUiModel

sealed interface WorkoutListUiState : UiState {

    data object Loading : WorkoutListUiState

    /** Список не удалось прочитать (данных ещё нет). */
    data class Error(val error: LyteError) : WorkoutListUiState

    data object Empty : WorkoutListUiState

    data class Content(
        val programs: List<WorkoutProgramUiModel>,
        /** Диалог удаления существует только над списком; несёт имя — экран не ищет программу по id. */
        val pendingDelete: WorkoutProgramUiModel? = null,
        /** Неудачное удаление — баннер над списком (список остаётся). */
        val actionError: LyteError? = null,
    ) : WorkoutListUiState
}

/** События экрана списка программ; решение принимает `WorkoutListViewModel`. */
sealed interface WorkoutListIntent : UiIntent {

    /** Пользователь тапнул по программе [id]. */
    data class OnProgramClicked(val id: String) : WorkoutListIntent

    /** Пользователь нажал «Новая программа». */
    data object OnCreateProgramClicked : WorkoutListIntent

    /** Пользователь нажал «удалить» на программе [id]. */
    data class OnDeleteProgramClicked(val id: String) : WorkoutListIntent

    /** Пользователь подтвердил удаление отмеченной программы. */
    data object OnDeleteConfirmed : WorkoutListIntent

    /** Пользователь закрыл диалог подтверждения удаления. */
    data object OnDeleteDismissed : WorkoutListIntent
}
