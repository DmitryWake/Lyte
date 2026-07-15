package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.feature.tracker.presentation.util.formatWeight
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity

/**
 * Маппит доменную программу в готовую к отрисовке [WorkoutPreviewUiModel]: нумерация упражнений,
 * подсчёт подходов, выбор «с весом / со своим весом» и формат числа веса. Чистая функция без ресурсов —
 * локализованные единицы подставляет UI-слой.
 */
fun WorkoutEntity.toPreviewUiModel(): WorkoutPreviewUiModel =
    WorkoutPreviewUiModel(
        programName = name,
        exerciseCount = exercises.size,
        setCount = exercises.sumOf { exercise -> exercise.reps.size },
        exercises = exercises.mapIndexed { index, exercise ->
            WorkoutPreviewExerciseUiModel(
                number = index + 1,
                name = exercise.exercise.name,
                sets = exercise.reps.map { rep -> rep.toPreviewSet() },
            )
        },
    )

/** Конвенция домена: вес `null` или `0` — упражнение со своим весом, вес не показываем. */
private fun WorkoutRepEntity.toPreviewSet(): WorkoutPreviewSetUiModel {
    val weight = weight
    return if (weight != null && weight > 0.0) {
        WorkoutPreviewSetUiModel.Weighted(reps = count, weight = formatWeight(weight))
    } else {
        WorkoutPreviewSetUiModel.Bodyweight(reps = count)
    }
}
