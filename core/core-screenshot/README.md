# :core:core-screenshot

Общая политика скриншот-тестов: рендерит `@Preview` в PNG headless на JVM (Robolectric + Roborazzi),
без эмулятора и без macOS. Модуль **тестовый** — подключается только к `androidHostTest` и в
production-код не попадает.

Источник истины — сами `@Preview`. Модуль-потребитель не пишет тест на каждый экран: он объявляет
один параметризованный класс, и все превью его пакета снимаются автоматически, в светлой и тёмной
теме. Новое превью попадает под контроль без правок тестов.

## Публичный API

| Тип | Назначение |
|---|---|
| `LytePreviewScreenshotTest` | База теста: снимает один кейс. Наследуется модулем. |
| `lytePreviewCases(previewPackage)` | Находит все `@Preview` пакета и разворачивает в кейсы (по одному на тему). |
| `LytePreviewCase` | Один прогон: превью + тема. Знает путь эталона и имя в отчёте. |
| `LyteScreenshotTheme` | `LIGHT` / `DARK` — тема и её Robolectric-квалификатор. |
| `SCREENSHOT_SDK` | API уровня Robolectric (35): `android-all` под 36 не публикуется. |

## Подключение

Модулю достаточно convention-плагина — он сам добавит эту зависимость в `androidHostTest`:

```kotlin
plugins {
    // …остальные плагины модуля
    id("lyte.screenshot")
}

kotlin {
    androidLibrary {
        // Обязательно: без ресурсов Robolectric не найдёт шрифты и строки.
        withHostTest {
            isIncludeAndroidResources = true
        }
    }
}
```

И один тест-класс на модуль:

```kotlin
@RunWith(ParameterizedRobolectricTestRunner::class)
class WorkoutPreviewScreenshotTest(case: LytePreviewCase) : LytePreviewScreenshotTest(case) {

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun cases(): List<LytePreviewCase> =
            lytePreviewCases(previewPackage = "com.nikolaevskii.lyte.feature.workout")
    }
}
```

`previewPackage` — пакет **этого** модуля, а не корень `com.nikolaevskii.lyte`: в classpath теста
лежат и превью зависимостей (например, `core-design`), и без сужения фича сняла бы чужие компоненты
в свой каталог эталонов.

## Запуск

**Эталоны генерирует только CI** — джоба `screenshots` в `.github/workflows/ci.yml` на запинённом
`ubuntu-24.04`. Одна среда рендера означает отсутствие расхождений между машинами: на macOS
картинки отличаются субпиксельно. Пушишь код → CI перегенерирует эталоны, закоммитит их в ветку PR
и оставит комментарий со списком задетых экранов.

Локально команды нужны только чтобы посмотреть на результат своими глазами:

```bash
./gradlew :feature:workout:impl:recordRoborazziAndroidHostTest   # отрендерить и посмотреть
./gradlew :feature:workout:impl:verifyRoborazziAndroidHostTest   # сверить с эталонами
./gradlew :feature:workout:impl:compareRoborazziAndroidHostTest  # дифф-картинки
```

**Результат локального `record` не коммитим** — откатить перед коммитом:
`git checkout -- '*/screenshots/*'` (и удалить новые PNG, если появились). Иначе в репозиторий
уедут эталоны из чужой среды, мимо CI.

Эталоны лежат в `<module>/screenshots/*.png` — GitHub показывает их before/after прямо в diff'е PR.
Дифф-картинки `*_compare.png` (эталон / факт / дифф) появляются в `build/outputs/roborazzi/`.

## Что решено внутри

- **Бесконечные анимации.** `CircularProgressIndicator` и `rememberInfiniteTransition` не дают
  композиции дойти до idle — захват висит до `OutOfMemoryError`. Часы композиции останавливаются
  (`mainClock.autoAdvance = false`), кадр снимается в фиксированный момент.
- **Тёмная тема без правок превью.** `LyteTheme` читает тему через `isSystemInDarkTheme()`, поэтому
  тема переключается системным квалификатором Robolectric (`+night`).
- **Порог сравнения.** Субпиксельный шум не должен ронять verify, но реальные правки вёрстки ловятся.

## Дополнительные зависимости

Модуль сам тянет всё нужное (`api`): roborazzi, ComposablePreviewScanner, Robolectric, JUnit 4,
compose `ui-test-junit4` и `ui-test-manifest`. Отдельно подключать ничего не нужно.

`androidx.compose.ui:ui-test-manifest` добавляет `ComponentActivity` в манифест тестового APK —
без неё `createComposeRule()` падает с `Unable to resolve activity`.
