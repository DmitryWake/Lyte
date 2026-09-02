---
name: lyte-design-system
description: Дизайн-система Lyte (core-design) — кит Lyte*, токены LyteTheme, правила экрана и компонента, что брать готовым вместо самописного. Использовать при любой правке Compose-UI и при ревью UI-диффа.
---

# Дизайн-система Lyte

`:core:core-design`. `LyteTheme(darkTheme, content)` настраивает M3 `colorScheme`/`typography`/`shapes`
под токены Lyte и прокидывает расширенные токены через `CompositionLocal`. Корень UI-дерева в `App()`
оборачивается в `LyteTheme { … }` один раз — в экранах его не оборачивают повторно.

## Токены: где что брать

Доступ — аксессор-object `LyteTheme.*`, по аналогии с `MaterialTheme`:

| Токен | Что внутри |
|---|---|
| `LyteTheme.extendedColors` | цвета сверх M3-палитры |
| `LyteTheme.accents` | шесть акцентов упражнения (`LyteAccent`, дефолт `Slate`) |
| `LyteTheme.spacing` | `s1`…`s6` — вертикальные и горизонтальные отступы |
| `LyteTheme.elevation` | `level1`…`level2` |
| `LyteTheme.numericTypography` | табличные цифры, геро-числа |
| `LyteTheme.extendedShapes` | формы сверх M3 (`largeIncreased` и т.п.) |
| `LyteTheme.motion` | длительности и easing'и — **все** анимации ДС и экранов берут их отсюда |

Базовые цвета и типографика — из `MaterialTheme.colorScheme` / `MaterialTheme.typography`. Хардкод
цвета или своя длительность анимации — находка на ревью.

Шрифты: Space Grotesk (весь UI и цифры), Inter Tight (только вордмарк). Бандлятся в модуле.

## Сначала посмотри, есть ли готовое

Прежде чем писать свой composable — проверь кит. Он покрывает почти всё:

- **Кнопки и ввод**: `LyteButton` (variant × accent × size), `LyteIconButton`, `LyteChip`,
  `LyteTextField`, `LyteSwitch`, `LyteStepper`.
- **Карточки и строки**: `LyteProgramCard`, `LyteExerciseCard`, `LyteSessionCard`, `LyteListRow`,
  `LyteSetEditRow`.
- **Маркер и трек**: `LyteExerciseMark` (круг: цвет + знак движения, 36/38/52dp),
  `LyteProgressTrack` (`LyteProgressTrackMode.Tones` / `Plan` / `Progress`, тона — `LyteProgressTone`).
- **Обратная связь**: `LyteDiffRow`, `LyteDialog`, `LyteEmptyState`, `LyteBadge`, `LyteOverline`.
- **Навигация и оверлеи**: `LyteTopBar` (Small/Large), `LyteBottomNavigationBar`
  (+ `LyteBottomNavigationBarHeight`), `LyteBottomSheet`, `LyteRestTimerOverlay`.
- **Сессия**: `LyteTrackSetRow`, `LyteExerciseSetList`, `LyteSetDots`, `LyteExerciseStrip`.
- **Пикеры**: `LyteAccentPicker`, `LyteExerciseIconPicker`.
- **Иконки**: `LyteIcons` — курируемый словарь поверх Lucide. Пиктограммы движений —
  `LyteExerciseGlyph` + `LyteExerciseIcon`.

Полный список с сигнатурами — `core/core-design/README.md`.

## Правила экрана

- Корень каждого экрана — `Scaffold`, даже если `TopBar` и `BottomBar` не нужны.
- Заголовок — `LyteTopBar` в слоте `topBar`. Кнопка назад — параметр `onBack`, не свой `IconButton`.
- Корни вкладок сами резервируют место под плавающий док: `LyteBottomNavigationBarHeight` в нижнем
  `contentPadding`. Док рисуется overlay вне `Scaffold.bottomBar` (см. `App.kt`).
- Любая длительная операция показывает `loading` / `content` / `error`.

## Правила компонента

- `modifier: Modifier = Modifier` — **последний** параметр. Единственное исключение: обязательная
  замыкающая content-лямбда без дефолта (слот `content` у `LyteBottomSheet`) — тогда `modifier` идёт
  прямо перед ней. Необязательные слоты со значением по умолчанию (`trailing = null`, `actions = {}`)
  — обычные параметры и идут до `modifier`.
- Оверлеи не принимают флаг видимости: `LyteDialog`, `LyteBottomSheet`, `LyteRestTimerOverlay`
  управляются самим фактом композиции — `if (show) { LyteDialog(...) }`, как M3 `AlertDialog`.
- Доменный текст — всегда параметр вызывающей фичи. Внутри модуля живёт только компонентный «хром»
  (`a11y_*`, подписи вроде `Повт`/`Вес`). Хардкод строк в composable запрещён — только
  `stringResource`.
- Никаких магических чисел, кроме отступов и размеров в Compose (`padding`, `size`, `dp`).
- `@Preview` обязателен на каждый публичный компонент — он же источник истины для скриншот-тестов.

## Ресурсы

Иконки — vector drawable `composeResources/drawable/ic_*.xml`, подключение через `painterResource`.
Единственное исключение — растровые пиктограммы движений (`ic_exercise_*.png`, Flaticon): тёмная тема
решена тонированием по альфе (`ColorFilter.tint` в `LyteExerciseIcon`), а не подменой файла. Других
растровых иконок в проекте быть не должно. Кредит Flaticon обязателен —
`core/core-design/ATTRIBUTION.md`.

## Правило README

Изменил исходники `core-design` — открой `core/core-design/README.md` и приведи его в соответствие в
том же коммите: переименования, новые и удалённые публичные типы, изменения сигнатур.
