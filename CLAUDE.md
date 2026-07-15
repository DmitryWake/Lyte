# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Проект

Lyte — фитнес-трекер на Kotlin Multiplatform (Android + iOS), UI шарится через Compose Multiplatform. Корневой пакет: `com.nikolaevskii.lyte`. Имя корневого Gradle-проекта: `Lyte`.

Текущая стадия — **каркас**: выстроен пустой, но полностью «прошитый» по архитектуре скелет (модули, MVI, DI, навигация, БД), плюс готовая дизайн-система (`core-design`). Цель — чтобы новая фича добавлялась без правок инфраструктуры.

## Сборка и запуск

- Android (debug APK): `./gradlew :androidApp:assembleDebug`
- Android (релиз AAB): `./gradlew :androidApp:bundleRelease`. Релиз собирается с R8 + `shrinkResources` (keep-правила — `androidApp/proguard-rules.pro`, обязательны для `@Serializable` route-классов навигации). Подпись — из `keystore.properties` (в `.gitignore`) или env (`LYTE_KEYSTORE_FILE`/`LYTE_KEYSTORE_PASSWORD`/`LYTE_KEY_ALIAS`/`LYTE_KEY_PASSWORD`); без них релиз подписывается debug-ключом. Keystore и пароли в репозиторий не коммитятся.
- iOS: открыть `iosApp/iosApp.xcodeproj` в Xcode и запустить. iOS подключает статический фреймворк `Shared`, который собирает модуль `:shared` (`iosArm64`, `iosSimulatorArm64`); `MainViewController()` из фреймворка оборачивается в `ContentView.swift` через `UIViewControllerRepresentable`.

## Тесты

- Android (host/unit): `./gradlew :core:core-mvi:testAndroidHostTest`, `./gradlew :core:core-db:testAndroidHostTest`, `./gradlew :shared:testAndroidHostTest`.
- iOS симулятор: `./gradlew :shared:iosSimulatorArm64Test`.
- Common-тесты — в `commonTest` каждого модуля, платформенные — в `androidHostTest` / `iosTest`. Запуск одного теста: добавь `--tests "fully.qualified.ClassName.method"` к нужной Gradle-задаче.
- Стратегия: **Unit** — для сложной бизнес-логики (ViewModel-переходы, репозитории, мапперы), **UI-тесты** — для критичных экранов.

## Модули

В `settings.gradle.kts` включён `TYPESAFE_PROJECT_ACCESSORS`, поэтому в скриптах модули указываются как `projects.shared`, `projects.core.coreMvi` и т.д.

- `:androidApp` — Android-хост (`com.android.application`). `LyteApp : Application` инициализирует Koin (`initKoinShared { androidContext(...) }`), `MainActivity` вызывает шаренный `App()`. Namespace и `applicationId` — `com.nikolaevskii.lyte`.
- `:shared` — KMP + Compose Multiplatform модуль-агрегатор. Собирает статический фреймворк `Shared` для iOS и Android-библиотеку (namespace `com.nikolaevskii.lyte.shared`). Использует плагин `com.android.kotlin.multiplatform.library` (AGP 9.x) — конфигурация Android **внутри** блока `kotlin { androidLibrary { … } }`, отдельного top-level `android {}` блока нет. Compose-ресурсы включены. Владеет корневым `NavController`, `App()` (тема + bottom navigation + `LyteNavHost`) и точкой инициализации Koin (`di/KoinInit.kt`).
- `:core:core-mvi` — чистый KMP-модуль (Android + iOS, без Compose UI), база MVI. Namespace `com.nikolaevskii.lyte.core.mvi`.
- `:core:core-navigation` — KMP + Compose, общие нав-хелперы (`popUpToRoute<R>()`, `singleTop()`, `restorable()`), `LyteNavigator`/`LyteNavigatorImpl`, `TopLevelDestination`, собственный Koin-модуль `coreNavigationModule`. Namespace `com.nikolaevskii.lyte.core.navigation`.
- `:core:core-di` — KMP, только инициализация Koin (`initKoin`). Общих синглтонов (Navigator, DispatcherProvider и т.п.) здесь нет — каждый core/feature-модуль владеет своим Koin-модулем и подключает его сам. Namespace `com.nikolaevskii.lyte.core.di`.
- `:core:core-design` — дизайн-система: `LyteTheme` (M3 `colorScheme`/`typography`/`shapes` + расширенные токены — spacing, elevation, числовая типографика, доп. формы) и компонент-кит (`Lyte*`) поверх M3-примитивов. Иконки — `LyteIcons` поверх `com.composables:icons-lucide-cmp`. Namespace `com.nikolaevskii.lyte.core.design`. Подробности и полный список компонентов — в `core/core-design/README.md`.
- `:core:core-db` — Room KMP: `LyteDatabase` (`@ConstructedBy` + `expect object LyteDatabaseConstructor`), `WorkoutEntity`/`WorkoutDao`, expect/actual билдер БД (`androidMain`/`iosMain`), `coreDbModule()`. Namespace `com.nikolaevskii.lyte.core.db`.
- `:feature:tracker:{api,impl}` — главная вкладка (фитнес-трекер, дашборд).
- `:feature:workout:{api,impl}` — тренировки (список + детали). Эталонная фича — демонстрирует полный стек `domain/data/presentation` с Room-репозиторием.
- `:feature:history:{api,impl}` — история (пока экран-заглушка).

