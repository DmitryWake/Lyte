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
  - `LyteTheme.accents` — шесть акцентов упражнения (`LyteAccent`: `Coral`, `Indigo`, `Lime`,
    `Amber`, `Teal`, `Slate`), каждый — пара `LyteAccentColors(fg, container)`: `container` заливает
    круг-маркер, `fg` рисует глиф. Доступ по значению — `LyteTheme.accents[accent]`.
    **`Slate` — дефолт** (`LyteAccent.Default`), упражнение без выбранного цвета выглядит осознанным.
    Акценты — **не группы мышц**: цвет это обычное свойство упражнения, его выбирает пользователь.
  - `LyteTheme.spacing` — 4px-сетка: `s0, s1, s2, s3, s4, s5, s6, s8, s10, s12, s16, s20, s24` (`Dp`).
  - `LyteTheme.elevation` — `level1`…`level5` (`Dp`, для `shadowElevation`/`Modifier.shadow`).
  - `LyteTheme.numericTypography` — `hero`/`large`/`medium`: табличные (`tnum`) стили для «живых»
    чисел (секундомер, степпер, счётчики). Обычный текст всегда через `MaterialTheme.typography`.
  - `LyteTheme.extendedShapes` — `largeIncreased` (20dp), `extraLargeIncreased` (32dp), `full` (pill).
  - `LyteTheme.motion` — токены движения: `durationShort/Medium/Long` (150/250/400, `Int` мс —
    ложатся прямо в `tween()`) и `easingStandard/Emphasized/Decelerate/Accelerate` (`Easing`).
    Анимации компонентов и экранов берут значения отсюда, а не подбирают свои: переходы быстрые
    и без пружин.
- `MaterialTheme.colorScheme` / `.typography` / `.shapes` — уже настроены; отдельно доставать токены
  Lyte для базовых M3-ролей не нужно, они и есть источник правды после `LyteTheme { … }`.

### Шрифты

**Space Grotesk** (400/500/600/700 — весь UI-текст и цифры) и **Inter Tight** (700 — только
вордмарк «Lyte», через `lyteWordmarkFontFamily()`). Оба — OFL, забандлены в `composeResources/font/`.
`lyteFontFamily()` / `lyteTypography()` / `lyteNumericTypography()` — `@Composable`-билдеры; обычно
не нужны напрямую, уже прошиты в `LyteTheme`.

### Иконки

`LyteIcons` — небольшой осознанно ограниченный словарь (см. `design/v2/_ds/<id>/readme.md`
§ Iconography) поверх `com.composables:icons-lucide-cmp` (KMP-порт Lucide для Compose
Multiplatform: Android/iOS/JVM/JS/Wasm). Значения — обычный `ImageVector`; зависимость от библиотеки
Lucide — `implementation`-only внутри `core-design`, подключать её отдельно потребителям не нужно.

Доступные иконки: `Dumbbell`, `ClipboardList`, `History`, `Play`, `Plus`, `Minus`, `Check`, `Close`,
`ChevronRight`, `ChevronLeft`, `GripVertical`, `OverflowMenu`, `Sparkles` (зарезервирована для
ИИ-поверхностей — не использовать как обычную иконку), `Delete`, `Edit`, `List` (шторка упражнений
сессии), `SearchX` (пустой результат поиска), `ListChecks`, `Circle`, `CircleDot`, `CircleCheck`,
`CircleArrowUp`, `CircleArrowDown` (превысил/недобрал цель), `CircleX`, `CircleMinus`
(кружки-статусы подходов).

Словарь держит только то, что реально рисуется. `CircleX` и `OverflowMenu` в дизайне v2 не
используются, но пока живы их call-site'ы: крестик уходит вместе с переписанным `LyteTrackSetRow`
(RD-09), кебаб-меню — вместе со списком программ (RD-13).

#### Пиктограммы движений

`LyteExerciseIcon(glyph, tint, size, contentDescription)` — десять знаков движения
(`LyteExerciseGlyph`: `Squat`, `Deadlift`, `BenchPress`, `PullUp`, `DumbbellPress`, `Curl`,
`Crunch`, `Stretch`, `Rack`, `Machine`; дефолт — `Squat`). Вместе с `LyteAccent` образуют
круг-маркер упражнения: цвет и знак — обычные свойства упражнения, ничего не выводится из данных.
Подпись движения («Присед», «Становая», …) — `lyteExerciseGlyphLabel(glyph)`; по умолчанию она же
идёт в `contentDescription`, внутри маркера рядом с названием упражнения передавайте `null`.

