package com.nikolaevskii.lyte.core.screenshot

import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Один прогон скриншот-теста: конкретное `@Preview` в конкретной теме. Экземпляры создаёт
 * [lytePreviewCases], тест-классы фич их только принимают.
 */
data class LytePreviewCase(
    val preview: ComposablePreview<AndroidPreviewInfo>,
    val theme: LyteScreenshotTheme,
) {

    /** Путь эталона относительно корня модуля — эти PNG коммитятся и видны в diff'е PR. */
    val filePath: String
        get() = "$SCREENSHOTS_DIR/${preview.declaringClass.substringAfterLast(PACKAGE_SEPARATOR)}." +
            "${preview.methodName}_${theme.fileSuffix}.png"

    /** Имя прогона в отчёте JUnit (подставляется в `@Parameters(name = "{0}")`). */
    override fun toString(): String =
        "${preview.methodName}[${theme.fileSuffix}]"

    private companion object {
        const val SCREENSHOTS_DIR = "screenshots"
        const val PACKAGE_SEPARATOR = '.'
    }
}
