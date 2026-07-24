package com.nikolaevskii.lyte.core.screenshot

import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner

/**
 * Находит все `@Preview` в [previewPackage] и разворачивает их в кейсы — по одному на каждую тему.
 *
 * Пакет задаётся точечно (пакет самого модуля), а не корнем `com.nikolaevskii.lyte`: в classpath
 * теста лежат и превью зависимостей (например, `core-design`), и без сужения фича сняла бы чужие
 * компоненты в свой каталог эталонов.
 *
 * `includePrivatePreviews()` обязателен: почти все превью в проекте объявлены `private`.
 *
 * Пример использования в модуле:
 * ```
 * @RunWith(ParameterizedRobolectricTestRunner::class)
 * class HistoryPreviewScreenshotTest(case: LytePreviewCase) : LytePreviewScreenshotTest(case) {
 *     companion object {
 *         @JvmStatic
 *         @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
 *         fun cases(): List<LytePreviewCase> =
 *             lytePreviewCases("com.nikolaevskii.lyte.feature.history")
 *     }
 * }
 * ```
 */
fun lytePreviewCases(previewPackage: String): List<LytePreviewCase> =
    AndroidComposablePreviewScanner()
        .scanPackageTrees(previewPackage)
        .includePrivatePreviews()
        .getPreviews()
        .flatMap { preview ->
            LyteScreenshotTheme.entries.map { theme ->
                LytePreviewCase(preview = preview, theme = theme)
            }
        }
