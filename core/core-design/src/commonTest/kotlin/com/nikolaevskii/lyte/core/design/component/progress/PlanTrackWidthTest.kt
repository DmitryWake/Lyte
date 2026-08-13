package com.nikolaevskii.lyte.core.design.component.progress

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Геометрию трека плана проверяем числами, а не только скриншотами: глазом не отличить сегмент
 * на границе порога от сегмента чуть за ним, а именно эта граница решает, читается трек пилюлями
 * или рассыпается в точки.
 *
 * Значения — те, с которыми `LyteExerciseCard` вызывает хелпер.
 */
class PlanTrackWidthTest {

    @Test
    fun smallPlanKeepsSegmentAtMaximumInsteadOfStretchingToSlot() {
        // Три подхода: слот дал бы 22.7dp на сегмент, но максимум — 16dp, поэтому трек занимает
        // 16*3 + 4*2 = 56dp, ровно как до расширения слота.
        assertEquals(56.dp, planTrackWidth(setCount = 3))
        assertEquals(16.dp, planTrackWidth(setCount = 1))
    }

    @Test
    fun crowdedPlanFillsWholeSlot() {
        // От четырёх подходов сегмент уже меньше максимума, и трек забирает слот целиком.
        assertEquals(SlotWidth, planTrackWidth(setCount = 4))
        assertEquals(SlotWidth, planTrackWidth(setCount = 6))
    }

    @Test
    fun densestDrawablePlanKeepsPillProportion() {
        // Шесть подходов — предел слота: сегмент 9.33dp против высоты 5dp, пилюля ещё читается.
        // Ради этого случая слот и расширяли: в 56dp здесь был сегмент 6dp, то есть круг.
        val width = planTrackWidth(setCount = 6) ?: error("трек при шести подходах обязан рисоваться")
        val segment = (width - SegmentGap * 5) / 6
        assertTrue(
            segment >= MinSegmentWidth,
            "сегмент $segment должен быть не тоньше порога $MinSegmentWidth",
        )
    }

    @Test
    fun tooDensePlanIsNotDrawn() {
        // Семь пилюль в слот не влезают ни при каком зазоре — лучше показать одну подпись,
        // чем ряд точек рядом с карточками, где сегменты ещё пилюли.
        assertNull(planTrackWidth(setCount = 7))
        assertNull(planTrackWidth(setCount = 8))
        assertNull(planTrackWidth(setCount = 20))
    }

    @Test
    fun emptyPlanIsNotDrawn() {
        assertNull(planTrackWidth(setCount = 0))
        assertNull(planTrackWidth(setCount = -1))
    }

    private fun planTrackWidth(setCount: Int) = lytePlanTrackWidth(
        setCount = setCount,
        slotWidth = SlotWidth,
        minSegmentWidth = MinSegmentWidth,
        maxSegmentWidth = MaxSegmentWidth,
        segmentGap = SegmentGap,
    )

    private companion object {
        val SlotWidth = 76.dp
        val MinSegmentWidth = 9.dp
        val MaxSegmentWidth = 16.dp
        val SegmentGap = 4.dp
    }
}
