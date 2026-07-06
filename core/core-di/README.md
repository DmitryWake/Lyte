# core-di

Минимальная обёртка над Koin: единственная точка старта DI-графа для обеих платформ.

## Что внутри

- `initKoin(appDeclaration)` — тонкая обёртка над `startKoin { ... }`.

Никаких общих Koin-модулей этот модуль не декларирует — каждый core/feature-модуль владеет своим Koin-модулем и подключает его сам (например, `coreNavigationModule` в `:core:core-navigation`, `coreDbModule()` в `:core:core-db`, `featureWorkoutModule` в `:feature:workout:impl`).

## Использование

Единая точка инициализации собирается в `:shared` (`di/KoinInit.kt`) — она перечисляет все модули, которые нужно поднять:

```kotlin
fun initKoinShared(
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication =
    initKoin {
        appDeclaration()
        modules(
            coreDbModule(),
            coreNavigationModule,
            featureTrackerModule,
            featureWorkoutModule,
            featureHistoryModule,
        )
    }
```

Bootstrap платформ:

```kotlin
// Android
class LyteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinShared { androidContext(this@LyteApp) }
    }
}

// iOS — initKoinShared() вызывается один раз перед стартом Compose UI (см. MainViewController.kt)
```

VM в `@Composable`:

```kotlin
@Composable
fun WorkoutListScreen(viewModel: WorkoutListViewModel = koinViewModel()) { /* ... */ }

@Composable
fun WorkoutDetailsScreen(
    id: Long,
    viewModel: WorkoutDetailsViewModel = koinViewModel { parametersOf(id) },
) { /* ... */ }
```

## Подключение

```kotlin
implementation(projects.core.coreDi)
```

Модуль использует только `implementation(...)`-зависимости, ничего транзитивно не экспортирует. Потребитель **обязан** подключить сам:

- `libs.koin.core` — `Module`, `module { ... }`, `single { ... }`, `factory { ... }`, типы `KoinApplication`/`KoinAppDeclaration`.
- `libs.koin.core.viewmodel` — `viewModel { ... }`, `viewModelOf(...)` в Koin-модулях.
- `libs.kotlinx.coroutines.core` — при необходимости корутин внутри сервисов, объявленных в Koin-модулях.
- `libs.koin.android` (только `androidMain`) — `androidContext(...)` в Android-bootstrap.
- `libs.koin.compose` — `koinInject()`, `get()` в `@Composable`.
- `libs.koin.compose.viewmodel` / `libs.koin.compose.viewmodel.navigation` — `koinViewModel()` в экранах.

## Нюансы

- `initKoin` вызывается **строго один раз на процесс** (см. guard `koinStarted` в `shared/src/iosMain/.../MainViewController.kt`). Динамические модули — через `koin.loadModules(...)`.
- Koin-модули фич и core-модулей конфигурируются рядом с их исходниками и подключаются в `initKoinShared` (`:shared`). `startKoin` из самих фич/core-модулей **не** вызываем.
- Модуль не хранит общих синглтонов (Navigator, DispatcherProvider и т.п.) — это ответственность модуля, которому синглтон принадлежит логически (пример: `LyteNavigator` — в `:core:core-navigation`).

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
