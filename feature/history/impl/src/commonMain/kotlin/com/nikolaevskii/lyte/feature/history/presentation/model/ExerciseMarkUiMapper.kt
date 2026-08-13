package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph

/**
 * Маркер: домен → UI. Пара enum'ов намеренно разведена по модулям — `:core:core-workout` не знает
 * про Compose, `:core:core-design` не знает про домен, — поэтому перевод делает фича, как и для
 * остальных UiModel'ей. Исчерпывающий `when` вместо сопоставления по имени или `ordinal`: новое
 * значение в любом из двух наборов должно ломать компиляцию, а не тихо превращаться в дефолт.
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
