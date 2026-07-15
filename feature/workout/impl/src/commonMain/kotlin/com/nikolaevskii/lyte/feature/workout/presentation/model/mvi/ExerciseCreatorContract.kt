package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity

/**
 * Состояние формы создания упражнения библиотеки. Форма редактирует само [exercise]: его `id`
 * генерируется сразу при открытии, поэтому по [isCreated] владельцу отдаётся ровно та модель,
 * которая записана в библиотеку.
 *
 * [isSubmitEnabled] — решение ViewModel, а не UI: можно ли сейчас нажать «Создать».
 */
data class ExerciseCreatorUiState(
    val exercise: WorkoutExerciseEntity,
    val isSubmitEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val isCreated: Boolean = false,
    val errorMessage: String? = null,
) : UiState

/** События формы создания; решение принимает `ExerciseCreatorViewModel`. */
sealed interface ExerciseCreatorIntent : UiIntent {

    /** Пользователь изменил название упражнения. */
    data class OnNameChanged(val name: String) : ExerciseCreatorIntent

    /** Пользователь изменил описание упражнения (необязательное поле). */
    data class OnDescriptionChanged(val description: String) : ExerciseCreatorIntent

    /** Пользователь нажал «Создать». */
    data object OnCreateClicked : ExerciseCreatorIntent
}
