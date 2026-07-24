import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Версия приложения — единственный источник, правила бампа в CLAUDE.md → Версионирование.
val versionProperties = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}

// Подпись релиза — из keystore.properties (в .gitignore). Ни keystore, ни пароли в репозиторий не
// коммитятся. Без этого файла релизные задачи падают (проверка внизу), чтобы debug-подписанная
// сборка не уехала в стор незамеченной.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val hasReleaseSigning: Boolean = keystoreProperties.isNotEmpty()

val lyteVersionName: String = requireNotNull(versionProperties.getProperty("lyte.versionName")) {
    "version.properties: не задан lyte.versionName"
}

val lyteVersionCode: Int = requireNotNull(versionProperties.getProperty("lyte.versionCode")) {
    "version.properties: не задан lyte.versionCode"
}.toInt()

fun keystoreSecret(key: String): String = requireNotNull(keystoreProperties.getProperty(key)) {
    "keystore.properties: не задан ключ $key"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.nikolaevskii.lyte"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.nikolaevskii.lyte"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = lyteVersionCode
        versionName = lyteVersionName
    }
    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreSecret("storeFile"))
                storePassword = keystoreSecret("storePassword")
                keyAlias = keystoreSecret("keyAlias")
                keyPassword = keystoreSecret("keyPassword")
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Fail-fast: без keystore.properties подписать релиз нечем. Проверка висит на задаче, а не на
// конфигурации, иначе сломался бы и assembleDebug.
//
// Флаг копируется в локальную переменную намеренно: top-level `val` в .gradle.kts — это поле объекта
// build-скрипта, и лямбда doFirst захватила бы ссылку на сам скрипт, которую configuration cache
// сериализовать не умеет. Локальная копия делает захват обычным Boolean.
run {
    val signingConfigured = hasReleaseSigning
    tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
        doFirst {
            check(signingConfigured) {
                "keystore.properties не найден в корне проекта — релиз подписать нечем. " +
                    "См. README → «Подпись релиза»."
            }
        }
    }
}
