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
    Подпись цвета («Коралловый», «Индиго», …) — `lyteAccentLabel(accent)`; в интерфейсе слово не
    рисуется (цвет виден глазами), оно нужно скринридеру и долгому нажатию на кружок пикера.
  - `LyteTheme.spacing` — 4px-сетка: `s0, s1, s2, s3, s4, s5, s6, s8, s10, s12, s16, s20, s24` (`Dp`).
  - `LyteTheme.elevation` — `level1`…`level5` (`Dp`, для `shadowElevation`/`Modifier.shadow`).
  - `LyteTheme.numericTypography` — `hero`/`large`/`medium`: табличные (`tnum`) стили для «живых»
    чисел (секундомер, степпер, счётчики). Обычный текст всегда через `MaterialTheme.typography`.
  - `LyteTheme.extendedShapes` — `largeIncreased` (20dp), `extraLargeIncreased` (32dp), `full` (pill).
  - `LyteTheme.motion` — токены движения: `durationShort/Medium/Long` (150/250/400, `Int` мс —
    ложатся прямо в `tween()`) и `easingStandard/Emphasized/Decelerate/Accelerate` (`Easing`).
    Анимации компонентов и экранов берут значения отсюда, а не подбирают свои: переходы быстрые
    и без пружин. Рядом с ними (`theme/PressScale.kt`) лежит `Modifier.lytePressScale(…)` —
    общее правило нажатия, собранное из этих токенов (см. «Нюансы»); он не компонент, поэтому
    живёт при токенах, как и `TextStyle.withTabularNums()` при типографике.
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
`CircleArrowUp`, `CircleArrowDown` (превысил/недобрал цель), `CircleMinus`
(кружки-статусы подходов).

Словарь держит только то, что реально рисуется. `OverflowMenu` в дизайне v2 не используется, но у
него пока жив call-site: кебаб-меню уходит вместе со списком программ (RD-13).

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
- `com.nikolaevskii.lyte.core.design.format.lyteSetValueLabel(value: LyteSetValue): String`
  (`@Composable`) — единый формат значения подхода: «10×60 кг» при заданном весе, «10 повт» при своём.
  Публичная, потому что фича обязана писать это значение там, где его рисует не компонент ДС
  (например, пилюли целей в шторке упражнений сессии) — второму форматтеру того же значения в системе
  места нет.

### Модели

- `com.nikolaevskii.lyte.core.design.model.LyteSetValue(reps: Int, weight: Double?)` — значение
  одного подхода. `weight = null` — упражнение своего веса: это «веса нет», а не «вес 0», и
  показывать «12×0 кг» нельзя. Числами, а не строкой «12×62,5»: по паре значений компоненты считают
  расхождение плана и факта и сами подставляют единицы.

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
| `component.mark` | `LyteExerciseMark` (круг-маркер: цвет + знак движения; размеры макета — 36/38/52dp) |
| `component.progress` | `LyteProgressTrack` (`LyteProgressTrackMode.Tones`/`Plan`/`Progress`, тона — `LyteProgressTone`) |
| `component.picker` | `LyteAccentPicker` (шесть цветов), `LyteExerciseIconPicker` (сетка 5×2 знаков) |
| `component.stepper` | `LyteStepper` (size Large/Medium; ± контрол + ручной tap-to-edit ввод; ввод ограничен: ≤5 цифр целой части и ≤2 знаков после запятой, при `allowDecimal=false` — целочисленный режим для повторов без дробной части, `fillMaxWidth` — для колонок), `LyteSetEditRow` (строка редактирования одного планового подхода программы: заголовок-параметр `title`, удаление, степперы повторов/веса — планирование, не привязано к состоянию активной сессии в отличие от `LyteTrackSetRow`) |
| `component.card` | `LyteProgramCard` (маркер + один факт + `trailing`), `LyteExerciseCard` (маркер + трек плана; действия задаёт `variant`: `LyteExerciseCardVariant.Editor` — drag-хэндл + edit/remove, `LyteExerciseCardVariant.ReadOnly` — превью программы), `LyteSessionCard` (маркер + геро-число + трек), `LyteListRow` (ведущий элемент — `LyteListRowLeading.Mark`/`Icon`) |
| `component.feedback` | `LyteDiffRow` (результат подхода: факт + дельта-чип, тон — `LyteProgressTone`), `LyteDialog`, `LyteEmptyState` |
| `component.navigation` | `LyteTopBar` (size Small/Large), `LyteBottomNavigationBar` (+ `LyteBottomNavigationBarHeight` — резерв под него для контента, см. «Нюансы») |
| `component.overlay` | `LyteBottomSheet` (слоты `title`/`subtitle`/`topContent`/`content`/`bottomBar` + `LyteBottomSheetHeight`, см. ниже), `LyteRestTimerOverlay` |
| `component.datadisplay` | `LyteSessionStopwatch` |
| `component.session` | `LyteTrackSetRow` (подход на экране тренировки: спокойная строка или фокус-карточка — `LyteTrackSetState`), `LyteExerciseSetList` (все подходы упражнения с якорем фокус-карточки), `LyteSetDots`, `LyteExerciseStrip` |

