package com.nikolaevskii.lyte.feature.history.presentation.model

/**
 * Готовая к отрисовке строка завершённой сессии для списка Истории (5.1). Содержит только примитивы;
 * локализацию (название месяца, единицы) подставляет UI-слой. Дату держим разложенной по локальному
 * календарю ([year]/[monthNumber]/[dayOfMonth]), а не форматированной строкой, чтобы формат оставался
 * в Compose. [monthNumber] — 1..12.
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
)
