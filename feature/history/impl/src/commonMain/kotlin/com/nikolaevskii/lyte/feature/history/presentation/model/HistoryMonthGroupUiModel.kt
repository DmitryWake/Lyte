package com.nikolaevskii.lyte.feature.history.presentation.model

/**
 * Группа сессий одного месяца в списке Истории (5.1) — заголовок «месяц» + сессии этого месяца.
 * Группируем по паре ([year], [monthNumber]), чтобы одинаковые месяцы разных лет не сливались.
 * [monthNumber] — 1..12.
 */
data class HistoryMonthGroupUiModel(
    val year: Int,
    val monthNumber: Int,
    val sessions: List<HistorySessionUiModel>,
)