Новый модуль регистрируется в `settings.gradle.kts` через `include(":path:to:module")`, подключается типобезопасным акцессором (`implementation(projects.core.coreNavigation)` и т.п.).

### README модулей

У каждого core-модуля (`core/core-*`) есть свой `README.md` с назначением, публичным API, инструкцией по подключению, runnable-примером использования и списком **дополнительных** зависимостей, которые модуль НЕ тянет сам (например, `koin-compose-viewmodel` для `koinViewModel()` или `lifecycle-runtime-compose` для `collectAsStateWithLifecycle()`).

**Правило: при любом изменении исходников core-модуля обязательно открой его `README.md`. Если README перестал отражать актуальный публичный API, поведение или способ подключения — отредактируй его в том же изменении.** Это касается переименований, новых/удалённых публичных типов и функций, изменений сигнатур, перевода зависимостей между `api`/`implementation`, новых обязательных плагинов, переноса Koin-модулей между core-модулями и т.п.

Сейчас правило обязательно только для `:core:*`; по мере появления README у других модулей (`:feature:*` и т.д.) область его действия расширится на них.

### Целевая структура

- **Multi-Module по фичам, api/impl-split**: каждая фича — **два** Gradle-модуля по схеме `feature/<name>/api` и `feature/<name>/impl`.
  - **`:api`** — pure KMP (Android + iOS, **без Compose** и Android-специфики). Содержит `@Serializable` route-классы и доменные контракты (UseCase / Repository интерфейсы, модели). Видим другим фичам — они тянут только `:api`, чтобы навигироваться сюда типобезопасно.
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
- ViewModel-ы экранов наследуются от `BaseViewModel<State : UiState, Intent : UiIntent>`. База предоставляет `uiState: StateFlow<State>`, реализует `CoroutineScope` поверх `viewModelScope + SupervisorJob()`, требует `getInitialState()` и `onIntent(intent)`.
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
- **Bottom navigation**: 3 вкладки (`TrackerTabGraph`, `WorkoutTabGraph`, `HistoryTabGraph`) под общим `BottomNavGraph` в `:shared`. Переключение — `navController.navigateToTopLevel(tab)` с `saveState`/`restoreState`/`launchSingleTop`, что сохраняет back stack каждой вкладки при переключении.
- Пока **не реализованы**: deep links, per-screen `Effect` для не-навигационных one-shot (toast/snackbar), адаптивный шелл.
- При проектировании навигации, аргументов, стек-шейпинга и deep-links — skill `kotlin-navigation-compose-multiplatform`.

## Дизайн-система (`:core:core-design`)

- `LyteTheme(darkTheme, content)` настраивает `MaterialTheme.colorScheme/typography/shapes` под токены Lyte (M3 tonal-палитра light/dark, типографика на Space Grotesk, форма) и прокидывает расширенные токены через `CompositionLocal`; доступ к ним — аксессор-object `LyteTheme.{extendedColors,spacing,elevation,numericTypography,extendedShapes}`, по аналогии с `MaterialTheme`. Корень UI-дерева в `App()` (`:shared`) оборачивается в `LyteTheme { ... }` один раз.
- Шрифты — Space Grotesk (400/500/600/700, весь UI и цифры) и Inter Tight (700, только вордмарк) — бандлятся в `composeResources/font/` модуля (OFL).
- Иконки — `LyteIcons`, курируемый словарь `ImageVector` поверх `com.composables:icons-lucide-cmp` (KMP-порт Lucide, Android/iOS/JVM/JS/Wasm).
- Компонент-кит (`Lyte*`) — на M3-примитивах, где дизайн позволяет (`Button`, `TextField`, `Card`, `AlertDialog`, `ModalBottomSheet`, `TopAppBar`, `FilterChip`, `Switch`), кастомные composable — только там, где нет M3-аналога (`LyteStepper`, `LyteDiffRow`, `LyteBottomNavigationBar`, `LyteRestTimerOverlay`, `LyteSessionStopwatch`, `LyteEmptyState`, `LyteBadge`, `LyteOverline`, `LyteTopBar` size=Large, а также `component.session`: `LyteSetDots`/`LyteSetOverview`/`LyteTrackSetRow`/`LyteExerciseStrip`). Полный список и API — `core/core-design/README.md`.
- Доменный текст (заголовки, сводки) — всегда параметр вызывающей фичи; компонентный «хром» модуля — в его собственном `composeResources/values/strings.xml`.

