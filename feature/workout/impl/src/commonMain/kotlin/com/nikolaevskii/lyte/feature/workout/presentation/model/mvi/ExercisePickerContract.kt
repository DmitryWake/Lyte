package com.nikolaevskii.lyte.feature.workout.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.ExercisePickerResult

/**
 * Состояние шторки выбора упражнения из библиотеки.
 *
 * [exercises] — результат запроса к библиотеке по [query]; фильтрует и сортирует БД, поэтому второго
 * списка «всё, что есть» здесь нет. Пустой [exercises] читается по [query]: при пустом запросе это
 * «библиотека пуста», при непустом — «ничего не найдено».
 *
 * [result] — терминальное состояние шторки: она своё дело сделала и должна быть закрыта владельцем.
 * Отдельного канала one-shot-событий у MVI-каркаса нет, поэтому результат живёт в состоянии.
 */
data class ExercisePickerUiState(
    val query: String = "",
    val exercises: List<WorkoutExerciseEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val result: ExercisePickerResult? = null,
) : UiState

/** События шторки выбора; решение принимает `ExercisePickerViewModel`. */
sealed interface ExercisePickerIntent : UiIntent {

    /** Пользователь изменил текст в строке поиска. */
    data class OnQueryChanged(val query: String) : ExercisePickerIntent

    /** Пользователь тапнул по строке упражнения [exerciseId]. */
    data class OnExerciseClicked(val exerciseId: String) : ExercisePickerIntent

    /** Пользователь нажал «Создать новое упражнение». */
    data object OnCreateExerciseClicked : ExercisePickerIntent
}
