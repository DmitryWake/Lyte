package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Готовая к отрисовке строка завершённой сессии для списка Истории (5.1). Локализацию (название
 * месяца, единицы) подставляет UI-слой. Дату держим разложенной по локальному календарю
 * ([year]/[monthNumber]/[dayOfMonth]), а не форматированной строкой, чтобы формат оставался
 * в Compose. [monthNumber] — 1..12.
 *
 * [accent]/[glyph] — маркер программы из снапшота сессии, а не из живой программы: карточка обязана
 * пережить перекраску и удаление программы.
 */
data class HistorySessionUiModel(
    val id: String,
    val programName: String,
    val year: Int,
    val monthNumber: Int,
    val dayOfMonth: Int,
    val durationMinutes: Int,
    val completedSetCount: Int,
    val totalSetCount: Int,
    val accent: LyteAccent = LyteAccent.Default,
    val glyph: LyteExerciseGlyph = LyteExerciseGlyph.Default,
)