## БД (`:core:core-db`)

- Room KMP. `LyteDatabase` объявлена как `@Database(entities = [...], exportSchema = true)` + `@ConstructedBy(LyteDatabaseConstructor::class)`; `expect object LyteDatabaseConstructor : RoomDatabaseConstructor<LyteDatabase>` — actual генерируется KSP на каждой платформе (`@Suppress("NO_ACTUAL_FOR_EXPECT")` на expect-объекте, т.к. IDE не видит сгенерированный код до сборки).
- Драйвер — `BundledSQLiteDriver` (androidx.sqlite), `setQueryCoroutineContext(Dispatchers.IO)`, `addMigrations(*LYTE_MIGRATIONS)` — см. `RoomBuilderDefaults.applyLyteDefaults()`. Схема заморожена на `version = 1` (история схлопнута до релиза); деструктивного `fallbackToDestructiveMigration` нет — дальше только `Migration`-объекты (подробности — `core-db/README.md`).
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
- Все строки — в `composeResources/values/strings.xml`, использование — через `stringResource(Res.string.*)`. Хардкод строк в `@Composable` запрещён.

### UI
- **`modifier: Modifier = Modifier` — всегда последний параметр** `@Composable`-функции (и любой функции, принимающей `Modifier`). Единственное исключение — обязательная замыкающая content-лямбда без значения по умолчанию (слот содержимого, напр. `content` у `LyteBottomSheet`): она остаётся последней ради синтаксиса вызова с trailing-лямбдой, а `modifier` идёт прямо перед ней (как у M3 `ModalBottomSheet`/`Card`). Опциональные слоты со значением по умолчанию (`trailing = null`, `actions = {}`) — обычные необязательные параметры и идут до `modifier`.
- **Корень каждого экрана — `Scaffold`**, даже если `TopBar`/`BottomBar` не нужны.
- Заголовки экранов — `TopAppBar` в слоте `topBar` у `Scaffold`. Кнопка назад — `IconButton` в `navigationIcon`.
- **`@Preview` обязателен** для всех экранов и публичных `@Composable`-компонентов.
- Любая длительная операция отображает состояние `loading / content / error`.

## Git Flow

- `main` — релизные изменения.
- `development` — текущая разработка.
- `feature/<task>` — отведена от `development` под конкретную задачу; по завершении — один коммит (squash), rebase перед PR.

## Tech stack

Уже в проекте: Kotlin Multiplatform, Compose Multiplatform, Coroutines/Flow, MVI (`core-mvi`), Jetpack Navigation с типобезопасными `@Serializable` роутами, Koin (DI), Room (локальная БД), дизайн-система (`core-design`, см. раздел выше).

Запланировано (следующие этапы):
- **Сеть**: Ktor Client + единый слой API-контрактов — модуль `core-network` пока не создан.
- **Secure storage**: абстракция над `Keychain` (iOS) / `Keystore` (Android) — для будущих токенов авторизации.
- **Тесты**: Unit — для сложной бизнес-логики; UI — для критичных экранов.

## Offline-first паттерн доступа к данным

1. Читаем из локального хранилища (Room), **сразу** показываем.
2. Когда появится сеть — параллельно в фоне дёргаем обновление.
3. Успех — обновляем локальные данные, обновлённое показываем в следующих точках запроса.

## Версии и тулчейн

- Все версии — в `gradle/libs.versions.toml`; добавляешь зависимость — туда, не инлайнить. Алиасы плагинов (`libs.plugins.*`) применяются на уровне модулей; в корневом `build.gradle.kts` декларируются с `apply false`.
- **Kotlin 2.3.21** (запинен < 2.4: для Kotlin 2.4.0 нет релиза KSP — [google/ksp#2965](https://github.com/google/ksp/issues/2965) — а Room требует KSP; связка kotlin 2.3.21 + ksp 2.3.7 + room 2.8.4 проверена). Обновить Kotlin можно будет, когда выйдет совместимый KSP.
- Compose Multiplatform 1.11.1, AGP 9.0.1, Material3 1.11.0-alpha07, AndroidX Lifecycle 2.11.0-beta01, AndroidX Navigation 2.9.2, Koin 4.2.1, Room 2.8.4.
- JVM target — 11 (и для `:shared`/core/feature Android, и для `:androidApp`). `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`.

## Платформенный код

- `expect`/`actual` — по одному файлу на модуль/назначение (например, `core-db/internal/LyteDatabaseBuilder.kt` с actual в `androidMain`/`iosMain`). Новые платформенные API добавляй так же — не через service locator.
- iOS-вход: `MainViewController()` в `shared/src/iosMain/.../MainViewController.kt` инициализирует Koin (с guard от повторного вызова) и оборачивает `App()` в `ComposeUIViewController`.
- Compose-ресурсы доступны через сгенерированный объект `lyte.shared.generated.resources.Res` (см. `App.kt`, `navigation/LyteBottomBarItem.kt`).
