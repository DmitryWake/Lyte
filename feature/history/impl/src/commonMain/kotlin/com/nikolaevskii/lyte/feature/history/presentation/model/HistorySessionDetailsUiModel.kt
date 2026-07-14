package com.nikolaevskii.lyte.feature.history.presentation.model

/**
 * Готовые к отрисовке детали завершённой сессии (5.2). Дата/время начала разложены по локальному
 * календарю ([year]/[monthNumber]/[dayOfMonth], [startHour]/[startMinute]) — формат и локализованные
 * подписи собирает UI-слой. [monthNumber] — 1..12.
 */
data class HistorySessionDetailsUiModel(
    val programName: String,
    val year: Int,
    val monthNumber: Int,
    val dayOfMonth: Int,
    val startHour: Int,
    val startMinute: Int,
    val durationMinutes: Int,
    val completedSetCount: Int,
    val totalSetCount: Int,
    val exercises: List<HistoryExerciseGroupUiModel>,
)