`LyteDialog` / `LyteBottomSheet` / `LyteRestTimerOverlay` не принимают флаг видимости — видимостью
управляет вызывающая сторона самим фактом композиции (`if (showDialog) { LyteDialog(...) }`), как
принято в M3 (`AlertDialog`, `ModalBottomSheet` не имеют параметра `visible`).

### Маркер, трек и пикеры

Четыре компонента, на которых держится визуальная логика: маркер отвечает на «что это за
упражнение», трек — на «как оно прошло», пикеры дают пользователю задать первое.

**`LyteExerciseMark(accent, glyph, size)`** — круг-маркер, единственный визуальный якорь карточек и
строк. Несёт два сигнала: заливка = цвет упражнения, рисунок = знак движения. Оба — обычные свойства
упражнения, выбранные пользователем; ничего не выводится из данных и таксономии за ними нет. Глиф
занимает 0.58 диаметра, поэтому маркер одинаково читается и на 36dp, и на 52dp. По умолчанию
декоративен (`contentDescription = null`) — рядом всегда стоит название упражнения; подпись нужна
там, где нажимают по самому маркеру. Фотографий в системе нет: параметр `image` веб-версии не
перенесён.

**`LyteProgressTrack(mode)`** — прогресс подходов сегментами вместо чисел. Режим задаётся
`LyteProgressTrackMode`, чтобы исключить бессмысленные сочетания полей:

| Режим | Что показывает | Где |
|---|---|---|
| `Tones(tones)` | как прошла сессия — по сегменту на подход | карточка истории, итог сессии, детали |
| `Plan(total, accent)` | что запланировано и не начиналось | карточка упражнения в программе и превью |
| `Progress(total, done, missed)` | сколько подходов позади | сводки по ходу сессии |

В режиме `Tones` **исход кодируется высотой сегмента, а не только цветом**: превысил цель — во всю
высоту, попал — по средней линии, недобрал — короткий, пропустил — полая обводка, впереди — тонкая
риска. Пять состояний неразличимы одним оттенком на пятипиксельной полоске (два зелёных — «попал» и
«превысил» — сливаются), а легенда убила бы саму идею беглого взгляда. Тона (`LyteProgressTone`) —
общий словарь системы: одно и то же событие обязано выглядеть одинаково и здесь, и в `LyteDiffRow`.

Ширину задаёт вызывающая сторона (`Modifier.width(56.dp)` в карточке упражнения, `fillMaxWidth()`
для сводки сессии) — сегменты делят её поровну.

**`LyteAccentPicker(value, onChange)`** и **`LyteExerciseIconPicker(value, accent, onChange)`** —
выбор цвета и знака, обычно рядом в одной шторке. Выбранный цвет отмечен **кольцом снаружи, а не
галочкой**: галочка внутри кружка закрыла бы собой цвет, который в этот момент и выбирают. Кольцо
входит в габариты компонента, а не висит поверх соседей, как box-shadow в вебе, — иначе его срезал
бы первый же скроллер. Сетка знаков перекрашивается вслед за `accent`, и два пикера читаются как
одно решение: выбрал цвет — перекрасилось всё, выбрал знак — маркер готов; тайл выглядит ровно так,
как будет выглядеть маркер в списке. Подпись поля (`label`) можно убрать, передав `null`.

```kotlin
Column {
    LyteExerciseMark(accent = accent, glyph = glyph, size = 52.dp)
    LyteAccentPicker(value = accent, onChange = { accent = it })
    LyteExerciseIconPicker(value = glyph, accent = accent, onChange = { glyph = it })
    LyteProgressTrack(
        mode = LyteProgressTrackMode.Plan(total = 4, accent = accent),
        modifier = Modifier.width(56.dp),
    )
}
```

### Анатомия карточки

У всех карточек она **одна и фиксированная**:

```
маркер → заголовок → один тихий факт → [геро-число справа] → [трек во всю ширину]
```

Больше в карточку не помещается ничего. Это не стилевое пожелание, а следствие: в v1 карточка несла
две строки метаданных равного веса («5 упражнений · посл. сессия 2 июл»), и три таких карточки
подряд не давали глазу за что зацепиться. Поэтому факт — ровно один, а всё остальное уезжает на
экран-деталь или на касание глубже.

