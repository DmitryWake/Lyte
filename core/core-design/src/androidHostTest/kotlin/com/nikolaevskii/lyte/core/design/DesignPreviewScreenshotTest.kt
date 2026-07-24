package com.nikolaevskii.lyte.core.design

import com.nikolaevskii.lyte.core.screenshot.LytePreviewCase
import com.nikolaevskii.lyte.core.screenshot.LytePreviewScreenshotTest
import com.nikolaevskii.lyte.core.screenshot.lytePreviewCases
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

/**
 * Скриншоты всех `@Preview` дизайн-системы. Новый компонент попадает под контроль автоматически —
 * править этот файл не нужно.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
class DesignPreviewScreenshotTest(case: LytePreviewCase) : LytePreviewScreenshotTest(case) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun cases(): List<LytePreviewCase> =
            lytePreviewCases(previewPackage = "com.nikolaevskii.lyte.core.design")
    }
}
