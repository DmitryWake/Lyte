package com.nikolaevskii.lyte.feature.workout.presentation.model

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity

/**
 * Чем закончилась шторка выбора упражнения. Обрабатывает владелец — экран редактора программы:
 * шторка лишь складывает результат в `ExercisePickerUiState.result` и ждёт, когда её закроют.
 */
sealed interface ExercisePickerResult {

    /** Пользователь выбрал готовое упражнение библиотеки. */
    data class Picked(val exercise: WorkoutExerciseEntity) : ExercisePickerResult

    /** Нужного упражнения в библиотеке нет — открыть форму создания, предзаполнив [name] запросом. */
    data class CreationRequested(val name: String) : ExercisePickerResult
}
