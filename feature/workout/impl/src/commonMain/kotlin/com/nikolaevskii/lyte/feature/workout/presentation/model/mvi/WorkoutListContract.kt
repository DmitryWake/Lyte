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
     * Открыть детали программы [id]. Пока заглушка — редактор программы (3.2) появится
     * отдельной задачей, обработчик в VM ничего не делает.
     */
    data class OpenDetails(val id: String) : WorkoutListIntent

    /** Создать новую программу. Пока заглушка — см. [OpenDetails]. */
    data object CreateProgram : WorkoutListIntent

    /** Запросить подтверждение удаления программы [id] — показывает диалог. */
    data class RequestDelete(val id: String) : WorkoutListIntent

    /** Подтвердить удаление программы, отмеченной [WorkoutListUiState.pendingDeleteId]. */
    data object ConfirmDelete : WorkoutListIntent

    /** Отменить удаление, закрыть диалог подтверждения. */
    data object CancelDelete : WorkoutListIntent
}
