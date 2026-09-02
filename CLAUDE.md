# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Формат ответов

Отвечай кратко, последовательно и понятно. Думай и отвечай на русском языке (если не сказано иначе). Экономь токены.

**Кратко и структурно — везде, не только в чате.** Ответы, сообщения коммитов и описания PR — выжимка,
которую понимаешь беглым взглядом: короткие пункты, каждый про одно решение. Не пересказывай диффом то,
что и так видно в коде; не расписывай очевидное; не дублируй одну мысль в трёх местах. В описании PR —
что изменилось и почему так, а не как. Развёрнутое обоснование уместно только там, где решение
неочевидно и его иначе не понять.

## Проект

Lyte — фитнес-трекер на Kotlin Multiplatform (Android + iOS), UI шарится через Compose Multiplatform. Корневой пакет: `com.nikolaevskii.lyte`. Имя корневого Gradle-проекта: `Lyte`.

Стадия — **функциональный MVP**: реализованы splash + сид библиотеки, трекер (лендинг / выбор программы / превью / активная сессия), редактор программ, история (список + детали сессии); полностью «прошитая» инфраструктура (модули, MVI, DI, навигация, БД) и дизайн-система (`core-design`). Не реализованы: сеть, авторизация, UI-тесты, deep links. Цель — чтобы новая фича добавлялась без правок инфраструктуры.

## Сборка и запуск

- Android (debug APK): `./gradlew :androidApp:assembleDebug`
- Android (релизный APK): `./gradlew :androidApp:assembleRelease` — это формат для RuStore. AAB (`:androidApp:bundleRelease`) нужен для Google Play; RuStore для AAB требует загрузить приватный ключ подписи на свои серверы, поэтому по умолчанию собираем APK. Релиз собирается с R8 + `shrinkResources` (keep-правила — `androidApp/proguard-rules.pro`, обязательны для `@Serializable` route-классов навигации). Подпись — только из `keystore.properties` в корне (в `.gitignore`): ключи `storeFile`/`storePassword`/`keyAlias`/`keyPassword`. Без этого файла релизные задачи падают с внятной ошибкой — молчаливого отката на debug-ключ нет, чтобы неподписанная как надо сборка не уехала в стор. Keystore и пароли в репозиторий не коммитятся.
- iOS: открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить. iOS подключает статический фреймворк `Shared`, который собирает модуль `:shared` (`iosArm64`, `iosSimulatorArm64`); `MainViewController()` из фреймворка оборачивается в `ContentView.swift` через `UIViewControllerRepresentable`.

## Тесты

- Тесты живут в модулях, где есть логика: `:core:core-mvi`, `:core:core-db`, `:core:core-workout`, `:core:core-session`, `:feature:{tracker,workout,history,splash}:impl`. Гейт (гонять перед коммитом/релизом, macOS):
  - Host/JVM: `./gradlew :core:core-mvi:testAndroidHostTest :core:core-db:testAndroidHostTest :core:core-workout:testAndroidHostTest :core:core-session:testAndroidHostTest :feature:tracker:impl:testAndroidHostTest :feature:workout:impl:testAndroidHostTest :feature:history:impl:testAndroidHostTest :feature:splash:impl:testAndroidHostTest`
  - iOS-симулятор (ловит Kotlin/Native-only поломки): те же модули с `:iosSimulatorArm64Test`, **кроме** `:core:core-db` — его единственный тест (миграция) живёт в `androidHostTest`, потому что требует Robolectric.
  - Сборки: `./gradlew :androidApp:assembleDebug` и `:shared:linkDebugFrameworkIosSimulatorArm64`.
  - Скриншоты в гейт не входят: их сверяет и перегенерирует CI (см. «Скриншот-тесты»). На macOS
    локальный `verifyRoborazzi*` краснеет из-за субпиксельных расхождений — это не поломка.
- Common-тесты — в `commonTest` каждого модуля. Запуск одного теста: добавь `--tests "fully.qualified.ClassName.method"` к нужной Gradle-задаче.
- Стратегия: **Unit** — для сложной бизнес-логики (ViewModel-переходы, репозитории, мапперы) — сделано; **скриншот-тесты UI** — сделано (см. ниже); **интеракционные UI-тесты** (клики, ввод) — ещё не написаны.

### Скриншот-тесты

Источник истины — `@Preview`. Отдельный тест на экран не пишется: модуль объявляет один
параметризованный класс, и **все** его превью снимаются автоматически в светлой и тёмной теме.
Инфраструктура — `:core:core-screenshot` + convention-плагин `lyte.screenshot`.

- Покрыты: `core-design` и все четыре `feature/*/impl`.
- Эталоны — `<module>/screenshots/*.png`, лежат в гите: GitHub показывает before/after в diff'е PR.

**Эталоны генерирует только CI** (джоба `screenshots` в `.github/workflows/ci.yml`, запинённый
`ubuntu-24.04`). Одна среда рендера — значит нет расхождений между машинами: на macOS картинки
отличаются субпиксельно. Правишь UI → пушишь **только код** → CI сам перегенерирует эталоны,
закоммитит их в ветку PR и напишет комментарий со списком задетых экранов.

