package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.ExercisePickerResult

/**
 * Состояние шторки выбора упражнения из библиотеки.
 *
 * [query] — сквозное поле: строка поиска отрисована в topContent во всех состояниях (включая Loading/Error)
 * и является ИСТОЧНИКОМ запросов (`observeQuery` читает `uiState.query`).
 *
 * [result] — сквозной терминальный one-shot: шторка сделала своё дело и должна быть закрыта владельцем.
 * Отдельного канала one-shot-событий у MVI-каркаса нет, поэтому результат живёт в состоянии; армом его
 * сделать нельзя — [content] должно продолжать рисоваться в кадре между результатом и закрытием.
 *
 * [content] — что показывает шторка прямо сейчас: исчерпывающий `when` без комбинаций флагов.
 */
data class ExercisePickerUiState(
    val query: String = "",
    val content: ExercisePickerContent = ExercisePickerContent.Loading,
    val result: ExercisePickerResult? = null,
) : UiState {

    sealed interface ExercisePickerContent {

        data object Loading : ExercisePickerContent

        data class Error(val error: LyteError) : ExercisePickerContent

        /** Пустой запрос и пустая выдача — библиотека пуста. */
        data object EmptyLibrary : ExercisePickerContent

        /** Непустой запрос и пустая выдача — ничего не найдено. */
        data object NotFound : ExercisePickerContent

        data class Exercises(val exercises: List<WorkoutExerciseEntity>) : ExercisePickerContent
    }
}

/** События шторки выбора; решение принимает `ExercisePickerViewModel`. */
sealed interface ExercisePickerIntent : UiIntent {

    /** Пользователь изменил текст в строке поиска. */
    data class OnQueryChanged(val query: String) : ExercisePickerIntent

    /** Пользователь тапнул по строке упражнения [exerciseId]. */
    data class OnExerciseClicked(val exerciseId: String) : ExercisePickerIntent

    /** Пользователь нажал «Создать новое упражнение». */
    data object OnCreateExerciseClicked : ExercisePickerIntent
}
