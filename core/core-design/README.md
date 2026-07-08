# core-design

Дизайн-система Lyte: токены (Material 3 tonal-цвета light/dark, типографика на Space Grotesk с
отдельной табличной числовой шкалой, 4px spacing grid, формы, elevation), курируемый набор иконок
и компонент-кит поверх M3-примитивов. Больше не заглушка — полноценная реализация.

## Что внутри

### Тема

- `LyteTheme(darkTheme = isSystemInDarkTheme(), content)` — точка входа. Настраивает
  `MaterialTheme.colorScheme/typography/shapes` под токены Lyte и прокидывает расширенные токены
  через `CompositionLocal`.
- `LyteTheme` (аксессор-object, по аналогии с `MaterialTheme`) — доступ к тому, чего нет в M3:
  - `LyteTheme.extendedColors` — `success`/`onSuccess`/`successContainer`/`onSuccessContainer`,
    `diffPositive/Bg`, `diffNegative/Bg`, `diffNeutral/Bg`, `diffSkipped/Bg`, `diffMet/Bg`
    (попал точно в цель — мягкий зелёный, отличен от «превысил»), `aiAccent`, `aiAccentContainer`.
  - `LyteTheme.spacing` — 4px-сетка: `s0, s1, s2, s3, s4, s5, s6, s8, s10, s12, s16, s20, s24` (`Dp`).
  - `LyteTheme.elevation` — `level1`…`level5` (`Dp`, для `shadowElevation`/`Modifier.shadow`).
  - `LyteTheme.numericTypography` — `hero`/`large`/`medium`: табличные (`tnum`) стили для «живых»
    чисел (секундомер, степпер, счётчики). Обычный текст всегда через `MaterialTheme.typography`.
  - `LyteTheme.extendedShapes` — `largeIncreased` (20dp), `extraLargeIncreased` (32dp), `full` (pill).
- `MaterialTheme.colorScheme` / `.typography` / `.shapes` — уже настроены; отдельно доставать токены
  Lyte для базовых M3-ролей не нужно, они и есть источник правды после `LyteTheme { … }`.

### Шрифты

**Space Grotesk** (400/500/600/700 — весь UI-текст и цифры) и **Inter Tight** (700 — только
вордмарк «Lyte», через `lyteWordmarkFontFamily()`). Оба — OFL, забандлены в `composeResources/font/`.
`lyteFontFamily()` / `lyteTypography()` / `lyteNumericTypography()` — `@Composable`-билдеры; обычно
не нужны напрямую, уже прошиты в `LyteTheme`.

### Иконки

`LyteIcons` — небольшой осознанно ограниченный словарь (см. `ICONOGRAPHY.md` дизайн-системы) поверх
`com.composables:icons-lucide-cmp` (KMP-порт Lucide для Compose Multiplatform: Android/iOS/JVM/JS/Wasm).
Значения — обычный `ImageVector`; зависимость от библиотеки Lucide — `implementation`-only внутри
`core-design`, подключать её отдельно потребителям не нужно.

Доступные иконки: `Dumbbell`, `ClipboardList`, `History`, `Plus`, `Minus`, `Check`, `Close`,
`ChevronRight`, `ChevronDown`, `ChevronLeft`, `GripVertical`, `OverflowMenu`, `Search`, `Sparkles`
(зарезервирована для ИИ-поверхностей — не использовать как обычную иконку), `AddNote`,
`SkipForward`, `AddCircle`, `Delete`, `Edit`, `ListChecks`, `Circle`, `CircleDot`, `CircleCheck`,
`CircleX`, `CircleMinus` (кружки-статусы подходов).

### Компоненты

Каждый — стейтлес `@Composable` на M3-примитивах (где дизайн позволяет), с собственным `@Preview`.
Доменный текст (заголовки карточек, сводки подходов) — всегда параметр вызывающей стороны;
компонентный «хром» (см. «Строки» ниже) — внутри модуля.