- **Руками эталоны не коммитим.** `recordRoborazziAndroidHostTest` локально запускать можно и
  полезно — но только чтобы **посмотреть на результат глазами**. Перед коммитом откатить:
  `git checkout -- '*/screenshots/*'` (и удалить новые PNG, если появились).
- `verifyRoborazziAndroidHostTest` — для локальной сверки; на macOS будет ложно краснеть из-за
  субпиксельных расхождений, это ожидаемо. Дифф-картинки — `compareRoborazziAndroidHostTest`,
  результат в `build/outputs/roborazzi/` (`*_compare.png` — эталон/факт/дифф).
- Поскольку CI перезаписывает эталоны, сверка **не блокирует** мерж: визуальная регрессия приезжает
  в PR как изменившаяся картинка, и поймать её должен ревьюер, посмотрев diff.
- **Скриншоты покрывают только то, у чего есть `@Preview`.** Добавил новое состояние экрана (новый
  арм `UiState`) — заведи на него превью, иначе оно выпадет из-под контроля. И помни, что снимается
  превью с фейковыми данными, а не живое приложение: навигацию, реальные данные и реакцию на клик
  скриншот не проверяет.
- Подробности (как решены анимации, темы, порог сравнения) — `core/core-screenshot/README.md`.

## Модули

В `settings.gradle.kts` включён `TYPESAFE_PROJECT_ACCESSORS`, поэтому в скриптах модули указываются как `projects.shared`, `projects.core.coreMvi` и т.д.

- `:androidApp` — Android-хост (`com.android.application`). `LyteApp : Application` инициализирует Koin (`initKoinShared { androidContext(...) }`), `MainActivity` вызывает шаренный `App()`. Namespace и `applicationId` — `com.nikolaevskii.lyte`.
- `:shared` — KMP + Compose Multiplatform модуль-агрегатор. Собирает статический фреймворк `Shared` для iOS и Android-библиотеку (namespace `com.nikolaevskii.lyte.shared`). Использует плагин `com.android.kotlin.multiplatform.library` (AGP 9.x) — конфигурация Android **внутри** блока `kotlin { androidLibrary { … } }`, отдельного top-level `android {}` блока нет. Compose-ресурсы включены. Владеет корневым `NavController`, `App()` (тема + bottom navigation + `LyteNavHost`) и точкой инициализации Koin (`di/KoinInit.kt`).
- `:core:core-mvi` — чистый KMP-модуль (Android + iOS, без Compose UI), база MVI. Namespace `com.nikolaevskii.lyte.core.mvi`.
- `:core:core-navigation` — KMP + Compose, общие нав-хелперы (`popUpToRoute<R>()`, `singleTop()`, `restorable()`), `LyteNavigator`/`LyteNavigatorImpl`, `TopLevelDestination`, собственный Koin-модуль `coreNavigationModule`. Namespace `com.nikolaevskii.lyte.core.navigation`.
- `:core:core-di` — KMP, только инициализация Koin (`initKoin`). Общих синглтонов (Navigator, DispatcherProvider и т.п.) здесь нет — каждый core/feature-модуль владеет своим Koin-модулем и подключает его сам. Namespace `com.nikolaevskii.lyte.core.di`.
- `:core:core-design` — дизайн-система: `LyteTheme` (M3 `colorScheme`/`typography`/`shapes` + расширенные токены — акценты упражнений, spacing, elevation, числовая типографика, доп. формы, движение) и компонент-кит (`Lyte*`) поверх M3-примитивов. Иконки — `LyteIcons` поверх `com.composables:icons-lucide-cmp`. Namespace `com.nikolaevskii.lyte.core.design`. Подробности и полный список компонентов — в `core/core-design/README.md`.
- `:core:core-db` — Room KMP: `LyteDatabase` (`@ConstructedBy` + `expect object LyteDatabaseConstructor`), 8 сущностей `*DatabaseEntity` в трёх доменах (`db/workout/`, `db/session/`, `db/app/`) + 4 DAO, `version = 1`, `is_archived` (soft-delete) и `name_normalized` (ASCII-коллация для поиска), expect/actual билдер БД (`androidMain`/`iosMain`), `coreDbModule()`. Namespace `com.nikolaevskii.lyte.core.db`. Подробности — `core/core-db/README.md`.
- `:core:core-workout` — data-слой библиотеки упражнений и программ: доменные модели (`WorkoutEntity` и т.д.), маркер упражнения и программы (`ExerciseAccent`/`ExerciseGlyph` — доменная пара к UI-шным `LyteAccent`/`LyteExerciseGlyph`, маппинг делает фича), интерфейсы `WorkoutRepository`/`WorkoutExerciseRepository` + impl, `coreWorkoutModule()`. Общие данные приложения (пишет workout, читают tracker/splash), поэтому в core, а не в фиче. Namespace `com.nikolaevskii.lyte.core.workout`.
- `:core:core-screenshot` — тестовая инфраструктура скриншотов: рендер `@Preview` в PNG headless на JVM (Robolectric + Roborazzi), light/dark, остановленные часы композиции. Подключается только к `androidHostTest` через convention-плагин `lyte.screenshot`. Namespace `com.nikolaevskii.lyte.core.screenshot`. Подробности — `core/core-screenshot/README.md`.
- `:core:core-session` — data-слой сессий тренировки: `Session*`-модели, `SessionHistoryRepository` (read для истории) / `WorkoutSessionRepository` (write для трекера, ISP-сплит), доменные правила `SessionProgression`/`SessionSetOutcomeUtils`/`SessionPlanProgression` (прогрессия плана: завершение сессии подтягивает цели программы под факты), `coreSessionModule()`. Namespace `com.nikolaevskii.lyte.core.session`.
- `:feature:splash:{api,impl}` — стартовый экран (`SplashRoute` — корень `NavHost`): анимация вордмарка + гейт первого запуска. `:impl` — `AppInitializer`/`AppInitializationManager` и `WorkoutLibraryInitializer` (одноразовый сид `DefaultExerciseLibrary`/`DefaultWorkoutPrograms`), гейт — флаг `app_launch_state`, а не пустота таблиц.
- `:feature:tracker:{api,impl}` — вкладка «Трекер»: лендинг (гейт активной сессии + шторка выбора программы), превью, активная сессия. Данные сессий берёт из `:core:core-session`.
- `:feature:workout:{api,impl}` — вкладка «Тренировки»: список программ, редактор программы, библиотека упражнений (шторки выбора/создания). Данные из `:core:core-workout`.
- `:feature:history:{api,impl}` — история завершённых сессий: список с группировкой по месяцам + экран деталей сессии. Читает `SessionHistoryRepository` из `:core:core-session`; единственный потребитель `kotlinx-datetime`.

