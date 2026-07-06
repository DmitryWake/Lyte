# core-navigation

DSL-хелперы стек-шейпинга поверх мультиплатформенной Jetpack Navigation Compose плюс `LyteNavigator` — абстракция навигации для ViewModel. Сами Navigation-типы (`NavHost`, `NavController`, `composable<T>`, `toRoute<T>()`) потребитель подключает сам — модуль их не реэкспортирует.

## Что внутри

- `LyteNavigator` — абстракция навигации для ViewModel: VM шлёт команды, шелл (`App()` / `LyteNavHost`) применяет их к `NavController`. Реализация — `LyteNavigatorImpl` (синглтон в DI).
- `coreNavigationModule` (`di/NavigationModule.kt`) — Koin-модуль, регистрирующий `single<LyteNavigator> { LyteNavigatorImpl() }`.
- `NavCommand` — команда навигации: `Forward(route, options)`, `Back`, `SwitchTab(destination)`.
- `LyteNavOptions` — декларативные опции стек-шейпинга (`popUpTo`/`popUpToInclusive`/`saveState`/`launchSingleTop`/`restoreState`), свободные от androidx-типов.
- `NavOptionsBuilder.applyOptions(options)` — трансляция `LyteNavOptions` в `NavOptionsBuilder` (вызывается шеллом).
- `popUpToRoute<R>(inclusive, saveState)` — типобезопасный `popUpTo` по `@Serializable`-роуту.
- `singleTop()` — `launchSingleTop = true`.
- `restorable()` — `restoreState = true`.
- `TopLevelDestination` — контракт верхнеуровневой вкладки bottom-bar (маршрут её вложенного графа).
- `NavController.navigateToTopLevel(destination)` — канонический переход на вкладку с сохранением/восстановлением её back stack.
- `NavDestination?.isTopLevelSelected(destination)` — выбрана ли вкладка (проверка по иерархии).

## Навигация из ViewModel (LyteNavigator)

Навигацию инициирует **ViewModel** через инжектируемый `LyteNavigator`, а не колбэки. `NavController` живёт только в `:shared` (`LyteNavHost`) и остаётся **единственным** подписчиком команд.

Routes лежат в `:feature:<name>:api`:

```kotlin
@Serializable
data object WorkoutListRoute

@Serializable
data class WorkoutDetailsRoute(val id: Long)
```

VM шлёт команды (логика перехода — здесь, рядом с доменными решениями):

```kotlin
class WorkoutListViewModel(
    private val repository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutListUiState, WorkoutListIntent>() {
    override fun onIntent(intent: WorkoutListIntent) {
        when (intent) {
            is WorkoutListIntent.OpenDetails -> lyteNavigator.navigate(WorkoutDetailsRoute(id = intent.id))
        }
    }
}
```

Граф фичи (`:feature:<name>:impl`) — **без навигационных колбэков**:

```kotlin
fun NavGraphBuilder.workoutGraph() {
    composable<WorkoutListRoute> { WorkoutListScreen() }
    composable<WorkoutDetailsRoute> { entry ->
        WorkoutDetailsScreen(id = entry.toRoute<WorkoutDetailsRoute>().id)
    }
}
```

Шелл (`LyteNavHost` в `:shared`) — владелец `NavController` и единственный подписчик команд:

```kotlin
val lyteNavigator: LyteNavigator = koinInject()

LaunchedEffect(navController) {
    lyteNavigator.commands.collect { command ->
        when (command) {
            is NavCommand.Forward -> navController.navigate(command.route) {
                command.options?.let { applyOptions(it) }
            }
            NavCommand.Back -> navController.popBackStack()
            is NavCommand.SwitchTab -> navController.navigateToTopLevel(command.destination)
        }
    }
}
```

Шейпинг стека — из VM через `LyteNavOptions` (без androidx.navigation-типов в VM):

```kotlin
lyteNavigator.navigate(
    route = WorkoutListRoute,
    options = LyteNavOptions(popUpTo = SomeRoute, popUpToInclusive = true),
)
```

`LyteNavigator` регистрируется синглтоном в собственном Koin-модуле этого core-модуля:

```kotlin
val coreNavigationModule = module {
    single<LyteNavigator> { LyteNavigatorImpl() }
}
```

