package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import kotlin.time.Instant

/**
 * Готовая к отрисовке модель активной сессии (спека 4.3). Все преобразования домена делает маппер
 * `toActiveSessionUiModel` — Compose только рендерит и подставляет локализованные строки.
 *
 * [current] `null` — все подходы сессии разрешены (выполнены или пропущены): экран показывает
 * состояние «тренировка завершена» с кнопкой сохранения. [startedAt] — источник правды для
 * секундомера: elapsed всегда пересчитывается от него по wall-clock, а не копится счётчиком.
 * [completedCount] считает только выполненные подходы (пропущенные не в счёт).
 *
 * [setTones] — по сегменту на каждый подход сессии, в порядке упражнений: из них экран-итог собирает
 * трек `LyteProgressTrackMode.Tones`. Счётчики рядом не избыточны: [completedCount] — это «выполнено
 * без пропущенных», и по списку тонов экран считать его не должен.
 */
data class ActiveSessionUiModel(
    val sessionId: String,
    val programName: String,
    val startedAt: Instant,
    val completedCount: Int,
    val totalCount: Int,
    val setTones: List<LyteProgressTone>,
    val current: ActiveSessionCurrentUiModel?,
    val switcherRows: List<ActiveSessionSwitcherRowUiModel>,
)