Новый модуль регистрируется в `settings.gradle.kts` через `include(":path:to:module")`, подключается типобезопасным акцессором (`implementation(projects.core.coreNavigation)` и т.п.).

### README модулей

У каждого core-модуля (`core/core-*`) есть свой `README.md` с назначением, публичным API, инструкцией по подключению, runnable-примером использования и списком **дополнительных** зависимостей, которые модуль НЕ тянет сам (например, `koin-compose-viewmodel` для `koinViewModel()` или `lifecycle-runtime-compose` для `collectAsStateWithLifecycle()`).

**Правило: при любом изменении исходников core-модуля обязательно открой его `README.md`. Если README перестал отражать актуальный публичный API, поведение или способ подключения — отредактируй его в том же изменении.** Это касается переименований, новых/удалённых публичных типов и функций, изменений сигнатур, перевода зависимостей между `api`/`implementation`, новых обязательных плагинов, переноса Koin-модулей между core-модулями и т.п.

Сейчас правило обязательно только для `:core:*`; по мере появления README у других модулей (`:feature:*` и т.д.) область его действия расширится на них.

### Целевая структура

- **Multi-Module по фичам, api/impl-split**: каждая фича — **два** Gradle-модуля по схеме `feature/<name>/api` и `feature/<name>/impl`.
  - **`:api`** — pure KMP (Android + iOS, **без Compose** и Android-специфики). Содержит `@Serializable` route-классы — их видят другие фичи, чтобы навигироваться сюда типобезопасно. **Общие данные (модели + Repository-интерфейсы) живут не в `:feature:*:api`, а в core-модулях данных** (`:core:core-workout`, `:core:core-session`): фича-модуль не должен быть data-контрактом для других фич. **UseCase-слоя нет намеренно**: ViewModel обращается к репозиторию напрямую (репозитории тонкие, один источник — Room); сложные правила выносятся в чистые доменные функции/сервисы (`SessionProgression`, `SessionSetOutcomeUtils`), а не в pass-through UseCase.
  - **`:impl`** — KMP + Compose. Экраны (`*Screen` + `*Content`), `ViewModel`-и, `NavGraphBuilder.<feature>Graph(...)` extension, реализации контрактов из `:api`. Слои **`domain` / `data` / `presentation`** разбиты **пакетами** внутри `:impl`. `:impl` экспонирует `:api` через `api(projects.feature.<name>.api)`, чтобы агрегатор `:shared` видел routes без отдельной зависимости.
    - **Разбиение пакета `presentation` (обязательно):**
      - `presentation/screen` — экраны: `*Screen` (stateful, `koinViewModel()`) + `*Content` (stateless, превьюшный).
      - `presentation/viewmodel` — `ViewModel`-и экранов (`BaseViewModel<…>`).
      - `presentation/navigation` — `NavGraphBuilder.<feature>Graph(...)` extension'ы.
      - `presentation/model/mvi` — MVI-контракт экрана: `UiState`, `UiIntent` и их реализации.

      Эталон разбиения — `feature/workout/impl`.
- Общий **`core`**-модуль для переиспользуемой инфраструктуры (`core-mvi`, `core-navigation`, `core-di`, `core-design`, `core-db`).
- Принципы: **Clean Architecture**, **SSOT** (Single Source of Truth), **Offline-first** (приоритет локальных данных).
- При проектировании модулей и границ — skill `kotlin-project-modularization`.

## MVI-каркас (`:core:core-mvi`)

