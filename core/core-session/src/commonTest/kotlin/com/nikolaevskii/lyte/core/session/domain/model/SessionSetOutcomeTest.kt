package com.nikolaevskii.lyte.core.session.domain.model

import com.nikolaevskii.lyte.core.session.completed
import com.nikolaevskii.lyte.core.session.domain.util.hasWeight
import com.nikolaevskii.lyte.core.session.domain.util.outcome
import com.nikolaevskii.lyte.core.session.sessionSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SessionSetOutcomeTest {

    @Test
    fun pendingSetHasNoOutcome() {
        assertNull(sessionSet(id = "s", targetCount = 10, targetWeight = 60.0).outcome())
    }

    @Test
    fun skippedSetIsSkipped() {
        val set = sessionSet(id = "s", targetCount = 10, targetWeight = 60.0, result = SessionSetResultEntity.Skipped)

        assertEquals(SessionSetOutcomeEntity.SKIPPED, set.outcome())
    }

    @Test
    fun exactMatchIsMet() {
        val set = sessionSet(id = "s", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0))

        assertEquals(SessionSetOutcomeEntity.MET, set.outcome())
    }

    @Test
    fun moreRepsSameWeightIsExceeded() {
        val set = sessionSet(id = "s", targetCount = 10, targetWeight = 60.0, result = completed(count = 11, weight = 60.0))

        assertEquals(SessionSetOutcomeEntity.EXCEEDED, set.outcome())
    }

    @Test
    fun moreWeightSameRepsIsExceeded() {
        val set = sessionSet(id = "s", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 62.5))

        assertEquals(SessionSetOutcomeEntity.EXCEEDED, set.outcome())
    }

    @Test
    fun fewerRepsIsMissedEvenWhenWeightHigher() {
        // Любой параметр ниже цели — промах, даже если другой выше.
        val set = sessionSet(id = "s", targetCount = 10, targetWeight = 60.0, result = completed(count = 9, weight = 65.0))

        assertEquals(SessionSetOutcomeEntity.MISSED, set.outcome())
    }

    @Test
    fun lowerWeightIsMissed() {
        val set = sessionSet(id = "s", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 57.5))

        assertEquals(SessionSetOutcomeEntity.MISSED, set.outcome())
    }

    @Test
    fun weightIgnoredForBodyweightTarget() {
        // Цель без веса (bodyweight): сравниваем только повторения, любой факт-вес игнорируется.
        val met = sessionSet(id = "s", targetCount = 12, targetWeight = null, result = completed(count = 12, weight = null))
        val exceeded = sessionSet(id = "s", targetCount = 12, targetWeight = null, result = completed(count = 15, weight = null))
        val missed = sessionSet(id = "s", targetCount = 12, targetWeight = null, result = completed(count = 10, weight = null))

        assertEquals(SessionSetOutcomeEntity.MET, met.outcome())
        assertEquals(SessionSetOutcomeEntity.EXCEEDED, exceeded.outcome())
        assertEquals(SessionSetOutcomeEntity.MISSED, missed.outcome())
    }

    @Test
    fun zeroWeightTargetTreatedAsBodyweight() {
        // Конвенция домена: вес 0 — тоже bodyweight, в сравнении не участвует.
        val zeroTarget = SessionSetValueEntity(count = 12, weight = 0.0)

        assertFalse(zeroTarget.hasWeight)
    }

    @Test
    fun positiveWeightHasWeight() {
        assertTrue(SessionSetValueEntity(count = 10, weight = 60.0).hasWeight)
    }
}