Это **растровый** набор (PNG в `composeResources/drawable/ic_exercise_*.png`), а не vector drawable:
исходники — линейная графика Flaticon, трассировка в вектор ухудшает рисунок. Чёрный штрих
перекрашивается через `ColorFilter.tint` по альфе, поэтому один файл одинаково работает на светлой
карточке, на тёмной теме и внутри насыщенного круга-маркера. Размер задаётся всегда явно: у
растрового `Painter` есть свой intrinsic size, и дефолтные 24dp `Icon` не подставляет.

Набор намеренно неполный: для жима стоя, брусьев и выпадов знака нет, они берут ближайший
корректный. Приблизительный глиф хуже честно разделённого.

**Иконки требуют атрибуции** — Flaticon, автор Icongeek26, бесплатная лицензия. Точная формулировка
кредита, место, где он обязан быть виден, и рецепт получения файлов из бандла — в
[`ATTRIBUTION.md`](ATTRIBUTION.md).

### Форматирование

- `com.nikolaevskii.lyte.core.design.format.formatWeight(weight: Double): String` — единый формат веса (целый — «60», дробный — «62.5»). Одна реализация на все фичи, чтобы правило отображения жило в одном месте.

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
| `component.stepper` | `LyteStepper` (± контрол + ручной tap-to-edit ввод; ввод ограничен: ≤5 цифр целой части и ≤2 знаков после запятой, при `allowDecimal=false` — целочисленный режим для повторов без дробной части, `fillMaxWidth` — для колонок), `LyteSetEditRow` (строка редактирования одного планового подхода программы: заголовок-параметр `title`, удаление, степперы повторов/веса — планирование, не привязано к состоянию активной сессии в отличие от `LyteTrackSetRow`) |
| `component.card` | `LyteProgramCard` (+`trailing`), `LyteExerciseCard` (`setLabels`-пилюли; ведущий элемент и действия задаёт `variant`: `LyteExerciseCardVariant.Editor` — drag-хэндл + edit/remove, `LyteExerciseCardVariant.Preview(index)` — номер упражнения без действий для read-only превью), `LyteSessionCard`, `LyteListRow` |
| `component.feedback` | `LyteDiffRow` (тона Met/Positive/Negative/Neutral/Skipped), `LyteDialog`, `LyteEmptyState` |
| `component.navigation` | `LyteTopBar` (size Small/Large), `LyteBottomNavigationBar` (+ `LyteBottomNavigationBarHeight` — резерв под него для контента, см. «Нюансы») |
| `component.overlay` | `LyteBottomSheet` (слоты `title`/`subtitle`/`topContent`/`content`/`bottomBar` + `LyteBottomSheetHeight`, см. ниже), `LyteRestTimerOverlay` |
| `component.datadisplay` | `LyteSessionStopwatch` |
| `component.session` | `LyteSetDots`, `LyteSetOverview` (`currentIndex` — автопрокрутка к плашке текущего подхода), `LyteTrackSetRow`, `LyteExerciseStrip` (экран активной сессии) |

`LyteDialog` / `LyteBottomSheet` / `LyteRestTimerOverlay` не принимают флаг видимости — видимостью
управляет вызывающая сторона самим фактом композиции (`if (showDialog) { LyteDialog(...) }`), как
принято в M3 (`AlertDialog`, `ModalBottomSheet` не имеют параметра `visible`).

### Слоты `LyteBottomSheet`

Сверху вниз: `title` → `subtitle` → `topContent` → `content` → `bottomBar`. Всё, кроме `content`,
закреплено и не скроллится: `topContent` — под строку поиска или фильтры, `bottomBar` — под основное
действие шторки, которое должно быть на виду независимо от длины контента.

Три правила для потребителя:

1. **Высота задаётся параметром `height`** (`LyteBottomSheetHeight`): `Full` — во весь экран, для
   длинных и заранее неизвестных по высоте списков; `WrapContent` — по высоте контента, для коротких
   форм на пару полей. В обоих режимах `bottomBar` прижат к низу шторки.
2. **Скролл `content` реализует потребитель**, а не шторка. Длинные списки — `LazyColumn`
   (ленивая отрисовка), короткий контент — `Column(Modifier.verticalScroll(...))`. При `Full`
   `content` получает всю оставшуюся высоту (`weight(1f)`), при `WrapContent` — свою собственную.