- `UiState` и `UiIntent` — пустые маркер-интерфейсы. **Без `Effect`** — навигация и прочие эффекты идут через `LyteNavigator`/явное состояние, а не через отдельный канал one-shot событий.
- ViewModel-ы экранов наследуются от `BaseViewModel<State : UiState, Intent : UiIntent>`. База предоставляет `uiState: StateFlow<State>`, реализует `CoroutineScope` поверх `viewModelScope.coroutineContext + CoroutineExceptionHandler` — **без** дополнительного `+ SupervisorJob()`: тот заменил бы `Job`, привязанный к `onCleared()`, и корутины пережили бы очистку VM (см. комментарий в `BaseViewModel.kt`). У `CoroutineExceptionHandler` иной `Key`, поэтому он `Job` не заменяет. Требует `getInitialState()` и `onIntent(intent)`.
- **UiState экрана — sealed-иерархия взаимоисключающих состояний** (`Loading`/`Error`/`Empty`/`Content` и т.п.), а не `data class` с флагами `isLoading`/`isError`; рендер экрана — исчерпывающий `when` без невозможных комбинаций. Сквозные поля (`id` редактора, `query`/`result` шторки) выносятся из армов наружу только с обоснованием. Эталон — `HistoryUiState`, `WorkoutDetailsUiState`.
- **Ошибки** — типизированный `LyteError` (`:core:core-mvi`), а не сырой `Throwable.message`. Непойманный сбой корутины VM приходит в `protected open fun handleError(error)` (наследник переводит в свой `Error`-арм); `CancellationException` туда не попадает. Экран мапит `LyteError` в `stringResource`. Служебный `Job`-хэндл как guard от повторного запуска (напр. `SplashViewModel.initializationJob`) — допустимое исключение из правила «не храни поля во VM».
- Изменение стейта — только через защищённый `updateState { copy(...) }`; текущее значение — `uiStateValue`. `_uiState` инициализируется лениво из `getInitialState()`.
- База построена на `androidx.lifecycle.ViewModel` из мультиплатформенного `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose`, поэтому работает на обеих платформах без `expect`/`actual`.
- **Не храни поля во `ViewModel`**: всё нужное живёт в `UiState` либо читается из него. Значения, нужные только при инициализации (например, id из аргументов роута), передавай через конструктор в `getInitialState()`.
- **Диспатч интентов — через ссылку `viewModel::onIntent`.** Stateful `*Screen` собирает стейт и передаёт во вложенный stateless `*Content` (и далее вглубь) **одну** функцию `onIntent: (XIntent) -> Unit` как `viewModel::onIntent`. Конкретный интент создаёт и шлёт сам вложенный composable: `onClick = { onIntent(XIntent.OpenDetails(id)) }`. **Запрещено** оборачивать вызов VM в лямбду-проп на уровне экрана и пробрасывать вниз набор узких колбэков под каждый интент. Навигация — тоже интент: переходы (`OpenX`, `Back`) идут через `onIntent` → VM → `LyteNavigator`, а не через отдельные нав-колбэки.

## Навигация (`:core:core-navigation` + фичи)

- Библиотека — `org.jetbrains.androidx.navigation:navigation-compose` (мультиплатформенная Jetpack Navigation 2). Типобезопасные роуты — `@Serializable` data class / data object.
- **Routes лежат в `:feature:<name>:api`** (kotlinx-serialization, без Compose). Это делает их доступными другим фичам и `:shared` без подтягивания `:impl`.
- **Экраны и `NavGraphBuilder.<feature>Graph()` лежат в `:feature:<name>:impl`** (без нав-колбэков). Внутри — `composable<XRoute> { entry -> XScreen(args = entry.toRoute<XRoute>()) }`; навигацию инициирует VM экрана через `LyteNavigator`.
- **`NavController` создаётся в `App()` и используется только внутри `App()`/`LyteNavHost` (`:shared`)**, никуда глубже не пробрасывается. Навигацию инициирует **ViewModel** через инжектируемый `LyteNavigator` (`:core:core-navigation`, регистрируется в его собственном Koin-модуле `coreNavigationModule`): VM шлёт команды (`navigate(route)` / `back()` / `switchTab(...)`), а `LyteNavHost` — **единственный** подписчик `lyteNavigator.commands` — применяет их к `NavController`. Графы и экраны навигационных колбэков **не принимают**.
- Аргументы роутов — минимальные (id, enum), не доменные модели. Экран загружает данные сам через ViewModel + Repository.
- Стек-шейпинг — из VM через декларативные `LyteNavOptions` (`lyteNavigator.navigate(route, LyteNavOptions(popUpTo = …, popUpToInclusive = …))`) либо DSL-хелперы из `:core:core-navigation` (`popUpToRoute<R>()`, `singleTop()`, `restorable()`) в шелле.
- Плагин `org.jetbrains.kotlin.plugin.serialization` применяется в каждом модуле, где используется типобезопасное API навигации: `:api`, `:impl`, `:shared`.
- **Корень `NavHost` — `SplashRoute`** (`splashGraph()`), не `BottomNavGraph`: приложение стартует со сплэша, и после инициализации VM сплэша делает `navigate(TrackerLandingRoute, LyteNavOptions(popUpTo = SplashRoute, popUpToInclusive = true))`.
- **Bottom navigation**: 3 вкладки (`TrackerTabGraph`, `WorkoutTabGraph`, `HistoryTabGraph`) под общим `BottomNavGraph` в `:shared` (входится после сплэша). Переключение — `navController.navigateToTopLevel(tab)` с `saveState`/`restoreState`/`launchSingleTop`, что сохраняет back stack каждой вкладки при переключении. Плавающий док рисуется overlay вне `Scaffold.bottomBar` (см. `App.kt`); корни вкладок сами резервируют место через `LyteBottomNavigationBarHeight`.
- Пока **не реализованы**: deep links, per-screen `Effect` для не-навигационных one-shot (toast/snackbar), адаптивный шелл.
- При проектировании навигации, аргументов, стек-шейпинга и deep-links — skill `kotlin-navigation-compose-multiplatform`.

