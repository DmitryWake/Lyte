package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.feature.history.TEST_TIME_ZONE
import com.nikolaevskii.lyte.feature.history.TEST_TODAY
import com.nikolaevskii.lyte.feature.history.finishedSession
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HistoryUiMapperTest {

    @Test
    fun groupsByMonthDescendingAndMapsFields() {
        val sessions = listOf(
            finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 18, 24), durationMinutes = 52),
            finishedSession("2", "Pull Day", LocalDateTime(2026, Month.JUNE, 30, 19, 0), durationMinutes = 58),
            finishedSession("3", "Leg Day", LocalDateTime(2026, Month.JUNE, 28, 8, 0), durationMinutes = 61),
        )

        val groups = sessions.toMonthGroups(TEST_TIME_ZONE, TEST_TODAY)

        assertEquals(listOf(2026 to 7, 2026 to 6), groups.map { it.year to it.monthNumber })
        val july = groups[0]
        assertEquals(1, july.sessions.size)
        val push = july.sessions.single()
        assertEquals("Push Day", push.programName)
        assertEquals(2, push.dayOfMonth)
        assertEquals(52, push.durationMinutes)
        // Внутри месяца порядок тоже по убыванию finishedAt: 30 июня раньше 28 июня.
        assertEquals(listOf(30, 28), groups[1].sessions.map { it.dayOfMonth })
    }

    @Test
    fun mapsProgramMarkFromSessionSnapshot() {
        val sessions = listOf(
            finishedSession(
                "1",
                "Push Day",
                LocalDateTime(2026, Month.JULY, 2, 18, 24),
                durationMinutes = 52,
                accent = ExerciseAccent.Indigo,
                glyph = ExerciseGlyph.BenchPress,
            ),
            // Сессия без маркера в снапшоте: дефолт домена доезжает как дефолт дизайн-системы.
            finishedSession(
                "2",
                "Утренняя",
                LocalDateTime(2026, Month.JULY, 1, 8, 0),
                durationMinutes = 30,
            ),
        )

        val july = sessions.toMonthGroups(TEST_TIME_ZONE, TEST_TODAY).single()

        assertEquals(listOf(LyteAccent.Indigo, LyteAccent.Slate), july.sessions.map { it.accent })
        assertEquals(listOf(LyteExerciseGlyph.BenchPress, LyteExerciseGlyph.Squat), july.sessions.map { it.glyph })
    }

    @Test
    fun sortsUnorderedInputByFinishedAtDescending() {
        val sessions = listOf(
            finishedSession("old", "Leg Day", LocalDateTime(2026, Month.MAY, 1, 12, 0), durationMinutes = 40),
            finishedSession("new", "Push Day", LocalDateTime(2026, Month.AUGUST, 1, 12, 0), durationMinutes = 40),
        )

        val groups = sessions.toMonthGroups(TEST_TIME_ZONE, TEST_TODAY)

        assertEquals(listOf(2026 to 8, 2026 to 5), groups.map { it.year to it.monthNumber })
    }

    @Test
    fun keepsSameMonthOfDifferentYearsInSeparateGroups() {
        val sessions = listOf(
            finishedSession("a", "Push Day", LocalDateTime(2026, Month.JULY, 10, 12, 0), durationMinutes = 50),
            finishedSession("b", "Push Day", LocalDateTime(2025, Month.JULY, 10, 12, 0), durationMinutes = 50),
        )

        val groups = sessions.toMonthGroups(TEST_TIME_ZONE, TEST_TODAY)

        assertEquals(listOf(2026 to 7, 2025 to 7), groups.map { it.year to it.monthNumber })
        assertEquals(1, groups[0].sessions.size)
        assertEquals(1, groups[1].sessions.size)
    }

    @Test
    fun truncatesDurationToWholeMinutes() {
        // Старт на 52 мин 40 сек раньше окончания → 52 целых минуты.
        val base = finishedSession("x", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 0)
        val session = base.copy(startedAt = base.finishedAt - (52.minutes + 40.seconds))

        val group = listOf(session).toMonthGroups(TEST_TIME_ZONE, TEST_TODAY).single()

        assertEquals(52, group.sessions.single().durationMinutes)
    }

    @Test
    fun mapsSetOutcomesToTrackTonesInOrder() {
        val sessions = listOf(
            finishedSession(
                "1",
                "Push Day",
                LocalDateTime(2026, Month.JULY, 2, 18, 24),
                durationMinutes = 52,
                setOutcomes = listOf(
                    SessionSetOutcomeEntity.MET,
                    SessionSetOutcomeEntity.EXCEEDED,
                    SessionSetOutcomeEntity.MISSED,
                    SessionSetOutcomeEntity.SKIPPED,
                    // Подход без результата в завершённой сессии не встречается, но тон у него есть.
                    null,
                ),
            ),
        )

        val session = sessions.toMonthGroups(TEST_TIME_ZONE, TEST_TODAY).single().sessions.single()

        assertEquals(
            listOf(
                LyteProgressTone.Met,
                LyteProgressTone.Positive,
                LyteProgressTone.Negative,
                LyteProgressTone.Skipped,
                LyteProgressTone.Skipped,
            ),
            session.setTones,
        )
    }

    @Test
    fun countsRelativeDateWithinLastWeekOnly() {
        val today = LocalDate(2026, Month.JULY, 10)
        val sessions = listOf(
            finishedSession("today", "Push Day", LocalDateTime(2026, Month.JULY, 10, 7, 0), durationMinutes = 40),
            finishedSession("yesterday", "Pull Day", LocalDateTime(2026, Month.JULY, 9, 21, 0), durationMinutes = 40),
            finishedSession("three", "Leg Day", LocalDateTime(2026, Month.JULY, 7, 12, 0), durationMinutes = 40),
            // Ровно неделя назад — уже за порогом: показываем обычную дату.
            finishedSession("week", "Push Day", LocalDateTime(2026, Month.JULY, 3, 12, 0), durationMinutes = 40),
        )

        val byId = sessions.toMonthGroups(TEST_TIME_ZONE, today).single().sessions.associateBy { it.id }

        assertEquals(0, byId.getValue("today").daysAgo)
        assertEquals(1, byId.getValue("yesterday").daysAgo)
        assertEquals(3, byId.getValue("three").daysAgo)
        assertNull(byId.getValue("week").daysAgo)
    }

    @Test
    fun hasNoRelativeDateForSessionFinishedAfterToday() {
        // Часы съехали назад: «завтрашняя» сессия не должна превратиться в «-1 день назад».
        val sessions = listOf(
            finishedSession("future", "Push Day", LocalDateTime(2026, Month.JULY, 11, 12, 0), durationMinutes = 40),
        )

        val session = sessions
            .toMonthGroups(TEST_TIME_ZONE, LocalDate(2026, Month.JULY, 10))
            .single()
            .sessions
            .single()

        assertNull(session.daysAgo)
    }

    @Test
    fun emptyInputProducesEmptyList() {
        assertTrue(emptyList<WorkoutSessionItemEntity>().toMonthGroups(TEST_TIME_ZONE, TEST_TODAY).isEmpty())
    }
}
