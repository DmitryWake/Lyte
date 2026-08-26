package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity

/**
 * Состояние формы создания упражнения библиотеки.
 *
 * [exercise] — сквозное поле: редактируемая модель формы, присутствует во всех состояниях, и по
 * [ExerciseCreatorContent.Created] владельцу отдаётся ровно она (`id` выдан при открытии). [content] —
 * фаза формы: редактирование / запись / готово.
 */
data class ExerciseCreatorUiState(
    val exercise: WorkoutExerciseEntity,
    val content: ExerciseCreatorContent = ExerciseCreatorContent.Editing(),
) : UiState {

    sealed interface ExerciseCreatorContent {

        /** [isSubmitEnabled] — решение ViewModel: можно ли нажать «Сохранить». */
        data class Editing(
            val isSubmitEnabled: Boolean = false,
            val error: LyteError? = null,
        ) : ExerciseCreatorContent

        /** Идёт запись: сабмит по построению недоступен. */
        data object Saving : ExerciseCreatorContent

        /** Терминальное: упражнение в библиотеке, шторку закрывает владелец. */
        data object Created : ExerciseCreatorContent
    }
}

/** События формы создания; решение принимает `ExerciseCreatorViewModel`. */
sealed interface ExerciseCreatorIntent : UiIntent {

    /** Пользователь изменил название упражнения. */
    data class OnNameChanged(val name: String) : ExerciseCreatorIntent

    /** Пользователь изменил описание упражнения (необязательное поле). */
    data class OnDescriptionChanged(val description: String) : ExerciseCreatorIntent

    /** Пользователь выбрал цвет упражнения. */
    data class OnAccentChanged(val accent: ExerciseAccent) : ExerciseCreatorIntent

    /** Пользователь выбрал знак движения. */
    data class OnGlyphChanged(val glyph: ExerciseGlyph) : ExerciseCreatorIntent

    /** Пользователь нажал «Сохранить». */
    data object OnCreateClicked : ExerciseCreatorIntent
}
