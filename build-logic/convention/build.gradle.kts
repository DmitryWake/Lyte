plugins {
    `kotlin-dsl`
}

group = "com.nikolaevskii.lyte.buildlogic"

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    // implementation, а не compileOnly: precompiled-скрипт применяет roborazzi через plugins {},
    // поэтому его marker нужен на classpath для генерации типобезопасных аксессоров.
    implementation(libs.roborazzi.gradlePlugin)
}