## Дизайн-система (`:core:core-design`)

- `LyteTheme(darkTheme, content)` настраивает `MaterialTheme.colorScheme/typography/shapes` под токены Lyte (M3 tonal-палитра light/dark, типографика на Space Grotesk, форма) и прокидывает расширенные токены через `CompositionLocal`; доступ к ним — аксессор-object `LyteTheme.{extendedColors,accents,spacing,elevation,numericTypography,extendedShapes,motion}`, по аналогии с `MaterialTheme`. `accents` — шесть акцентов упражнения (`LyteAccent`, дефолт `Slate`), `motion` — длительности и easing'и, из которых берутся все анимации ДС и экранов. Корень UI-дерева в `App()` (`:shared`) оборачивается в `LyteTheme { ... }` один раз.
- Шрифты — Space Grotesk (400/500/600/700, весь UI и цифры) и Inter Tight (700, только вордмарк) — бандлятся в `composeResources/font/` модуля (OFL).
- Иконки — `LyteIcons`, курируемый словарь `ImageVector` поверх `com.composables:icons-lucide-cmp` (KMP-порт Lucide, Android/iOS/JVM/JS/Wasm). Отдельно — пиктограммы движений: `LyteExerciseGlyph` (10 знаков) + `LyteExerciseIcon`, растровый набор Flaticon, перекрашивается тонированием по альфе. Кредит обязателен — `core/core-design/ATTRIBUTION.md`.
- Компонент-кит (`Lyte*`) — на M3-примитивах, где дизайн позволяет (`Button`, `TextField`, `Card`, `AlertDialog`, `ModalBottomSheet`, `TopAppBar`, `FilterChip`, `Switch`), кастомные composable — только там, где нет M3-аналога (`LyteWordmark`, `LyteStepper`, `LyteDiffRow`, `LyteBottomNavigationBar`, `LyteRestTimerOverlay`, `LyteSessionStopwatch`, `LyteEmptyState`, `LyteBadge`, `LyteOverline`, `LyteTopBar` size=Large, а также `component.session`: `LyteSetDots`/`LyteTrackSetRow`/`LyteExerciseSetList`/`LyteExerciseStrip`). Полный список и API — `core/core-design/README.md`.
- Доменный текст (заголовки, сводки) — всегда параметр вызывающей фичи; компонентный «хром» модуля — в его собственном `composeResources/values/strings.xml`.

## БД (`:core:core-db`)

- Room KMP. `LyteDatabase` объявлена как `@Database(entities = [...], exportSchema = true)` + `@ConstructedBy(LyteDatabaseConstructor::class)`; `expect object LyteDatabaseConstructor : RoomDatabaseConstructor<LyteDatabase>` — actual генерируется KSP на каждой платформе (`@Suppress("NO_ACTUAL_FOR_EXPECT")` на expect-объекте, т.к. IDE не видит сгенерированный код до сборки).
- Драйвер — `BundledSQLiteDriver` (androidx.sqlite), `setQueryCoroutineContext(Dispatchers.IO)`, `addMigrations(*LYTE_MIGRATIONS)` — см. `RoomBuilderDefaults.applyLyteDefaults()`. Текущая версия схемы — **2** (v1→v2 добавила маркер: `accent`/`glyph` у `exercise` и `workout`, `program_accent`/`program_glyph` — снапшот в `workout_session`); деструктивного `fallbackToDestructiveMigration` нет — только `Migration`-объекты в `db/migration/` (подробности — `core-db/README.md`).
- **Изменил схему — вместе с ней в том же коммите**: бамп `version`, `Migration`-объект в `LYTE_MIGRATIONS`, новый файл в `core/core-db/schemas/`, тест миграции (эталон — `Migration1To2Test`: БД предыдущей версии собирается по DDL из закоммиченной схемы и мигрируется на настоящей SQLite).
- Билдер БД — expect/actual (`internal/LyteDatabaseBuilder.kt`): на Android контекст берётся через `Koin.GlobalContext` (`androidDatabaseContext()`), на iOS путь — `NSDocumentDirectory` (`iosDatabaseFilePath()`).
- KSP-компилятор Room подключается **per-target**, общего `ksp(...)` нет:
  ```kotlin
  dependencies {
      add("kspAndroid", libs.androidx.room.compiler)
      add("kspIosArm64", libs.androidx.room.compiler)
      add("kspIosSimulatorArm64", libs.androidx.room.compiler)
  }
  ```
