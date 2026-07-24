package com.nikolaevskii.lyte.core.screenshot

/**
 * Тема, в которой снимается превью. Переключается системным квалификатором Robolectric, потому
 * что `LyteTheme` читает тему через `isSystemInDarkTheme()` — так снимок проходит тот же путь,
 * что и реальное приложение, и превью править не нужно.
 */
enum class LyteScreenshotTheme(val qualifier: String, val fileSuffix: String) {
    LIGHT(qualifier = "+notnight", fileSuffix = "light"),
    DARK(qualifier = "+night", fileSuffix = "dark"),
}
