package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseSheet
import com.nikolaevskii.lyte.feature.workout.presentation.model.WorkoutExerciseUiModel

data class WorkoutDetailsUiState(
    val id: String,
    val name: String = "",
    val description: String? = null,
    val exercises: List<WorkoutExerciseUiModel> = emptyList(),
    val editingExerciseIndex: Int? = null,
    val exerciseSheet: WorkoutExerciseSheet? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) : UiState

/**
 * События экрана редактора программы: что пользователь сделал, а не что должно произойти.
 * Решение принимает `WorkoutDetailsViewModel`.
 */
sealed interface WorkoutDetailsIntent : UiIntent {

    /** Пользователь изменил название программы. */
    data class OnNameChanged(val name: String) : WorkoutDetailsIntent

    /** Пользователь перетащил упражнение с [fromIndex] на [toIndex] за drag-хэндл. */
    data class OnExerciseMoved(val fromIndex: Int, val toIndex: Int) : WorkoutDetailsIntent

    /** Пользователь нажал «удалить» на упражнении [index]. */
    data class OnRemoveExerciseClicked(val index: Int) : WorkoutDetailsIntent

    /** Пользователь нажал «Добавить упражнение». */
    data object OnAddExerciseClicked : WorkoutDetailsIntent

    /**
     * Пользователь закрыл шторку добавления упражнения — свайпом вниз либо тапом по скриму.
     * Какая именно шторка открыта и куда возвращаться, ViewModel знает из состояния.
     */
    data object OnExerciseSheetDismissed : WorkoutDetailsIntent

    /** Пользователь нажал «Создать новое упражнение», введя в поиске [query]. */
    data class OnCreateExerciseClicked(val query: String) : WorkoutDetailsIntent

    /**
     * Пользователь выбрал [exercise]: либо тапнул по строке библиотеки, либо только что создал его.
     * Для программы это одно и то же событие.
     */
    data class OnExerciseSelected(val exercise: WorkoutExerciseEntity) : WorkoutDetailsIntent

    /** Пользователь нажал «редактировать подходы» на упражнении [index]. */
    data class OnEditSetsClicked(val index: Int) : WorkoutDetailsIntent

    /** Пользователь закрыл редактор подходов — кнопкой «Готово» либо свайпом. */
    data object OnSetsEditorDismissed : WorkoutDetailsIntent

    /** Пользователь изменил число повторений подхода [setIndex]. */
    data class OnSetRepsChanged(val setIndex: Int, val reps: Int) : WorkoutDetailsIntent

    /** Пользователь изменил вес подхода [setIndex]. */
    data class OnSetWeightChanged(val setIndex: Int, val weight: Double) : WorkoutDetailsIntent

    /** Пользователь нажал «Добавить подход». */
    data object OnAddSetClicked : WorkoutDetailsIntent

    /** Пользователь нажал «удалить» на подходе [setIndex]. */
    data class OnRemoveSetClicked(val setIndex: Int) : WorkoutDetailsIntent

    /** Пользователь нажал «Сохранить». */
    data object OnSaveClicked : WorkoutDetailsIntent

    /** Пользователь нажал «назад». */
    data object OnBackClicked : WorkoutDetailsIntent
}
