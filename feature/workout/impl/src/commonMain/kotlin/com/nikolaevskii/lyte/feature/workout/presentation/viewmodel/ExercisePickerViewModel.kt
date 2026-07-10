package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutExerciseRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.ExercisePickerResult
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExercisePickerIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExercisePickerUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Шторка выбора упражнения из библиотеки. ViewModel живёт ровно столько, сколько шторка
 * (`SheetViewModelStoreOwner`), поэтому повторное открытие — это новая ViewModel и свежие данные.
 * По той же причине [initialQuery] приходит снаружи: при возврате из формы создания поисковый запрос
 * восстанавливает владелец.
 *
 * Поиск выполняет БД (`ExerciseDao.search`), поэтому запросы не сыплются на каждый введённый
 * символ — см. [observeQuery].
 *
 * Выбор упражнения и запрос на создание нового не выполняются здесь, а складываются в
 * [ExercisePickerUiState.result]: закрыть шторку и распорядиться результатом — дело владельца.
 */
class ExercisePickerViewModel(
    private val initialQuery: String,
    private val workoutExerciseRepository: WorkoutExerciseRepository,
) : BaseViewModel<ExercisePickerUiState, ExercisePickerIntent>() {

    init {
        observeQuery()
    }

    override fun onIntent(intent: ExercisePickerIntent) {
        when (intent) {
            is ExercisePickerIntent.OnQueryChanged -> updateState { copy(query = intent.query) }
            is ExercisePickerIntent.OnExerciseClicked -> pickExercise(intent.exerciseId)
            ExercisePickerIntent.OnCreateExerciseClicked -> requestExerciseCreation()
        }
    }

    override fun getInitialState(): ExercisePickerUiState =
        ExercisePickerUiState(query = initialQuery)

    /**
     * Источник запросов — сам `uiState`, поэтому отдельного поля с текущим запросом ViewModel не заводит.
     *
     * [debounce] ждёт паузы в наборе: иначе каждый введённый символ уходил бы в БД отдельным запросом.
     * Исключение — [initialQuery]: он ищется сразу, держать спиннер лишние [SEARCH_DEBOUNCE_MILLIS] на
     * открытии шторки незачем. [collectLatest] отменяет запрос, ответ которого уже никому не нужен.
     */
    @OptIn(FlowPreview::class)
    private fun observeQuery() {
        launch {
            uiState
                .map { state -> state.query }
                .distinctUntilChanged()
                .debounce { query -> if (query == initialQuery) NO_DEBOUNCE_MILLIS else SEARCH_DEBOUNCE_MILLIS }
                .collectLatest { query -> loadExercises(query) }
        }
    }

    private suspend fun loadExercises(query: String) {
        val exercises = runCatching { workoutExerciseRepository.getExercises(query) }
        // Запрос мог быть отменён более свежим. Проверяем это до записи в состояние: иначе отмена,
        // пойманная runCatching, доехала бы до UI как ошибка загрузки. Именно currentCoroutineContext():
        // BaseViewModel — сам CoroutineScope, поэтому голый `coroutineContext` дал бы контекст скоупа
        // ViewModel (он всё ещё активен), а не отменённой корутины запроса.
        currentCoroutineContext().ensureActive()
        exercises
            .onSuccess {
                updateState {
                    copy(
                        exercises = it,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                updateState {
                    copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            }
    }

    private fun pickExercise(exerciseId: String) {
        val exercise = uiStateValue.exercises.firstOrNull { it.id == exerciseId } ?: return
        updateState { copy(result = ExercisePickerResult.Picked(exercise)) }
    }

    /** Название формы создания предзаполняем запросом: сюда приходят, когда искомого нет в библиотеке. */
    private fun requestExerciseCreation() {
        updateState { copy(result = ExercisePickerResult.CreationRequested(query.trim())) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val NO_DEBOUNCE_MILLIS = 0L
    }
}
