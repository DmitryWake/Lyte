package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import kotlin.time.Clock
import kotlin.time.Instant

/** Управляемые часы: тесты двигают [current] руками и проверяют пересчёт elapsed от wall-clock. */
internal class FakeClock(var current: Instant) : Clock {

    override fun now(): Instant = current
}
