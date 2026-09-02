package com.nikolaevskii.lyte.core.design.format

import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Единицы подставляются теми же словами, что лежат в ресурсах модуля, но своими константами: тест
 * проверяет склейку, а не словарь, и падать должен от смены разделителя или пробела.
 */
private const val REPS_UNIT = "повт"
private const val WEIGHT_UNIT = "кг"

/** Ожидаемый неразрывный пробел записан явно — иначе в тексте теста его не отличить от обычного. */
private const val NBSP = "\u00A0"

class SetValueFormatTest {

    @Test
    fun compactGluesRepsToWeight() {
        assertEquals(
            "10×62,5${NBSP}кг",
            label(LyteSetValue(reps = 10, weight = 62.5), LyteSetValueFormat.Compact),
        )
    }

    @Test
    fun expandedNamesBothUnits() {
        assertEquals(
            "10${NBSP}повт × 62,5${NBSP}кг",
            label(LyteSetValue(reps = 10, weight = 62.5), LyteSetValueFormat.Expanded),
        )
    }

    @Test
    fun bodyweightHasOneFormInBothModes() {
        val value = LyteSetValue(reps = 12)

        assertEquals("12${NBSP}повт", label(value, LyteSetValueFormat.Compact))
        assertEquals("12${NBSP}повт", label(value, LyteSetValueFormat.Expanded))
    }

    /** «Веса нет» и «вес 0» показываются одинаково: «12×0 кг» — не значение, а мусор. */
    @Test
    fun zeroWeightIsTreatedAsBodyweight() {
        assertEquals(
            label(LyteSetValue(reps = 12), LyteSetValueFormat.Compact),
            label(LyteSetValue(reps = 12, weight = 0.0), LyteSetValueFormat.Compact),
        )
    }

    @Test
    fun unitIsNeverSeparatedFromItsNumberByABreakableSpace() {
        val printed = allForms()

        assertFalse(printed.any { text -> text.contains(" $WEIGHT_UNIT") }, printed.toString())
        assertFalse(printed.any { text -> text.contains(" $REPS_UNIT") }, printed.toString())
        assertTrue(printed.all { text -> text.contains(NBSP) }, printed.toString())
    }

    @Test
    fun weightIsPrintedWithACommaInBothModes() {
        val printed = LyteSetValueFormat.entries.map { format ->
            label(LyteSetValue(reps = 10, weight = 62.5), format)
        }

        assertTrue(printed.all { text -> text.contains("62,5") }, printed.toString())
        assertFalse(printed.any { text -> text.contains('.') }, printed.toString())
    }

    /**
     * Подпись над степпером и сам степпер обязаны показать одно число: до RD-20 подпись печатала
     * «62.4999», а степпер под ней — «62.5», потому что округляли они в разных местах.
     */
    @Test
    fun labelShowsExactlyWhatTheStepperShows() {
        val typed = 62.4999

        LyteSetValueFormat.entries.forEach { format ->
            val printed = label(LyteSetValue(reps = 10, weight = typed), format)

            assertTrue(printed.contains(formatWeight(typed)), "$format: $printed")
            assertEquals(label(LyteSetValue(reps = 10, weight = 62.5), format), printed)
        }
    }

    private fun allForms(): List<String> = LyteSetValueFormat.entries.flatMap { format ->
        listOf(
            label(LyteSetValue(reps = 10, weight = 62.5), format),
            label(LyteSetValue(reps = 10), format),
        )
    }

    private fun label(value: LyteSetValue, format: LyteSetValueFormat): String = setValueLabel(
        value = value,
        format = format,
        repsUnit = REPS_UNIT,
        weightUnit = WEIGHT_UNIT,
    )
}