- `room { schemaDirectory("$projectDir/schemas") }` обязателен при `exportSchema = true` — без него KSP-таска падает. Схемы коммитятся в репозиторий.
- Сущности/DAO кладём в `core/core-db/src/commonMain/.../db/<domain>/`; фичи получают доступ через DI (`coreDbModule()` регистрирует `LyteDatabase` и DAO), а не создают инстансы БД напрямую.

## Кодстайл (выжимка)

### Нейминг
- Переменные — `camelCase`. Префиксы (`m`, `_` и т.п.) запрещены.
- Классы — `CamelCase`. Префикс `I` у интерфейсов запрещён. Реализация интерфейса — постфикс `Impl` (`class SomeRepositoryImpl : SomeRepository`).
- DTO — с постфиксом `Dto`.
- Никаких сокращений, если они не общеприняты (`description`, а не `desc`).
- Пакеты — в единственном числе (`viewmodel`, не `viewmodels`).

### Код
- Однострочное `if` / `when` не помещается — оборачивай в `{}`.
- Используешь именованные параметры — именуй **все** аргументы, а не только последний.
- Запрещено `nullableFoo?.isFoolable == false`; допустимо только `... == true`.
- Вместо `nullableString ?: ""` — `nullableString.orEmpty()`.
- Тип возвращаемого значения функции указывается явно.
- **Никаких «магических чисел»**: числовой литерал со смыслом выносится в именованную `const`/`val`. Исключение — отступы и размеры в Compose (`padding`, `size`, `dp`-метрики).

### Классы
- Порядок членов: `public` (сначала `override`, потом обычные) → `protected` → `private`.
- **Свойства объявляются до методов.** В классе: свойства → `init` → методы. В файле с топ-level декларациями (Composable-функции и т.п., без обёртывающего класса) — тот же порядок: `val`/`const val` выше, `fun` ниже.
- `companion object` — внизу класса, все константы — в нём.
- Для моделей-сущностей — `data class`.
- **Крупные `data class`/классы-модели — в отдельном файле**, не соседствуют в одном файле с Compose-функцией, которая их использует (пример: `LyteBottomNavItem` — отдельно от `LyteBottomNavigationBar`). Не касается простых `enum class`-селекторов варианта/размера для одной функции (`LyteButtonVariant` и т.п.) — те остаются рядом с функцией.

### Ресурсы
- Иконки — vector drawable в `composeResources/drawable/ic_*.xml`, подключение через `painterResource(Res.drawable.*)`.
- **Исключение — пиктограммы движений** (`core-design/composeResources/drawable/ic_exercise_*.png`): это готовая растровая линейная графика Flaticon, трассировка в вектор ухудшает рисунок. Растр допустим только потому, что решена проблема тёмной темы: чёрный штрих перекрашивается тонированием по альфе (`LyteExerciseIcon` → `ColorFilter.tint`), а не рисуется как есть. Других растровых иконок в проекте быть не должно.
- Все строки — в `composeResources/values/strings.xml`, использование — через `stringResource(Res.string.*)`. Хардкод строк в `@Composable` запрещён.

### UI
- **`modifier: Modifier = Modifier` — всегда последний параметр** `@Composable`-функции (и любой функции, принимающей `Modifier`). Единственное исключение — обязательная замыкающая content-лямбда без значения по умолчанию (слот содержимого, напр. `content` у `LyteBottomSheet`): она остаётся последней ради синтаксиса вызова с trailing-лямбдой, а `modifier` идёт прямо перед ней (как у M3 `ModalBottomSheet`/`Card`). Опциональные слоты со значением по умолчанию (`trailing = null`, `actions = {}`) — обычные необязательные параметры и идут до `modifier`.
- **Корень каждого экрана — `Scaffold`**, даже если `TopBar`/`BottomBar` не нужны.
- Заголовки экранов — `TopAppBar` в слоте `topBar` у `Scaffold`. Кнопка назад — `IconButton` в `navigationIcon`.
- **`@Preview` обязателен** для всех экранов и публичных `@Composable`-компонентов, **и на каждое
  состояние экрана** (каждый арм `UiState`: Loading/Error/Empty/Content). Превью — источник истины
  для скриншот-тестов: состояние без превью не попадает под визуальный контроль.
- Любая длительная операция отображает состояние `loading / content / error`.

## Git Flow

- **`master`** — только релизные срезы. Прямых коммитов нет: в неё приходит merge из `development` в момент релиза, и на этот коммит вешается тег `v<versionName>`. Тег означает «эта версия реально опубликована в сторе», а не «эту версию пробовали залить».
- **`development`** — интеграционная ветка, дефолтная на GitHub. Всё попадает в неё **только через pull request**; на каждом PR гоняется CI (`.github/workflows/ci.yml`).
- Фича-ветки ответвляются от `development` и вливаются обратно PR-ом. **В PR — ровно один коммит**: промежуточные шаги схлопываются перед открытием PR (`git reset --soft origin/development` + один коммит). Единственное исключение — коммиты, которые делает сам CI (например, `chore: обновить эталоны скриншотов` от джобы `screenshots`).
- **Claude не пушит напрямую в `master` или `development`** — только через PR. В `master` Claude не мержит никогда.
- **Мерж в `development` делает `/autopilot`** и только при выполнении всех условий сразу: CI зелёный на текущем head, нет конфликта с базой, ни одного нерешённого blocker от ревью-агентов, нет неразрешённых тредов ревью от человека. Метод — squash (текущая конвенция истории: `RD-17: … (#22)`). Любое невыполненное условие — стоп и вопрос человеку.
- **PR, открытый вне `/autopilot`, мержит человек.** Обычная работа Claude по-прежнему заканчивается открытым PR-ом с зелёным CI.
- **Claude не пишет комментарии в GitHub — ни в PR, ни в тредах ревью, ни в issue.** Замечание ревью закрывается коммитом, а не ответом в треде; всё, что нужно сказать по замечанию (что сделано, какие были компромиссы, почему что-то не сделано), пишется в чат сессии. Тред закрывает и разрешает человек.

