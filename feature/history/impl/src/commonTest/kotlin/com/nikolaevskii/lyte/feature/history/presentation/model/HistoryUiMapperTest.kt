package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.feature.history.TEST_TIME_ZONE
import com.nikolaevskii.lyte.feature.history.finishedSession
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionItemEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HistoryUiMapperTest {

    @Test
    fun groupsByMonthDescendingAndMapsFields() {
        val sessions = listOf(
            finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 18, 24), durationMinutes = 52, completedSetCount = 15, totalSetCount = 16),
            finishedSession("2", "Pull Day", LocalDateTime(2026, Month.JUNE, 30, 19, 0), durationMinutes = 58, completedSetCount = 17, totalSetCount = 17),
            finishedSession("3", "Leg Day", LocalDateTime(2026, Month.JUNE, 28, 8, 0), durationMinutes = 61, completedSetCount = 13, totalSetCount = 14),
        )

        val groups = sessions.toMonthGroups(TEST_TIME_ZONE)

        assertEquals(listOf(2026 to 7, 2026 to 6), groups.map { it.year to it.monthNumber })
        val july = groups[0]
        assertEquals(1, july.sessions.size)
        val push = july.sessions.single()
        assertEquals("Push Day", push.programName)
        assertEquals(2, push.dayOfMonth)
        assertEquals(52, push.durationMinutes)
        assertEquals(15, push.completedSetCount)
        assertEquals(16, push.totalSetCount)
        // Внутри месяца порядок тоже по убыванию finishedAt: 30 июня раньше 28 июня.
        assertEquals(listOf(30, 28), groups[1].sessions.map { it.dayOfMonth })
    }

    @Test
    fun sortsUnorderedInputByFinishedAtDescending() {
        val sessions = listOf(
            finishedSession("old", "Leg Day", LocalDateTime(2026, Month.MAY, 1, 12, 0), durationMinutes = 40, completedSetCount = 10, totalSetCount = 10),
            finishedSession("new", "Push Day", LocalDateTime(2026, Month.AUGUST, 1, 12, 0), durationMinutes = 40, completedSetCount = 10, totalSetCount = 10),
        )

        val groups = sessions.toMonthGroups(TEST_TIME_ZONE)

        assertEquals(listOf(2026 to 8, 2026 to 5), groups.map { it.year to it.monthNumber })
    }

    @Test
    fun keepsSameMonthOfDifferentYearsInSeparateGroups() {
        val sessions = listOf(
            finishedSession("a", "Push Day", LocalDateTime(2026, Month.JULY, 10, 12, 0), durationMinutes = 50, completedSetCount = 8, totalSetCount = 8),
            finishedSession("b", "Push Day", LocalDateTime(2025, Month.JULY, 10, 12, 0), durationMinutes = 50, completedSetCount = 8, totalSetCount = 8),
        )

        val groups = sessions.toMonthGroups(TEST_TIME_ZONE)

        assertEquals(listOf(2026 to 7, 2025 to 7), groups.map { it.year to it.monthNumber })
        assertEquals(1, groups[0].sessions.size)
        assertEquals(1, groups[1].sessions.size)
    }

    @Test
    fun truncatesDurationToWholeMinutes() {
        // Старт на 52 мин 40 сек раньше окончания → 52 целых минуты.
        val base = finishedSession("x", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 0, completedSetCount = 1, totalSetCount = 1)
        val session = base.copy(startedAt = base.finishedAt - (52.minutes + 40.seconds))

        val group = listOf(session).toMonthGroups(TEST_TIME_ZONE).single()

        assertEquals(52, group.sessions.single().durationMinutes)
    }

    @Test
    fun emptyInputProducesEmptyList() {
        assertTrue(emptyList<WorkoutSessionItemEntity>().toMonthGroups(TEST_TIME_ZONE).isEmpty())
    }
}
