package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity

data class WorkoutListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val programs: List<WorkoutItemEntity> = emptyList(),
    val pendingDeleteId: String? = null,
) : UiState

/** События экрана списка программ; решение принимает `WorkoutListViewModel`. */
sealed interface WorkoutListIntent : UiIntent {

    /**
     * Экран показан — в том числе при возврате из редактора программы, поэтому список нужно
     * перечитать, чтобы подхватить создание/переименование/удаление.
     */
    data object OnScreenShown : WorkoutListIntent

    /** Пользователь тапнул по программе [id]. */
    data class OnProgramClicked(val id: String) : WorkoutListIntent

    /** Пользователь нажал «Новая программа». */
    data object OnCreateProgramClicked : WorkoutListIntent

    /** Пользователь нажал «удалить» на программе [id]. */
    data class OnDeleteProgramClicked(val id: String) : WorkoutListIntent

    /** Пользователь подтвердил удаление программы, отмеченной [WorkoutListUiState.pendingDeleteId]. */
    data object OnDeleteConfirmed : WorkoutListIntent

    /** Пользователь закрыл диалог подтверждения удаления. */
    data object OnDeleteDismissed : WorkoutListIntent
}
