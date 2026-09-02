package com.nikolaevskii.lyte.core.design.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WeightFormatTest {

    @Test
    fun wholeWeightLosesFractionalPart() {
        assertEquals("60", formatWeight(60.0))
        assertEquals("0", formatWeight(0.0))
        assertEquals("100", formatWeight(100.0))
    }

    @Test
    fun fractionalWeightIsSeparatedByComma() {
        assertEquals("62,5", formatWeight(62.5))
        assertEquals("0,25", formatWeight(0.25))
    }

    @Test
    fun noWeightIsEverPrintedWithADot() {
        val printed = listOf(60.0, 62.5, 0.25, 137.75).map { weight -> formatWeight(weight) }

        assertFalse(printed.any { text -> text.contains('.') }, "точка как разделитель запрещена: $printed")
    }

    /**
     * Тот самый случай, на котором два форматтера расходились: подпись печатала «62.4999», а степпер
     * под ней — «62.5». Теперь округление одно, и оно живёт в форматтере, а не у вызывающего.
     */
    @Test
    fun weightIsRoundedToTheSamePrecisionTheStepperAccepts() {
        assertEquals("62,5", formatWeight(62.4999))
        assertEquals(formatWeight(62.5), formatWeight(62.4999))
        assertEquals("62,49", formatWeight(62.4949))
    }

    /** Что степпер сохранит после ввода и что покажет подпись — обязано печататься одинаково. */
    @Test
    fun storedValueAndPrintedValueAgree() {
        val typed = 62.4999

        assertEquals(formatWeight(typed), formatWeight(roundToWeightPrecision(typed)))
    }
}
