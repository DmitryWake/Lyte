package com.nikolaevskii.lyte.feature.workout.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph

/**
 * Маркер: домен ↔ UI. Пара enum'ов намеренно разведена по модулям — `:core:core-workout` не знает
 * про Compose, `:core:core-design` не знает про домен, — поэтому перевод делает фича, как и для
 * остальных UiModel'ей. Исчерпывающий `when` вместо сопоставления по имени или `ordinal`: новое
 * значение в любом из двух наборов должно ломать компиляцию, а не тихо превращаться в дефолт.
 *
 * Обратное направление нужно пикерам маркера: `LyteAccentPicker`/`LyteExerciseIconPicker` говорят
 * значениями дизайн-системы, а состояние экрана и интенты — доменными.
 */
internal fun ExerciseAccent.toLyteAccent(): LyteAccent = when (this) {
    ExerciseAccent.Coral -> LyteAccent.Coral
    ExerciseAccent.Indigo -> LyteAccent.Indigo
    ExerciseAccent.Lime -> LyteAccent.Lime
    ExerciseAccent.Amber -> LyteAccent.Amber
    ExerciseAccent.Teal -> LyteAccent.Teal
    ExerciseAccent.Slate -> LyteAccent.Slate
}

internal fun ExerciseGlyph.toLyteGlyph(): LyteExerciseGlyph = when (this) {
    ExerciseGlyph.Squat -> LyteExerciseGlyph.Squat
    ExerciseGlyph.Deadlift -> LyteExerciseGlyph.Deadlift
    ExerciseGlyph.BenchPress -> LyteExerciseGlyph.BenchPress
    ExerciseGlyph.PullUp -> LyteExerciseGlyph.PullUp
    ExerciseGlyph.DumbbellPress -> LyteExerciseGlyph.DumbbellPress
    ExerciseGlyph.Curl -> LyteExerciseGlyph.Curl
    ExerciseGlyph.Crunch -> LyteExerciseGlyph.Crunch
    ExerciseGlyph.Stretch -> LyteExerciseGlyph.Stretch
    ExerciseGlyph.Rack -> LyteExerciseGlyph.Rack
    ExerciseGlyph.Machine -> LyteExerciseGlyph.Machine
}

internal fun LyteAccent.toExerciseAccent(): ExerciseAccent = when (this) {
    LyteAccent.Coral -> ExerciseAccent.Coral
    LyteAccent.Indigo -> ExerciseAccent.Indigo
    LyteAccent.Lime -> ExerciseAccent.Lime
    LyteAccent.Amber -> ExerciseAccent.Amber
    LyteAccent.Teal -> ExerciseAccent.Teal
    LyteAccent.Slate -> ExerciseAccent.Slate
}

internal fun LyteExerciseGlyph.toExerciseGlyph(): ExerciseGlyph = when (this) {
    LyteExerciseGlyph.Squat -> ExerciseGlyph.Squat
    LyteExerciseGlyph.Deadlift -> ExerciseGlyph.Deadlift
    LyteExerciseGlyph.BenchPress -> ExerciseGlyph.BenchPress
    LyteExerciseGlyph.PullUp -> ExerciseGlyph.PullUp
    LyteExerciseGlyph.DumbbellPress -> ExerciseGlyph.DumbbellPress
    LyteExerciseGlyph.Curl -> ExerciseGlyph.Curl
    LyteExerciseGlyph.Crunch -> ExerciseGlyph.Crunch
    LyteExerciseGlyph.Stretch -> ExerciseGlyph.Stretch
    LyteExerciseGlyph.Rack -> ExerciseGlyph.Rack
    LyteExerciseGlyph.Machine -> ExerciseGlyph.Machine
}
