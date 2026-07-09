package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity

data class WorkoutListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val programs: List<WorkoutItemEntity> = emptyList(),
    val pendingDeleteId: String? = null,
) : UiState

sealed interface WorkoutListIntent : UiIntent {

    /**
     * Перезагрузить список программ — экран шлёт при каждом появлении (в т.ч. при возврате из
     * редактора программы, чтобы подхватить создание/переименование/удаление упражнений).
     */
    data object Refresh : WorkoutListIntent

    /** Открыть детали программы [id] в редакторе программы (3.2). */
    data class OpenDetails(val id: String) : WorkoutListIntent

    /** Создать новую программу — открывает редактор программы (3.2) в режиме создания. */
    data object CreateProgram : WorkoutListIntent

    /** Запросить подтверждение удаления программы [id] — показывает диалог. */
    data class RequestDelete(val id: String) : WorkoutListIntent

    /** Подтвердить удаление программы, отмеченной [WorkoutListUiState.pendingDeleteId]. */
    data object ConfirmDelete : WorkoutListIntent

    /** Отменить удаление, закрыть диалог подтверждения. */
    data object CancelDelete : WorkoutListIntent
}