| Пакет | Компоненты |
|---|---|
| `component.button` | `LyteButton` (variant Filled/Tonal/Outlined/Text × accent Primary/Secondary/Tertiary/Error × size Large/Medium/Small) |
| `component.iconbutton` | `LyteIconButton` |
| `component.chip` | `LyteChip` |
| `component.badge` | `LyteBadge` (size Small — счётчик, Medium — табличная stat-пилюля) |
| `component.switch` | `LyteSwitch` |
| `component.textfield` | `LyteTextField` |
| `component.overline` | `LyteOverline` (микро-заголовок капсом) |
| `component.stepper` | `LyteStepper` (± контрол + ручной tap-to-edit ввод; `allowDecimal=false` — целочисленный режим для повторов, `fillMaxWidth` — для колонок) |
| `component.card` | `LyteProgramCard` (+`trailing`), `LyteExerciseCard` (`setLabels`-пилюли + edit/remove), `LyteSessionCard`, `LyteListRow` |
| `component.feedback` | `LyteDiffRow` (тона Met/Positive/Negative/Neutral/Skipped), `LyteDialog`, `LyteEmptyState` |
| `component.navigation` | `LyteTopBar` (size Small/Large), `LyteBottomNavigationBar` |
| `component.overlay` | `LyteBottomSheet`, `LyteRestTimerOverlay` |
| `component.datadisplay` | `LyteSessionStopwatch` |
| `component.session` | `LyteSetDots`, `LyteSetOverview`, `LyteTrackSetRow`, `LyteExerciseStrip` (экран активной сессии) |

`LyteDialog` / `LyteBottomSheet` / `LyteRestTimerOverlay` не принимают флаг видимости — видимостью
управляет вызывающая сторона самим фактом композиции (`if (showDialog) { LyteDialog(...) }`), как
принято в M3 (`AlertDialog`, `ModalBottomSheet` не имеют параметра `visible`).

## Использование

```kotlin
@Composable
fun App() {
    LyteTheme {
        // всё дерево UI приложения
    }
}
```

```kotlin
// доступ к расширенным токенам — как у MaterialTheme
Box(modifier = Modifier.padding(LyteTheme.spacing.s4)) {
    LyteButton(
        text = "Начать тренировку",
        onClick = onStartClick,
        icon = LyteIcons.Dumbbell,
    )
}
```

Корень **каждого** экрана оборачивается в `LyteTheme` только один раз — в `App()` (`:shared`).

## Подключение

```kotlin
implementation(projects.core.coreDesign)
```

Модуль использует только `implementation(...)`-зависимости, ничего транзитивно не экспортирует.
Потребитель **обязан** подключить сам:

- `libs.compose.material3` — `MaterialTheme`, `Scaffold`, `TopAppBar` и остальные M3-компоненты.
- `libs.compose.runtime` — `@Composable`.
- `libs.compose.ui` / `libs.compose.foundation` — базовые модификаторы и layout-примитивы.

**Не нужно** подключать отдельно: `icons-lucide-cmp` (инкапсулирована за `LyteIcons`, возвращает
обычный `ImageVector`) и `compose.components.resources` (шрифты/строки уже зашиты в `LyteTheme`/
компоненты; напрямую `Res` этого модуля потребители не читают).

## Нюансы

- **Хардкод строк в `@Composable` запрещён** (см. корневой `CLAUDE.md`). Компонентный «хром»
  («Отдых», «повт», «кг», «Отмена», лейблы accessibility и т.п.) лежит в
  `composeResources/values/strings.xml` этого модуля. Доменный текст (название программы, сводка
  подходов) — параметр вызывающей стороны; core-design намеренно ничего не знает о доменной лексике
  (в т.ч. о русской плюрализации — «5 упражнений» собирает вызывающая фича, не компонент).
- **Backdrop-blur** плавающего `LyteBottomNavigationBar` из референса не воспроизведён — не
  переносится единообразно между Android/iOS в Compose Multiplatform; приближено полупрозрачной
  заливкой.
- **`LyteBadge` — не M3 `Badge`.** M3 `Badge` — точка-уведомление; `LyteBadge` — пилюля для
  метаданных (счётчики), поэтому реализована кастомно поверх `Surface`.
- **`androidLibrary { androidResources { enable = true } }` обязателен** для любого core/feature
  KMP-модуля, у которого есть свой `composeResources/` (как здесь). Без этого флага плагин
  `com.android.kotlin.multiplatform.library` не регистрирует Android-задачи упаковки ресурсов
  (`generateAndroidMainAssets`/`mergeAndroidMainAssets`) — модуль компилируется без ошибок, но
  `stringResource`/`Font` падают в рантайме и в `@Preview` с `MissingResourceException`, потому что
  скомпилированный `.cvr`-бандл и шрифты не попадают в AAR.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