| Компонент | Маркер | Факт | Геро | Трек |
|---|---|---|---|---|
| `LyteProgramCard` | 52dp | «N упражнений» | — | — |
| `LyteSessionCard` | 52dp | дата | длительность (19sp/700, табличная) | `Tones` или `Progress` |
| `LyteExerciseCard` | 38dp | «N подходов» | — | `Plan` в слоте 76dp |
| `LyteListRow` | 36dp | подзаголовок | — | — |

Тексты и числа формирует вызывающая фича: компоненты задают раскладку и стиль, но не склеивают
единицы и не выбирают форму множественного числа. У `LyteExerciseCard` из этого следует пара
параметров `setCount` + `setsLabel`: первое задаёт число сегментов трека, второе — готовую подпись,
и они обязаны быть про одно и то же число.

`LyteSessionCard.track` принимает режим целиком (`LyteProgressTrackMode?`), а не список тонов:
по-хорошему сводка сессии — это `Tones`, но пока исходы подходов не посчитаны, честнее показать
`Progress` («сколько подходов позади»), чем выдумывать тона.

**Плотный план трека не получает.** Сегмент в узком слоте зажат с двух сторон (`lytePlanTrackWidth`):
сверху 16dp — чтобы при одном-трёх подходах трек не растягивался на весь слот и выглядел как до
расширения; снизу 9dp — порог, ниже которого пилюля при высоте 5dp становится кругом. В слоте 76dp
это шесть подходов; семь пилюль туда не влезают ни при каком зазоре, поэтому от семи трек **не
рисуется вовсе** и подпись «N подходов» становится обычным подзаголовком под названием — как у
упражнения без подходов. Так в списке не соседствуют карточки с пилюлями и карточки с точками:
трек — избыточность к подписи, и когда он не читается взглядом, он только шумит. Число подходов
при этом не теряется, оно в подписи.

Кебаба на карточках нет: единственное действие (удаление) стоит в `trailing` прямо на карточке.

### Подходы на экране тренировки

**`LyteTrackSetRow(number, state)`** — один подход. Форму задаёт `LyteTrackSetState`, и это два
разных элемента, а не флаг у одного:

| Арм | Вид | Что несёт |
|---|---|---|
| `Resting(tone, value, note)` | спокойная строка 36dp, залитая тоном | исход подхода и заметку одной строкой |
| `Current(total, reps, weight, target, last, шаги)` | фокус-карточка: обводка `primary` 2dp + тень | степперы повторов и веса, ориентиры «Цель» / «В прошлый раз», слот `content` под заметку или чип |

Значения — `LyteSetValue(reps, weight)`, как и в `LyteDiffRow`: числами, а не готовой строкой, чтобы
единицы («повт», «кг») подставлял компонент, а не фича. У `Resting` это **одно** поле `value`, а не
пара «факт + цель»: по тону всегда валидно ровно одно из них, и второе поле означало бы невозможные
комбинации. У `Current.weight` `null` не бывает: степпер веса стоит в карточке всегда, в том числе
когда цель — свой вес. Ноль там значит «пока без веса», а не «веса не бывает», иначе к подтягиваниям
нечем было бы добавить пояс. Тем, что вес нулевой, распоряжается отображение значения:
`LyteSetValue.weight = null` — и строка пишет «12 повт», а не «12×0 кг».

У степпера повторов пол в **1**, как и при планировании подхода в `LyteSetEditRow`: подход на ноль
повторов — это пропуск, а не результат, и для пропуска на экране тренировки есть отдельная кнопка.
Минимум задан самим компонентом, а не параметром `LyteTrackSetState`: это правило системы, и второй
способ его задать разошёлся бы с `LyteSetEditRow`. У веса минимум обычный, 0.

Тон спокойной строки — тот же `LyteProgressTone`, что и у трека: собственного словаря у строки нет,
иначе один и тот же исход разъехался бы по виду между экраном тренировки и деталями сессии. Тон
решает и цвет, и иконку, и что стоит справа:

| Тон | Иконка | Значение справа |
|---|---|---|
| `Met` / `Positive` / `Negative` | галочка / стрелка вверх / стрелка вниз | факт из `value` |
| `Skipped` | минус | «пропущен» (`value` не читается) |
| `Todo` | пустой круг | «цель …» из `value` |

**Крестика нет ни в одном состоянии**: недобор до цели — это направление, а не провал, и ✗ рядом с
подходом, который человек всё-таки сделал, читается как наказание. По той же причине спокойная
строка не пишет слово «Подход» — позиция в списке и так говорит, какой это подход, а отказ от слова
и есть то, что позволяет семи отработанным подходам поместиться на экране рядом с фокус-карточкой.

### Результат подхода в истории

