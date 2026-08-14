package com.nikolaevskii.lyte.core.design.component.feedback

import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LyteDiffDeltaTest {

    @Test
    fun exactHitHasNothingToReport() {
        val target = LyteSetValue(reps = 10, weight = 60.0)
        assertNull(lyteDiffDelta(target = target, actual = LyteSetValue(reps = 10, weight = 60.0)))
    }

    @Test
    fun reportsBothDirections() {
        val target = LyteSetValue(reps = 10, weight = 60.0)
        assertEquals(
            LyteDiffDelta(reps = 2, weight = 2.5),
            lyteDiffDelta(target = target, actual = LyteSetValue(reps = 12, weight = 62.5)),
        )
        assertEquals(
            LyteDiffDelta(reps = -2, weight = -2.5),
            lyteDiffDelta(target = target, actual = LyteSetValue(reps = 8, weight = 57.5)),
        )
    }

    @Test
    fun reportsSingleAxisChange() {
        val target = LyteSetValue(reps = 10, weight = 60.0)
        assertEquals(
            LyteDiffDelta(reps = 0, weight = 5.0),
            lyteDiffDelta(target = target, actual = LyteSetValue(reps = 10, weight = 65.0)),
        )
        assertEquals(
            LyteDiffDelta(reps = 3, weight = 0.0),
            lyteDiffDelta(target = target, actual = LyteSetValue(reps = 13, weight = 60.0)),
        )
    }

    @Test
    fun bodyweightComparesRepsOnly() {
        assertEquals(
            LyteDiffDelta(reps = 3, weight = 0.0),
            lyteDiffDelta(target = LyteSetValue(reps = 12), actual = LyteSetValue(reps = 15)),
        )
        assertNull(lyteDiffDelta(target = LyteSetValue(reps = 12), actual = LyteSetValue(reps = 12)))
    }

    /** Вес есть только у одной стороны — сравнивать нечего, «потери 60 кг» не было. */
    @Test
    fun ignoresWeightWhenOnlyOneSideHasIt() {
        assertNull(
            lyteDiffDelta(target = LyteSetValue(reps = 10, weight = 60.0), actual = LyteSetValue(reps = 10)),
        )
        assertNull(
            lyteDiffDelta(target = LyteSetValue(reps = 10), actual = LyteSetValue(reps = 10, weight = 60.0)),
        )
    }

    @Test
    fun missingSideHasNoDelta() {
        val value = LyteSetValue(reps = 10, weight = 60.0)
        assertNull(lyteDiffDelta(target = value, actual = null))
        assertNull(lyteDiffDelta(target = null, actual = value))
        assertNull(lyteDiffDelta(target = null, actual = null))
    }

    /** Разность double'ов даёт хвосты — дельта обязана оставаться шагом веса, а не «2.4999…». */
    @Test
    fun roundsFloatingPointNoise() {
        val delta = lyteDiffDelta(
            target = LyteSetValue(reps = 10, weight = 57.7),
            actual = LyteSetValue(reps = 10, weight = 60.2),
        )
        assertEquals(2.5, delta?.weight)
    }
}
