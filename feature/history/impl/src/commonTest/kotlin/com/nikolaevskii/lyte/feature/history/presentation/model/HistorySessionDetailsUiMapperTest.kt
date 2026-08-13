package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.feature.history.TEST_TIME_ZONE
import com.nikolaevskii.lyte.feature.history.completed
import com.nikolaevskii.lyte.feature.history.finishedSessionEntity
import com.nikolaevskii.lyte.feature.history.sessionExercise
import com.nikolaevskii.lyte.feature.history.sessionSet
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HistorySessionDetailsUiMapperTest {

    private val session = finishedSessionEntity(
        id = "session-1",
        programName = "Push Day",
        startedAt = LocalDateTime(2026, Month.JULY, 2, 18, 24),
        durationMinutes = 52,
        exercises = listOf(
            sessionExercise(
                id = "e1",
                name = "Жим лёжа",
                sets = listOf(
                    sessionSet("s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 8, weight = 80.0)),
                    sessionSet("s2", targetCount = 8, targetWeight = 82.5, result = completed(count = 9, weight = 82.5)),
                    sessionSet("s3", targetCount = 8, targetWeight = 80.0, result = completed(count = 6, weight = 80.0), note = "тяжело"),
                    sessionSet("s4", targetCount = 8, targetWeight = 80.0, result = SessionSetResultEntity.Skipped),
                ),
            ),
            sessionExercise(
                id = "e2",
                name = "Отжимания на брусьях",
                sets = listOf(
                    sessionSet("s5", targetCount = 12, targetWeight = null, result = completed(count = 12, weight = null)),
                ),
            ),
        ),
    )

    @Test
    fun mapsHeaderMetaFromStartAndCounts() {
        val details = session.toDetailsUiModel(TEST_TIME_ZONE)

        assertEquals("Push Day", details.programName)
        assertEquals(2026, details.year)
        assertEquals(7, details.monthNumber)
        assertEquals(2, details.dayOfMonth)
        assertEquals(18, details.startHour)
        assertEquals(24, details.startMinute)
        assertEquals(52, details.durationMinutes)
        // Completed: s1, s2, s3, s5 — четыре; s4 пропущен. Всего пять.
        assertEquals(4, details.completedSetCount)
        assertEquals(5, details.totalSetCount)
    }

    @Test
    fun mapsExerciseGroupsInOrder() {
        val details = session.toDetailsUiModel(TEST_TIME_ZONE)

        assertEquals(listOf("Жим лёжа", "Отжимания на брусьях"), details.exercises.map { it.exerciseName })
        assertEquals(listOf("s1", "s2", "s3", "s4"), details.exercises[0].rows.map { it.id })
        assertEquals(listOf(1, 2, 3, 4), details.exercises[0].rows.map { it.index })
    }

    @Test
    fun mapsTonesFromOutcome() {
        val rows = session.toDetailsUiModel(TEST_TIME_ZONE).exercises[0].rows

        assertEquals(LyteProgressTone.Met, rows[0].tone)
        assertEquals(LyteProgressTone.Positive, rows[1].tone)
        assertEquals(LyteProgressTone.Negative, rows[2].tone)
        assertEquals(LyteProgressTone.Skipped, rows[3].tone)
    }

    @Test
    fun mapsWeightedValues() {
        val rows = session.toDetailsUiModel(TEST_TIME_ZONE).exercises[0].rows

        assertEquals(LyteSetValue(reps = 8, weight = 80.0), rows[0].target)
        assertEquals(LyteSetValue(reps = 8, weight = 80.0), rows[0].actual)
        assertEquals(LyteSetValue(reps = 8, weight = 82.5), rows[1].target)
        assertEquals(LyteSetValue(reps = 9, weight = 82.5), rows[1].actual)
    }

    /** У упражнения своего веса вес именно отсутствует, а не равен нулю — иначе строка диффа
     * показала бы «12×0 кг». */
    @Test
    fun mapsBodyweightValuesWithoutWeight() {
        val row = session.toDetailsUiModel(TEST_TIME_ZONE).exercises[1].rows.single()

        assertEquals(LyteSetValue(reps = 12, weight = null), row.target)
        assertEquals(LyteSetValue(reps = 12, weight = null), row.actual)
    }

    @Test
    fun mapsNoteAndNullActualForSkipped() {
        val rows = session.toDetailsUiModel(TEST_TIME_ZONE).exercises[0].rows

        assertNull(rows[0].note)
        assertEquals("тяжело", rows[2].note)
        assertNull(rows[3].actual)
    }
}
