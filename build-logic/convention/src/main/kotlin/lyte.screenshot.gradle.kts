/**
 * Скриншот-тесты модуля: подключает roborazzi и общую политику съёмки из `:core:core-screenshot`.
 *
 * Модулю остаётся объявить `id("lyte.screenshot")`, включить ресурсы в host-тестах
 * (`withHostTest { isIncludeAndroidResources = true }`) и добавить тест-класс со списком превью
 * (см. `lytePreviewCases`).
 */

plugins {
    id("io.github.takahirom.roborazzi")
}

// Лениво: конфигурация androidHostTest* появляется только после того, как KMP-плагин создаст
// source set'ы, то есть позже применения этого плагина.
configurations.matching { configuration -> configuration.name == "androidHostTestImplementation" }
    .configureEach {
        dependencies.add(project.dependencies.create(project(":core:core-screenshot")))
    }

tasks.withType<Test>().configureEach {
    // Compose-рендер прожорливее обычных unit-тестов.
    maxHeapSize = "2g"
    // Без hardware-режима PixelCopy roborazzi предупреждает о битых картинках.
    systemProperty("robolectric.pixelCopyRenderMode", "hardware")

    // Режим roborazzi выводим из запрошенной задачи: под KMP-таск `testAndroidHostTest` плагин
    // сам его не проставляет, а `-D` не долетает до форкнутой тест-JVM (config cache + форк).
    val requestedTasks = gradle.startParameter.taskNames
    fun requested(mode: String): String =
        requestedTasks.any { it.contains(other = mode, ignoreCase = true) }.toString()

    systemProperty("roborazzi.test.record", requested("record"))
    systemProperty("roborazzi.test.verify", requested("verify"))
}