## Хуки

В `.claude/hooks/` два `PreToolUse`-хука. Они исполняются харнессом, а не Claude, поэтому в отличие
от правил этого файла обойти их нельзя — оба закрывают правила, которые раньше держались на
дисциплине.

| Хук | Когда срабатывает | Что делает |
|---|---|---|
| `guard-screenshots.sh` | `git commit*` | блокирует коммит, если эталоны `*/screenshots/*.png` изменены — их генерирует только CI |
| `guard-protected-branch.sh` | `git push*` | блокирует прямой push в `master`/`development` — и по имени ветки в команде, и по текущей ветке |

Оба на POSIX sh без `jq` (на macOS его может не быть) и выходят с кодом 2 — это блокирует вызов, а
текст из stderr приходит модели как причина отказа. Мержу PR автопилотом не мешают: тот идёт через
GitHub API, а не пушем.

Перекос сознательно в сторону строгости: команда, которая **цитирует** запретный вызов (тест самого
хука, кусок документации), блокируется наравне с настоящим. Отличить цитату от вызова можно только
полноценным разбором шелла; цена ложного срабатывания — отказ с внятным текстом, цена пропуска —
сломанная защищённая ветка.

## Команда агентов

В `.claude/agents/` — семь субагентов. Шесть только читают (`tools` без Edit/Write); писать код
разрешено одному `implementer`, и тот работает в изолированном worktree. Прочитать CLAUDE.md — первый
шаг любого из них, он важнее общих советов из скиллов.

| Агент | Роль |
|---|---|
| `architect` | границы модулей, api/impl-сплит, MVI-контракт, навигация, DI |
| `reviewer` | кодстайл, корректность, `LyteError`, KMP-совместимость, дизайн-система |
| `tester` | превью на каждый арм `UiState`, покрытие логики, эталоны, миграции Room, состав гейта |
| `acceptance` | приёмка **среза** фичи как демо: проходится ли сценарий целиком, стыки задач, соответствие роадмапу и макету |
| `edge-hunter` | краевые сценарии до написания кода |
| `solution-scout` | вариант решения с ценой и рисками; запускается в нескольких экземплярах |
| `implementer` | **единственный, кто пишет код** — реализует задачу роадмапа в своём worktree и доводит до зелёного гейта |

### Что пишется в файле агента

Файл агента — это **не должностная инструкция и не список правил проекта**. Правила живут в CLAUDE.md,
README модулей и скиллах; агент их читает, а не носит копию — иначе состав модулей пришлось бы править
в семи местах, и агент верил бы своей протухшей копии, а не репозиторию.

В файле агента — то, чего в правилах нет: **чем этот специалист отличается от любого другого**.

| Раздел | Что в нём |
|---|---|
| **Угол** | один вопрос, на который агент отвечает, и чем он **не** занимается. Пересекающиеся описания — главная причина, по которой команда агентов не работает: маршрутизатор отдаёт задачу не тому |
| **Чем ты судишь** | эвристики роли: не «у тебя большая насмотренность», а сами суждения, которые она даёт («общий модуль, который правит один потребитель, — не общий») |
| **Канон** | именованные принципы роли — SOLID и YAGNI у архитектора, FIRST у тестировщика, Кано и JTBD у приёмки. Каждый с операционной формой и границей применимости: голый акроним модель и так знает, польза — в том, когда он **не** применяется. Имя нужно, чтобы находку можно было оспорить по существу |
| **Как ошибается такой специалист** | типовые отказы роли. Аудитору нужен перечень способов ошибиться, а не общее «проверь качество» |
| **Где факты** | ссылки на разделы CLAUDE.md, README и скиллы |
| **Ответ** | формат и требование сжатости: субагент возвращает выжимку, а не свой лог |

Три вещи, проверенные на этом наборе:

- **Правило без причины работает хуже правила с причиной.** «Не смягчай формулировку» — слабее, чем
  «не смягчай, потому что „кажется, возможно, стоило бы“ никому не помогает».
- **Голая личность не помогает, а мешает.** «Ты 10x-инженер» — это токены до первой пользы;
  исследования role prompting показывают, что личность в системном промпте объективные задачи не
  улучшает, а иногда ухудшает. Работает предметная рамка плюс эвристики, а не самоописание.
- **Верная высота — между жёсткой инструкцией и общими словами.** Захардкоженный алгоритм хрупок,
  расплывчатый совет бесполезен; нужны сильные эвристики, по которым агент принимает решение сам.

