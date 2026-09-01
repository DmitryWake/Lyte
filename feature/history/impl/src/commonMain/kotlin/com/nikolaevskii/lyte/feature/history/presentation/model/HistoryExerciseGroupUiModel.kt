package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Упражнение в деталях сессии (5.2): маркер, заголовок-имя и строки диффа по его подходам.
 *
 * [accent]/[glyph], как и имя, читаются живыми из библиотеки, а не из снапшота сессии: перекрасил
 * упражнение — история перекрасилась вслед. Маркер программы, наоборот, снапшотится (см.
 * `SessionProgramEntity`).
 */
data class HistoryExerciseGroupUiModel(
    val exerciseId: String,
    val exerciseName: String,
    val accent: LyteAccent,
    val glyph: LyteExerciseGlyph,
    val rows: List<HistoryDiffRowUiModel>,
)
