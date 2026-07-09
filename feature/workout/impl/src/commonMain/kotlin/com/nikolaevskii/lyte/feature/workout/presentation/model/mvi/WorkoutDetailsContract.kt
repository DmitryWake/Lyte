package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel

data class WorkoutDetailsUiState(
    val id: String,
    val name: String = "",
    val description: String? = null,
    val exercises: List<WorkoutExerciseUiModel> = emptyList(),
    val editingExerciseIndex: Int? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState

sealed interface WorkoutDetailsIntent : UiIntent {

    /** Изменить название программы. */
    data class ChangeName(val name: String) : WorkoutDetailsIntent

    /** Переместить упражнение с [fromIndex] на [toIndex] (drag-хэндл в списке). */
    data class MoveExercise(val fromIndex: Int, val toIndex: Int) : WorkoutDetailsIntent

    /** Убрать упражнение [index] из программы. */
    data class RemoveExercise(val index: Int) : WorkoutDetailsIntent

    /** Добавить упражнение. Пока заглушка — пикер упражнений (3.3) появится отдельной задачей. */
    data object AddExercise : WorkoutDetailsIntent

    /** Открыть редактор подходов упражнения [index]. */
    data class EditExerciseSets(val index: Int) : WorkoutDetailsIntent

    /** Закрыть редактор подходов (кнопка «Готово» либо системное закрытие шторки). */
    data object CloseSetsEditor : WorkoutDetailsIntent

    /** Изменить число повторений подхода [setIndex] у редактируемого упражнения. */
    data class ChangeSetReps(val setIndex: Int, val reps: Int) : WorkoutDetailsIntent

    /** Изменить вес подхода [setIndex] у редактируемого упражнения. */
    data class ChangeSetWeight(val setIndex: Int, val weight: Double) : WorkoutDetailsIntent

    /** Добавить подход в конец списка редактируемого упражнения — клон последнего подхода. */
    data object AddSet : WorkoutDetailsIntent

    /** Убрать подход [setIndex] у редактируемого упражнения. Не выполняется, если он последний. */
    data class RemoveSet(val setIndex: Int) : WorkoutDetailsIntent

    /** Сохранить программу (создание или редактирование — в зависимости от того, как открыт экран) и вернуться назад. */
    data object Save : WorkoutDetailsIntent

    /** Вернуться назад без сохранения. */
    data object Back : WorkoutDetailsIntent
}
