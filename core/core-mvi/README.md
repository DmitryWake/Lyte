# core-mvi

Базовая ViewModel для экранов в MVI-стиле. Один `StateFlow` стейта, обработка intent'ов от UI, общий жизненный цикл корутин. Без `Effect` — навигация и любые побочные эффекты идут через явный стейт либо через `Navigator` (`:core:core-navigation`), а не через отдельный one-shot канал.

## Что внутри

- `BaseViewModel<State : UiState, Intent : UiIntent>` — родитель для VM экранов.
- `UiState`, `UiIntent` — пустые маркер-интерфейсы.
- `LyteError` — типизированная ошибка презентации (`NotFound` / `Storage` / `Unknown(cause)`); `Throwable.toLyteError()` — нормализация. Маркер-исключения `LyteNotFoundException` / `LyteStorageException` — их бросают репозиторий/VM, чтобы получить нужный арм (сырой `Throwable.message` наружу не показываем).

## Использование

```kotlin
data class WorkoutPickerUiState(val programs: List<WorkoutItemEntity> = emptyList()) : UiState

sealed interface WorkoutPickerIntent : UiIntent {
    data class OnProgramClicked(val id: String) : WorkoutPickerIntent
}

class WorkoutPickerViewModel(
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutPickerUiState, WorkoutPickerIntent>() {

    override fun getInitialState(): WorkoutPickerUiState = WorkoutPickerUiState()

    override fun onIntent(intent: WorkoutPickerIntent) {
        when (intent) {
            is WorkoutPickerIntent.OnProgramClicked -> lyteNavigator.navigate(WorkoutDetailsRoute(id = intent.id))
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
- **Скоуп корутин** — `coroutineContext = viewModelScope.coroutineContext + CoroutineExceptionHandler`. Дополнительный `+ SupervisorJob()` **не добавляем**: он заменил бы `Job`, привязанный к `onCleared()`, и корутины пережили бы очистку VM. У `CoroutineExceptionHandler` свой `Key`, поэтому он `Job` не заменяет.
- **Ошибки** — любой непойманный сбой корутины на скоупе VM (`launch { … }`) приходит в `protected open fun handleError(error)`. `CancellationException` туда по контракту не попадает (отмена скоупа ошибкой не считается — это централизованно снимает «`runCatching` глотает cancellation»). Наследник переопределяет `handleError` и переводит стейт в Error-арм: `updateState { X.Error(error.toLyteError()) }`. Экран с двумя разными исходами провала (загрузка vs сохранение) ловит конкретную операцию сам, а в `handleError` пускает только неожиданное.

> При изменении исходников модуля проверь и при необходимости обнови этот README в том же коммите.
