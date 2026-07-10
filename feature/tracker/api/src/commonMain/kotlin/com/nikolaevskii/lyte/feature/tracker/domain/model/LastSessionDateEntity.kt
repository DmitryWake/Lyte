package com.nikolaevskii.lyte.feature.tracker.domain.model

import kotlin.time.Instant

/** Дата последней завершённой сессии по программе — для подписи «посл. сессия» на карточках программ. */
data class LastSessionDateEntity(
    val programId: String,
    val finishedAt: Instant,
)
