import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // Только Android: модуль обслуживает host-тесты (Robolectric), на iOS не собирается.
    androidLibrary {
        namespace = "com.nikolaevskii.lyte.core.screenshot"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        androidMain.dependencies {
            // api, а не implementation: потребители пишут свои тест-классы поверх этих типов
            // (ParameterizedRobolectricTestRunner, ComposablePreview и т.д.).
            api(libs.roborazzi)
            api(libs.roborazzi.compose)
            api(libs.composablePreviewScanner.android)
            api(libs.compose.uiTestJunit4)
            api(libs.androidx.compose.uiTestManifest)
            api(libs.robolectric)
            api(libs.junit)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
    }
}