**`LyteDiffRow(index, tone, target, actual, note)`** — та же пятёрка исходов, но постфактум, в
деталях завершённой сессии. Значения — `LyteSetValue(reps, weight)`, числами, а не готовой строкой:
по паре «цель — факт» компонент сам считает расхождение и сам подставляет единицы («повт», «кг») —
они его собственный «хром», а не доменный текст фичи. `weight = null` — упражнение своего веса, и
строка покажет «12 повт», а не «12×0 кг».

Факт назван **один раз и крупно** («12×62,5 кг»), а сравнение с целью выражено чипом-дельтой
(«+2 повт · +2,5 кг»). Цель не дублируется числами: её роль играет тон строки. **У подхода ровно в
цель чипа нет вовсе** — сообщать нечего. В v1 строка выписывала обе стороны целиком («10 повт · 60 кг
→ 12 повт · 62,5 кг») — шесть элементов в строке и пятнадцать строк на экране.

Заметка — свободный текст, написанный между подходами, поэтому её длина не ограничена: она **всегда**
идёт отдельной строкой под числами и никогда не обрезается. Одна форма у каждой строки — заметка не
спорит с чипом за ширину, строки списка остаются параллельными, и написанное человеком читается
целиком.

Тон — `LyteProgressTone`, общий для трека, строки подхода и этой строки: **собственного словаря
исходов у компонентов нет**. `Skipped` рисует «пропущено» вместо чисел; `Todo` в завершённой сессии
не встречается, но из словаря не выкидывается и выглядит ровно как «ещё не выполнен» на экране
тренировки.

**`LyteExerciseSetList(sets, …)`** — композиция экрана тренировки целиком: все подходы упражнения по
порядку, фокус-карточка среди них. Решает то, чего не может обычный список: карточка — единственный
элемент, которого касаются посреди подхода, и уехать за экран она не должна ни при одном подходе, ни
при восьми.

В вебе это `position: sticky; bottom: 0`; в Compose липкого низа нет, поэтому перенесено
**поведение**: список один, карточка — обычный элемент, а её позиция доводится программной
прокруткой так, чтобы низ карточки был в 104dp от низа области. Выполненные подходы уходят выше и
остаются в одном движении пальца, будущие — ниже и в настоящем порядке. Первая установка позиции
мгновенная, смена подхода — с анимацией по токенам движения: иначе список подъезжал бы при каждом
появлении экрана. Над первым подходом лежит пустая отбивка в высоту области — без неё карточку
первого подхода нечем опустить к якорю, прокручивать было бы нечего.

Список **не ленивый** (`Column` + `verticalScroll`): подходов в упражнении единицы, зато позиции всех
элементов известны сразу — на них и держится якорь. Требует **ограниченную по высоте** область
(`Modifier.weight(1f)` в колонке экрана или явная высота); в неограниченной работает как обычная
колонка, без прокрутки и якоря. Горизонтальные отступы задаёт вызывающая сторона — тени карточки
нужен запас по бокам, иначе её срежет край скроллера.

```kotlin
LyteExerciseSetList(
    sets = state.sets,
    onRepsChange = { onIntent(ActiveSessionIntent.ChangeReps(it)) },
    onWeightChange = { onIntent(ActiveSessionIntent.ChangeWeight(it)) },
    currentContent = { NoteSlot(note = state.note, onClick = { onIntent(ActiveSessionIntent.EditNote) }) },
    footer = { LyteOverline(text = lastSetLabel) },
    modifier = Modifier.weight(1f).padding(horizontal = LyteTheme.spacing.s5),
)
```

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
            LyteListRow(
                title = exercise.name,
                subtitle = exercise.description,
                leading = LyteListRowLeading.Mark(accent = exercise.accent, glyph = exercise.glyph),
                onClick = { onPick(exercise.id) },
            )
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
- **Нажатие — уменьшение контрола поверх M3-овского state layer.** Кнопка, икон-кнопка и чип жмутся
  до 0.97, кнопки ± степпера — до 0.94 и вдобавок перекидывают заливку в `primary`/`onPrimary`
  (одноручный тап вслепую должен дать подтверждение). Длительность и кривая — из
  `LyteTheme.motion`; общий модификатор — `Modifier.lytePressScale(interactionSource)`.
  Своя реализация, а не средство M3: `Indication` (штатная точка расширения отклика) недоступна —
  `Button`/`FilterChip`/`IconButton` принимают только `interactionSource`, а подмена
  `LocalIndication` их не достаёт, они зовут `ripple()` явно; штатный отклик M3 Expressive (морф
  формы) на `full`-пилюлях не виден.
- **Выключенная `LyteButton` гасится целиком до alpha 0.38**, а не подменяет цветовые роли: так
  задано в дизайн-системе. Поэтому M3-дефолты выключенных ролей приравнены к обычным — иначе
  кнопка тускнела бы дважды.
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
