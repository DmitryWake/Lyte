package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.ExercisePickerResult
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExercisePickerIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisePickerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsLoadingThenExposesWholeLibraryWithoutWaitingForDebounce() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = ExercisePickerViewModel(initialQuery = "", workoutExerciseRepository = repository)

        assertTrue(viewModel.uiState.value.isLoading)

        // Начальный запрос не ждёт паузы в наборе — иначе шторка открывалась бы со спиннером.
        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Жим лёжа", "Жим стоя", "Приседания со штангой"), state.exercises.map { it.name })
        assertNull(state.errorMessage)
        assertNull(state.result)
        assertEquals(listOf(""), repository.queries)
    }

    @Test
    fun loadFailureSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository().apply { getExercisesError = IllegalStateException("no library") }
        val viewModel = ExercisePickerViewModel(initialQuery = "", workoutExerciseRepository = repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("no library", state.errorMessage)
    }

    @Test
    fun initialQueryIsPassedToRepository() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = ExercisePickerViewModel(initialQuery = "жим", workoutExerciseRepository = repository)

        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("жим", state.query)
        assertEquals(listOf("Жим лёжа", "Жим стоя"), state.exercises.map { it.name })
        assertEquals(listOf("жим"), repository.queries)
    }

    @Test
    fun queryIsAppliedToStateImmediatelyButSearchWaitsForPause() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = loadedPicker(repository)

        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))

        // Поле ввода отзывчиво сразу, а запрос ещё не ушёл.
        assertEquals("жим", viewModel.uiState.value.query)
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertEquals(listOf(""), repository.queries)

        advanceTimeBy(1)
        runCurrent()

        assertEquals(listOf("", "жим"), repository.queries)
        assertEquals(listOf("Жим лёжа", "Жим стоя"), viewModel.uiState.value.exercises.map { it.name })
    }

    @Test
    fun typingWithoutPausesIssuesSingleQuery() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = loadedPicker(repository)

        // Быстрый набор «ж» → «жи» → «жим»: в БД должен уйти только последний запрос.
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("ж"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS / 2)
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жи"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS / 2)
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))
        advanceUntilIdle()

        assertEquals(listOf("", "жим"), repository.queries)
    }

    @Test
    fun repeatedQueryDoesNotIssueAnotherSearch() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = loadedPicker(repository)

        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))
        advanceUntilIdle()
        // Тот же запрос: набрали лишний символ и стёрли его.
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жимм"))
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))
        advanceUntilIdle()

        assertEquals(listOf("", "жим"), repository.queries)
    }

    @Test
    fun changeQueryWithoutMatchesLeavesListEmpty() = runTest(testDispatcher) {
        val viewModel = loadedPicker()

        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("Жим Арнольда"))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.exercises.isEmpty())
    }

    @Test
    fun blankQueryRestoresWholeLibrary() = runTest(testDispatcher) {
        val viewModel = loadedPicker()
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))
        advanceUntilIdle()

        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged(""))
        advanceUntilIdle()

        assertEquals(library.size, viewModel.uiState.value.exercises.size)
    }

    @Test
    fun staleResponseDoesNotOverwriteNewerQuery() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = loadedPicker(repository)

        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))
        advanceUntilIdle()
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("приседания"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("приседания", state.query)
        assertEquals(listOf("Приседания со штангой"), state.exercises.map { it.name })
        assertNull(state.errorMessage)
    }

    @Test
    fun cancelledSearchDoesNotSurfaceAsLoadingError() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository(initialExercises = library)
        val viewModel = loadedPicker(repository)
        // Ответ дольше, чем пауза debounce: следующий запрос отменит предыдущий прямо «в полёте».
        repository.getExercisesDelayMillis = RESPONSE_DELAY_MILLIS

        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("жим"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS + RESPONSE_DELAY_MILLIS / 2)
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("приседания"))
        advanceTimeBy(SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        // Запрос «жим» отменён. runCatching ловит и CancellationException, поэтому без явной
        // проверки активности отмена доехала бы до UI как ошибка загрузки.
        assertNull(viewModel.uiState.value.errorMessage)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.errorMessage)
        assertEquals(listOf("Приседания со штангой"), state.exercises.map { it.name })
    }

    @Test
    fun pickExerciseExposesPickedResult() = runTest(testDispatcher) {
        val viewModel = loadedPicker()

        viewModel.onIntent(ExercisePickerIntent.OnExerciseClicked(exerciseId = "l2"))

        assertEquals(ExercisePickerResult.Picked(library[1]), viewModel.uiState.value.result)
    }

    @Test
    fun pickExerciseWithUnknownIdIsNoOp() = runTest(testDispatcher) {
        val viewModel = loadedPicker()

        viewModel.onIntent(ExercisePickerIntent.OnExerciseClicked(exerciseId = "unknown"))

        assertNull(viewModel.uiState.value.result)
    }

    @Test
    fun requestExerciseCreationCarriesTrimmedQueryAsInitialName() = runTest(testDispatcher) {
        val viewModel = loadedPicker()
        viewModel.onIntent(ExercisePickerIntent.OnQueryChanged("  Жим Арнольда "))
        advanceUntilIdle()

        viewModel.onIntent(ExercisePickerIntent.OnCreateExerciseClicked)

        assertEquals(ExercisePickerResult.CreationRequested("Жим Арнольда"), viewModel.uiState.value.result)
    }

    private fun TestScope.loadedPicker(
        repository: FakeWorkoutExerciseRepository = FakeWorkoutExerciseRepository(initialExercises = library),
    ): ExercisePickerViewModel {
        val viewModel = ExercisePickerViewModel(initialQuery = "", workoutExerciseRepository = repository)
        runCurrent()
        return viewModel
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 300L
        const val RESPONSE_DELAY_MILLIS = 1_000L

        val library = listOf(
            WorkoutExerciseEntity(id = "l1", name = "Приседания со штангой", description = "Присед до параллели."),
            WorkoutExerciseEntity(id = "l2", name = "Жим лёжа", description = "Жим штанги от середины груди."),
            WorkoutExerciseEntity(id = "l3", name = "Жим стоя", description = null),
        )
    }
}
