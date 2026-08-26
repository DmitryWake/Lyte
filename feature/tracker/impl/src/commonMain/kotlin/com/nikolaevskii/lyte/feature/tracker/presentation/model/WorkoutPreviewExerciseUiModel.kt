package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Упражнение в превью программы: порядковый номер, название, описание, маркер и плановые подходы.
 *
 * [number] — ещё и идентичность внутри программы: одно и то же упражнение библиотеки может стоять в
 * ней дважды, поэтому id упражнения в качестве ключа не годится, а порядковый номер уникален по
 * построению. По нему же экран просит открыть шторку с описанием.
 *
 * [description] пустое у упражнений, которым его не задали, — тогда шторка показывает один маркер.
 */
data class WorkoutPreviewExerciseUiModel(
    val number: Int,
    val name: String,
    val description: String?,
    val sets: List<LyteSetValue>,
    val accent: LyteAccent = LyteAccent.Default,
    val glyph: LyteExerciseGlyph = LyteExerciseGlyph.Default,
)
