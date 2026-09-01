package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone

/**
 * Готовые к отрисовке детали завершённой сессии (5.2). Дата/время начала разложены по локальному
 * календарю ([year]/[monthNumber]/[dayOfMonth], [startHour]/[startMinute]) — формат и локализованные
 * подписи собирает UI-слой. [monthNumber] — 1..12.
 *
 * [setTones] — по тону на подход в порядке сессии (упражнение → подход): из них рисуется трек под
 * шапкой. Счётчика «сколько из скольких» экран не показывает — его заменил этот трек.
 */
data class HistorySessionDetailsUiModel(
    val programName: String,
    val year: Int,
    val monthNumber: Int,
    val dayOfMonth: Int,
    val startHour: Int,
    val startMinute: Int,
    val durationMinutes: Int,
    val setTones: List<LyteProgressTone>,
    val exercises: List<HistoryExerciseGroupUiModel>,
)