Лид отдельным агентом не выделен намеренно: у субагента чистый контекст без диалога, поэтому вести
разговор он может только хуже основной сессии. Разработчик выделен, но с оговоркой — `implementer`
берётся только за задачу с **готовой письменной спекой** из роадмапа. Задачу, контекст которой живёт
в диалоге, пишет основная сессия.

Обе команды работают вокруг **роадмапа** — `roadmap/<эпик>/TASKS.md`, один каталог на эпик. Формат
задачи и почему это отдельный каталог (а не `design/`, куда роадмап заезжать не должен, и не `docs/`,
который публикуется на GitHub Pages) — в `roadmap/README.md`.

Две команды:
- `/epic <описание>` — довести крупную задачу до роадмапа: инвентаризация «что уже есть» одним
  агентом → разведка тремя остальными поверх неё → вопросы только по по-настоящему неоднозначному →
  разбивка на задачи размером в PR с полями «Зависимости» и «Затрагивает». Роадмап несёт не только
  задачи: таблицу источников правды, отрицательную инвентаризацию, сквозные решения и обязательную
  замыкающую задачу про выпуск.
- `/autopilot <роадмап>` — провести эпик до серии смерженных PR. Раскладывает задачи на **волны** по
  зависимостям и пересечению файлов, внутри волны реализует параллельно в отдельных worktree (не
  больше трёх разом), затем по одной проводит через ревью, PR, CI и мерж. Отчёт — один, в конце.

## Версионирование

Единственный источник версии — `version.properties` в корне; `androidApp/build.gradle.kts` читает его. `lyte.versionName` — semver, его видит пользователь. `lyte.versionCode` — целое, **монотонно растёт**: стор отклонит загрузку с уже использованным или меньшим значением.

**Claude не меняет версию по собственной инициативе — никогда**, ни при каких правках кода. Бамп делается только по явной команде вида «готовим релиз 1.1.0». При бампе:

1. `lyte.versionCode` += 1 — **всегда**, даже если `versionName` не менялся (перезалив после отказа модерации — это новый versionCode).
2. `lyte.versionName` — по semver.
3. Синхронизировать iOS: в `iosApp/Configuration/Config.xcconfig` `MARKETING_VERSION` = versionName, `CURRENT_PROJECT_VERSION` = versionCode.
4. После успешной публикации в сторе — тег `git tag v<versionName>` на релизном коммите `master`.

## Tech stack

Уже в проекте: Kotlin Multiplatform, Compose Multiplatform, Coroutines/Flow, MVI (`core-mvi`), Jetpack Navigation с типобезопасными `@Serializable` роутами, Koin (DI), Room (локальная БД), дизайн-система (`core-design`, см. раздел выше).

Уже в проекте также: `LyteError` + воронка `handleError` (типизированные ошибки), реактивный SSOT (Room `Flow`), unit-тесты бизнес-логики.

Запланировано (следующие этапы):
- **Сеть**: Ktor Client + единый слой API-контрактов — модуль `core-network` пока не создан.
- **Secure storage**: абстракция над `Keychain` (iOS) / `Keystore` (Android) — для будущих токенов авторизации.
- **UI-тесты** для критичных экранов (`runComposeUiTest` над stateless `*Content`) — ещё не написаны.

## Offline-first паттерн доступа к данным

1. Читаем из локального хранилища (Room), **сразу** показываем.
2. Когда появится сеть — параллельно в фоне дёргаем обновление.
3. Успех — обновляем локальные данные, обновлённое показываем в следующих точках запроса.

## Версии и тулчейн

- Все версии — в `gradle/libs.versions.toml`; добавляешь зависимость — туда, не инлайнить. Алиасы плагинов (`libs.plugins.*`) применяются на уровне модулей; в корневом `build.gradle.kts` декларируются с `apply false`.
- **Kotlin 2.3.21** (запинен < 2.4: для Kotlin 2.4.0 нет релиза KSP — [google/ksp#2965](https://github.com/google/ksp/issues/2965) — а Room требует KSP; связка kotlin 2.3.21 + ksp 2.3.7 + room 2.8.4 проверена). Обновить Kotlin можно будет, когда выйдет совместимый KSP.
- Compose Multiplatform 1.11.1, AGP 9.0.1, Material3 1.11.0-alpha07, AndroidX Lifecycle 2.11.0-beta01, AndroidX Navigation 2.9.2, Koin 4.2.2, Room 2.8.4, kotlinx-datetime 0.7.1 (время — инъектируемый `kotlin.time.Clock`, `Clock.System` только в Koin-модуле).
- JVM target — 11 (и для `:shared`/core/feature Android, и для `:androidApp`). `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`.

## Платформенный код

- `expect`/`actual` — по одному файлу на модуль/назначение (например, `core-db/internal/LyteDatabaseBuilder.kt` с actual в `androidMain`/`iosMain`). Новые платформенные API добавляй так же — не через service locator.
- iOS-вход: `MainViewController()` в `shared/src/iosMain/.../MainViewController.kt` инициализирует Koin (с guard от повторного вызова) и оборачивает `App()` в `ComposeUIViewController`.
- Compose-ресурсы доступны через сгенерированный объект `lyte.shared.generated.resources.Res` (см. `App.kt`, `navigation/LyteBottomBarItem.kt`).
