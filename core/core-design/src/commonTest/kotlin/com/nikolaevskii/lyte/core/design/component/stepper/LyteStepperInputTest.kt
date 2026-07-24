package com.nikolaevskii.lyte.core.design.component.stepper

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LyteStepperInputTest {

    @Test
    fun emptyInputIsAccepted() {
        assertTrue(isStepperInputAccepted(text = "", allowDecimal = true))
        assertTrue(isStepperInputAccepted(text = "", allowDecimal = false))
    }

    @Test
    fun decimalMode_acceptsValidNumbers() {
        assertTrue(isStepperInputAccepted(text = "62", allowDecimal = true))
        assertTrue(isStepperInputAccepted(text = "62.5", allowDecimal = true))
        assertTrue(isStepperInputAccepted(text = "62,5", allowDecimal = true))
        assertTrue(isStepperInputAccepted(text = "12345", allowDecimal = true))
        // промежуточный ввод: разделитель без дробной части
        assertTrue(isStepperInputAccepted(text = "12.", allowDecimal = true))
    }

    @Test
    fun decimalMode_rejectsTooManyDecimals() {
        assertFalse(isStepperInputAccepted(text = "62.555", allowDecimal = true))
    }

    @Test
    fun rejectsTooManyIntegerDigits() {
        assertFalse(isStepperInputAccepted(text = "123456", allowDecimal = true))
        assertFalse(isStepperInputAccepted(text = "123456", allowDecimal = false))
    }

    @Test
    fun rejectsMalformedInput() {
        assertFalse(isStepperInputAccepted(text = "1.2.3", allowDecimal = true))
        assertFalse(isStepperInputAccepted(text = "abc", allowDecimal = true))
        assertFalse(isStepperInputAccepted(text = "-5", allowDecimal = true))
    }

    @Test
    fun integerMode_rejectsDecimals() {
        assertTrue(isStepperInputAccepted(text = "12", allowDecimal = false))
        assertFalse(isStepperInputAccepted(text = "12.5", allowDecimal = false))
        assertFalse(isStepperInputAccepted(text = "12,5", allowDecimal = false))
    }
}
