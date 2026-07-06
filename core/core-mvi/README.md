# core-mvi

Базовая ViewModel для экранов в MVI-стиле. Один `StateFlow` стейта, обработка intent'ов от UI, общий жизненный цикл корутин. Без `Effect` — навигация и любые побочные эффекты идут через явный стейт либо через `Navigator` (`:core:core-navigation`), а не через отдельный one-shot канал.

## Что внутри

- `BaseViewModel<State : UiState, Intent : UiIntent>` — родитель для VM экранов.
- `UiState`, `UiIntent` — пустые маркер-интерфейсы.

## Использование

```kotlin
data class TrackerUiState(val completedWorkoutsToday: Int = 0) : UiState

sealed interface TrackerIntent : UiIntent {
    data object OpenWorkouts : TrackerIntent
}

class TrackerViewModel(
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<TrackerUiState, TrackerIntent>() {

    override fun getInitialState(): TrackerUiState = TrackerUiState()

    override fun onIntent(intent: TrackerIntent) {
        when (intent) {
            TrackerIntent.OpenWorkouts -> lyteNavigator.navigate(WorkoutListRoute)
        }
    }
}
```

В `@Composable`:

```kotlin
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

## Подключение

```kotlin
implementation(projects.core.coreMvi)
```

Модуль использует только `implementation(...)`-зависимости, ничего транзитивно не экспортирует. Потребитель **обязан** подключить сам:

- `libs.androidx.lifecycle.viewmodelCompose` — базовый класс `androidx.lifecycle.ViewModel`, наследник `BaseViewModel`.
- `libs.androidx.lifecycle.runtimeCompose` — `collectAsStateWithLifecycle()`.
- `libs.kotlinx.coroutines.core` — `BaseViewModel` реализует `CoroutineScope`; внутри VM пишешь `launch { ... }`, `Flow.collect`, и т.п.
- `libs.koin.compose.viewmodel` / `libs.koin.compose.viewmodel.navigation` — `koinViewModel()` в Compose.

## Нюансы

- Стейт меняется **только** через `updateState { copy(...) }`; текущее значение — `uiStateValue`.
- Полей во VM не держим — всё в `UiState`. Параметры инициализации (например, `id` из аргумента роута) передаём через конструктор в `getInitialState()`.
- Диспатч интентов — только через ссылку `viewModel::onIntent`, без промежуточных лямбда-пропов на уровне экрана.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
