# core-navigation

DSL-хелперы стек-шейпинга поверх мультиплатформенной Jetpack Navigation Compose плюс `LyteNavigator` — абстракция навигации для ViewModel. Сами Navigation-типы (`NavHost`, `NavController`, `composable<T>`, `toRoute<T>()`) потребитель подключает сам — модуль их не реэкспортирует.

## Что внутри

- `LyteNavigator` — абстракция навигации для ViewModel: VM шлёт команды, шелл (`App()` / `LyteNavHost`) применяет их к `NavController`. Реализация — `LyteNavigatorImpl` (синглтон в DI).
- `coreNavigationModule` (`di/NavigationModule.kt`) — Koin-модуль, регистрирующий `single<LyteNavigator> { LyteNavigatorImpl() }`.
- `NavCommand` — команда навигации: `Forward(route, options)`, `Back`, `SwitchTab(graphRoute)`.
- `LyteNavOptions` — декларативные опции стек-шейпинга (`popUpTo`/`popUpToInclusive`/`saveState`/`launchSingleTop`/`restoreState`), свободные от androidx-типов.
- `NavOptionsBuilder.applyOptions(options)` — трансляция `LyteNavOptions` в `NavOptionsBuilder` (вызывается шеллом).
- `popUpToRoute<R>(inclusive, saveState)` — типобезопасный `popUpTo` по `@Serializable`-роуту.
- `singleTop()` — `launchSingleTop = true`.
- `restorable()` — `restoreState = true`.
- `TopLevelDestination` — контракт верхнеуровневой вкладки bottom-bar (маршрут её вложенного графа).
- `NavController.navigateToTopLevel(graphRoute)` — канонический переход на вкладку с сохранением/восстановлением её back stack; перегрузка `navigateToTopLevel(destination: TopLevelDestination)` делегирует в неё.
- `NavDestination?.isTopLevelSelected(destination)` — выбрана ли вкладка: `true` только на её стартовом экране, не на любом экране внутри графа вкладки.

## Навигация из ViewModel (LyteNavigator)

Навигацию инициирует **ViewModel** через инжектируемый `LyteNavigator`, а не колбэки. `NavController` живёт только в `:shared` (`LyteNavHost`) и остаётся **единственным** подписчиком команд.

Routes лежат в `:feature:<name>:api`:

```kotlin
@Serializable
data object WorkoutListRoute

@Serializable
data class WorkoutDetailsRoute(val id: String? = null)
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
            is NavCommand.SwitchTab -> navController.navigateToTopLevel(command.graphRoute)
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

Маршруты графов вкладок (`TrackerTabGraph`, `WorkoutTabGraph`, `HistoryTabGraph`) лежат в соответствующих `:feature:<name>:api` — тогда фича может переключиться на чужую вкладку через `switchTab`, не завися от `:shared`. В `:shared` остаётся только граф-контейнер `BottomNavGraph`.

```kotlin
enum class LyteBottomBarItem(override val graphRoute: Any, ...) : TopLevelDestination {
    TRACKER(graphRoute = TrackerTabGraph, ...),
    WORKOUTS(graphRoute = WorkoutTabGraph, ...),
    HISTORY(graphRoute = HistoryTabGraph, ...),
}
```

Переход на другую вкладку **из VM** — только `switchTab` с маршрутом её графа:

```kotlin
// :feature:tracker:impl зависит от :feature:workout:api, поэтому видит WorkoutTabGraph
lyteNavigator.switchTab(WorkoutTabGraph)
```

В `LyteNavHost` (`:shared`) — каждая вкладка во вложенном `navigation<TabGraph>(...)`, выбор и переход через хелперы:

```kotlin
val current = navController.currentBackStackEntryAsState().value?.destination
// selected = current.isTopLevelSelected(tab)
// onClick  = { navController.navigateToTopLevel(tab) }

navigation<BottomNavGraph>(startDestination = TrackerTabGraph) {
    navigation<TrackerTabGraph>(startDestination = TrackerLandingRoute) { trackerGraph() }
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
- Кросс-фичевая навигация: VM зависит только от `:feature:<other>:api` (route-цели), не от её `:impl` (см. `WorkoutPickerViewModel` в `:feature:tracker:impl` → `feature:workout:api`).
- Multi-stack / bottom-bar — через `TopLevelDestination` + `navigateToTopLevel` (или `LyteNavigator.switchTab`).
- **Между вкладками нельзя ходить обычным `navigate()`.** `navigate(WorkoutListRoute)` из вкладки «Трекер» кладёт граф вкладки «Тренировки» *поверх* трекера (Navigation достраивает недостающий entry её графа). Следующий `navigateToTopLevel` делает не-inclusive `popUpTo(saveState = true)` до старта графа-контейнера, а Navigation при этом привязывает сохранённый стек к цели `popUpTo` и всем её предкам по цепочке start-destination — то есть к `TrackerTabGraph`. Идущий следом `restoreState` восстанавливает по этому ключу стек **тренировок**, и вкладка «Трекер» перестаёт открываться, пока не нажмёшь системный «назад». Правильный переход — `switchTab(WorkoutTabGraph)`.
- Уходя с не-стартового экрана вкладки на другую вкладку, сначала `back()`, потом `switchTab(...)`: иначе `saveState` сохранит вкладку вместе с этим экраном, и при возврате восстановится он, а не корень вкладки.
- `isTopLevelSelected` сверяет id текущего назначения со стартовым назначением графа вкладки, а не просто «внутри графа ли» — иначе detail-экраны вкладки (пушнутые поверх её списка) наследовали бы bottom-bar.
- Пока не реализованы: deep links, общий per-screen `Effect` для не-навигационных one-shot (toast/snackbar), адаптивный шелл.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
