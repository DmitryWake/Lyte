package com.nikolaevskii.lyte.feature.tracker.presentation.model

import kotlin.time.Instant

/**
 * Готовая к отрисовке модель активной сессии (спека 4.3). Все преобразования домена делает маппер
 * `toActiveSessionUiModel` — Compose только рендерит и подставляет локализованные строки.
 *
 * [current] `null` — все подходы сессии разрешены (выполнены или пропущены): экран показывает
 * состояние «все подходы выполнены» с кнопкой финализации. [startedAt] — источник правды для
 * секундомера: elapsed всегда пересчитывается от него по wall-clock, а не копится счётчиком.
 * [completedCount] считает только выполненные подходы (пропущенные не в счёт).
 */
data class ActiveSessionUiModel(
    val sessionId: String,
    val programName: String,
    val startedAt: Instant,
    val completedCount: Int,
    val totalCount: Int,
    val current: ActiveSessionCurrentUiModel?,
    val switcherRows: List<ActiveSessionSwitcherRowUiModel>,
)