3. **Шторка паддингует только `title` и `subtitle`.** `topContent`, `content` и `bottomBar` паддингует
   потребитель — иначе список скроллился бы не под самый край, а прибитая снизу кнопка не смогла бы
   растянуть подложку с тенью на всю ширину. Горизонталь, к которой нужно выравниваться, —
   `LyteTheme.spacing.s5`.

```kotlin
LyteBottomSheet(
    title = "Добавить упражнение",
    onDismissRequest = onDismiss,
    topContent = {
        LyteTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Поиск по названию",
            modifier = Modifier.fillMaxWidth().padding(horizontal = LyteTheme.spacing.s5),
        )
    },
    bottomBar = {
        LyteButton(
            text = "Создать новое упражнение",
            onClick = onCreate,
            fullWidth = true,
            modifier = Modifier.padding(LyteTheme.spacing.s5),
        )
    },
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = LyteTheme.spacing.s5),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(exercises, key = { it.id }) { exercise ->
            LyteListRow(title = exercise.name, onClick = { onPick(exercise.id) })
        }
    }
}
```

Короткая форма — та же шторка, но по высоте контента:

```kotlin
LyteBottomSheet(
    title = "Новое упражнение",
    onDismissRequest = onDismiss,
    height = LyteBottomSheetHeight.WrapContent,
    bottomBar = {
        LyteButton(
            text = "Создать",
            onClick = onCreate,
            enabled = isSubmitEnabled,
            fullWidth = true,
            modifier = Modifier.padding(LyteTheme.spacing.s5),
        )
    },
) {
    LyteTextField(
        value = name,
        onValueChange = onNameChange,
        label = "Название",
        modifier = Modifier.fillMaxWidth().padding(horizontal = LyteTheme.spacing.s5),
    )
}
```

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
- `libs.compose.animation` — если модуль читает `LyteTheme.motion`: `Easing` и `tween()` живут в
  `androidx.compose.animation.core` (приезжает и транзитивно с `compose.foundation`, но полагаться
  на это не стоит).

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
- **`LyteBottomNavigationBar` не встраивается в `Scaffold.bottomBar`** (см. `App()` в `:shared`) —
  этот layout-слот меряет фактическую высоту содержимого на каждый layout pass, а slide/fade-only
  анимация показа/скрытия не уменьшает измеренный размер синхронно с визуальной анимацией (это делает
  только `shrinkVertically`/`changeSize`), из-за чего contentPadding экрана держится полным весь exit
  и потом падает в один кадр. Поэтому док рендерится как floating overlay поверх `NavHost`, а экраны —
  корни вкладок, показывающиеся вместе с ним, — сами резервируют место под него константой
  `LyteBottomNavigationBarHeight` в нижнем `contentPadding` своих скролл-контейнеров (см.
  `WorkoutListScreen` в `feature:workout:impl`).
- **`LyteBadge` — не M3 `Badge`.** M3 `Badge` — точка-уведомление; `LyteBadge` — пилюля для
  метаданных (счётчики), поэтому реализована кастомно поверх `Surface`.
- **`androidLibrary { androidResources { enable = true } }` обязателен** для любого core/feature
  KMP-модуля, у которого есть свой `composeResources/` (как здесь). Без этого флага плагин
  `com.android.kotlin.multiplatform.library` не регистрирует Android-задачи упаковки ресурсов
  (`generateAndroidMainAssets`/`mergeAndroidMainAssets`) — модуль компилируется без ошибок, но
  `stringResource`/`Font` падают в рантайме и в `@Preview` с `MissingResourceException`, потому что
  скомпилированный `.cvr`-бандл и шрифты не попадают в AAR.

## Скриншот-тесты

Все `@Preview` этого модуля автоматически снимаются в PNG (светлая и тёмная тема) — инфраструктура
в `:core:core-screenshot`, подключается плагином `lyte.screenshot`. Отдельный тест на компонент
писать не нужно: добавил компонент с превью — он под контролем.

Эталоны перегенерирует **CI** после пуша — руками их коммитить не нужно (см. корневой `CLAUDE.md`).
Локально команды нужны, только чтобы посмотреть на результат своими глазами:

```bash
./gradlew :core:core-design:recordRoborazziAndroidHostTest   # отрендерить и посмотреть
./gradlew :core:core-design:verifyRoborazziAndroidHostTest   # сверить с эталонами
```

Эталоны — `core/core-design/screenshots/*.png`: в diff'е PR видно, как компонент выглядел до и
после. После локального `record` откатить PNG перед коммитом: `git checkout -- '*/screenshots/*'`.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
