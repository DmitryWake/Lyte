package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity

/**
 * Маппит доменную программу в готовую к отрисовке [WorkoutPreviewUiModel]: нумерация упражнений,
 * подсчёт и перевод плановых подходов в значение дизайн-системы. Чистая функция без ресурсов —
 * формат значения и локализованные единицы подставляет UI-слой (`lyteSetValueLabel`).
 */
fun WorkoutEntity.toPreviewUiModel(): WorkoutPreviewUiModel =
    WorkoutPreviewUiModel(
        programName = name,
        exerciseCount = exercises.size,
        exercises = exercises.mapIndexed { index, exercise ->
            WorkoutPreviewExerciseUiModel(
                number = index + 1,
                name = exercise.exercise.name,
                description = exercise.exercise.description,
                sets = exercise.reps.map { rep -> rep.toSetValue() },
                accent = exercise.exercise.accent.toLyteAccent(),
                glyph = exercise.exercise.glyph.toLyteGlyph(),
            )
        },
    )

/** Конвенция домена: вес `null` или `0` — упражнение со своим весом, вес не показываем. */
private fun WorkoutRepEntity.toSetValue(): LyteSetValue =
    LyteSetValue(reps = count, weight = weight?.takeIf { value -> value > 0.0 })