Подключи `coreNavigationModule` в общей точке инициализации Koin (`initKoinShared` в `:shared`).

## Bottom navigation bar (multi-stack)

Каждая вкладка — **вложенный граф** со своим back stack. Вкладка описывается через `TopLevelDestination` (маршрут её графа); метаданные иконки/подписи и сам bottom-bar UI — на стороне `:shared` (`LyteBottomBarItem`, `App()`), здесь только логика стека.

```kotlin
enum class LyteBottomBarItem(override val graphRoute: Any, ...) : TopLevelDestination {
    TRACKER(graphRoute = TrackerTabGraph, ...),
    WORKOUTS(graphRoute = WorkoutTabGraph, ...),
    HISTORY(graphRoute = HistoryTabGraph, ...),
}
```

В `LyteNavHost` (`:shared`) — каждая вкладка во вложенном `navigation<TabGraph>(...)`, выбор и переход через хелперы:

```kotlin
val current = navController.currentBackStackEntryAsState().value?.destination
// selected = current.isTopLevelSelected(tab)
// onClick  = { navController.navigateToTopLevel(tab) }

navigation<BottomNavGraph>(startDestination = TrackerTabGraph) {
    navigation<TrackerTabGraph>(startDestination = TrackerRoute) { trackerGraph() }
    navigation<WorkoutTabGraph>(startDestination = WorkoutListRoute) { workoutGraph() }
    navigation<HistoryTabGraph>(startDestination = HistoryRoute) { historyGraph() }
}
```

`navigateToTopLevel` сворачивает на старт **графа-контейнера вкладок** (родитель графа текущей вкладки, либо корневой граф, если такого родителя нет) с `saveState`, плюс `launchSingleTop` + `restoreState` — стек каждой вкладки сохраняется при переключениях и не дублируется корень.

## Подключение

```kotlin
plugins {
    alias(libs.plugins.kotlinSerialization) // обязательно
}

implementation(projects.core.coreNavigation)
```

Модуль использует только `implementation(...)`-зависимости, ничего транзитивно не экспортирует. Потребитель **обязан** подключить сам:

- `libs.androidx.navigation.compose` — `NavHost`, `NavController`, `rememberNavController`, `composable<T>`, `NavGraphBuilder`, `toRoute<T>`, `navigate(T)`, `popBackStack`.
- `libs.compose.runtime` — `@Composable`.
- `libs.kotlinx.serialization.json` — формат сериализации для `@Serializable`-роутов (плюс плагин `kotlinSerialization` обязательно).
- `libs.koin.compose` — `koinInject()` в шелле, чтобы получить `LyteNavigator` и собрать `commands` (только в `LyteNavHost`).

Сам `LyteNavigator` (интерфейс/команды/опции) тянется транзитивно вместе с модулем — VM зависит только от `implementation(projects.core.coreNavigation)`, без androidx.navigation.

Плагин `kotlinSerialization` и зависимость `kotlinx-serialization-json` нужны в **каждом** модуле с `@Serializable`-роутами и вызовами `composable<T>` / `toRoute<T>` / `navigate(T)` — это `:api`, `:impl`, `:shared`.

## Нюансы

- `NavController` глубже `:shared` не пробрасываем; навигацию инициирует VM через `LyteNavigator`, а графы/экраны навигационных колбэков не принимают.
- Подписчик `LyteNavigator.commands` — **один** (`LyteNavHost` в `:shared`). Несколько подписчиков «разорвут» поток команд (FIFO-канал).
- Аргументы роутов — минимальные (id, enum), не доменные модели. Данные загружает сам экран через VM + Repository.
- Стек-шейпинг — из VM через `LyteNavOptions` либо хелперы (`popUpToRoute`/`singleTop`/`restorable`) в шелле; без сырых `popUpTo`/`launchSingleTop` по местам.
- Кросс-фичевая навигация: VM зависит только от `:feature:<other>:api` (route-цели), не от её `:impl` (см. `TrackerViewModel` → `feature:workout:api`).
- Multi-stack / bottom-bar — через `TopLevelDestination` + `navigateToTopLevel` (или `LyteNavigator.switchTab`).
- Пока не реализованы: deep links, общий per-screen `Effect` для не-навигационных one-shot (toast/snackbar), адаптивный шелл.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
