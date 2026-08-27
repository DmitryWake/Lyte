package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Готовая к отрисовке строка завершённой сессии для списка Истории (5.1). Локализацию (название
 * месяца, единицы) подставляет UI-слой. Дату держим разложенной по локальному календарю
 * ([year]/[monthNumber]/[dayOfMonth]), а не форматированной строкой, чтобы формат оставался
 * в Compose. [monthNumber] — 1..12.
 *
 * [daysAgo] — сколько дней назад прошла сессия, если она попадает в последнюю неделю: `0` — сегодня,
 * `1` — вчера, `2..6` — «N дней назад». `null` — сессия старее недели (или, при сбитых часах, из
 * будущего): тогда карточка показывает обычную дату. Считать «сегодня» внутри карточки нельзя —
 * модель обязана оставаться результатом чистой функции.
 *
 * [setTones] — по тону на подход в порядке сессии: из них рисуется трек карточки.
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
    val daysAgo: Int?,
    val durationMinutes: Int,
    val setTones: List<LyteProgressTone>,
    val accent: LyteAccent = LyteAccent.Default,
    val glyph: LyteExerciseGlyph = LyteExerciseGlyph.Default,
)
