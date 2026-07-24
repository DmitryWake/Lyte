package com.nikolaevskii.lyte.core.screenshot

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Robolectric не публикует `android-all` под API 36, поэтому тесты идут на 35 при `compileSdk = 36`.
 */
const val SCREENSHOT_SDK: Int = 35

/**
 * Момент, на котором снимается кадр. Ненулевой, чтобы входные анимации успели прийти в конечное
 * состояние, и при этом фиксированный — снимок детерминирован.
 */
private const val STABLE_FRAME_MILLIS: Long = 1_000L

/** Допустимая доля изменившихся пикселей: гасит субпиксельный шум, но ловит реальные правки вёрстки. */
private const val CHANGE_THRESHOLD: Float = 0.001F

/**
 * База скриншот-тестов: снимает одно `@Preview` в одной теме. Модуль-потребитель наследует её
 * и отдаёт список кейсов через [lytePreviewCases] — по превью-тесту писать не нужно.
 *
 * Здесь же централизованы три правила съёмки:
 * 1. **Часы композиции остановлены** (`autoAdvance = false`) — иначе бесконечные анимации
 *    (`CircularProgressIndicator`, `rememberInfiniteTransition` в splash) не дают дойти до idle,
 *    и захват висит до OutOfMemoryError. Кадр берётся в фиксированный момент времени.
 * 2. **Тема задаётся системным квалификатором**, а не правкой превью (см. [LyteScreenshotTheme]).
 * 3. **Единый порог сравнения** — субпиксельный шум рендера не должен ронять verify.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SCREENSHOT_SDK], qualifiers = RobolectricDeviceQualifiers.Pixel5)
abstract class LytePreviewScreenshotTest(private val case: LytePreviewCase) {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    @Test
    fun capturePreview() {
        RuntimeEnvironment.setQualifiers(case.theme.qualifier)

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            case.preview()
        }
        composeTestRule.mainClock.advanceTimeBy(STABLE_FRAME_MILLIS)

        composeTestRule.onRoot().captureRoboImage(
            filePath = case.filePath,
            // Опции создаются здесь, а не в companion: статический инициализатор отработал бы
            // до старта Robolectric-окружения и упал бы NPE в ConfigurationRegistry — тест-класс
            // тогда не доходит даже до сбора параметров.
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = CHANGE_THRESHOLD),
            ),
        )
    }
}
