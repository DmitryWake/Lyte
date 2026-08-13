package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/** Упражнение в превью программы: порядковый номер, название, маркер и плановые подходы. */
data class WorkoutPreviewExerciseUiModel(
    val number: Int,
    val name: String,
    val sets: List<WorkoutPreviewSetUiModel>,
    val accent: LyteAccent = LyteAccent.Default,
    val glyph: LyteExerciseGlyph = LyteExerciseGlyph.Default,
)
