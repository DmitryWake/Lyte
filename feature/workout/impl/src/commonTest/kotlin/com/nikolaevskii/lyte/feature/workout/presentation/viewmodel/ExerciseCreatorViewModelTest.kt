package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.ExerciseCreatorUiState.ExerciseCreatorContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class ExerciseCreatorViewModelTest {

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
    fun startsWithGeneratedIdAndPrefilledName() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialName = "Жим Арнольда")

        val state = viewModel.uiState.value
        assertTrue(state.exercise.id.isNotBlank())
        assertEquals("Жим Арнольда", state.exercise.name)
        assertNull(state.exercise.description)
        assertTrue(state.isSubmitEnabled)
        assertFalse(state.isSaving)
        assertFalse(state.isCreated)
    }

    @Test
    fun blankNameDisablesSubmit() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialName = "   ")

        assertFalse(viewModel.uiState.value.isSubmitEnabled)

        viewModel.onIntent(ExerciseCreatorIntent.OnNameChanged("Жим"))
        assertTrue(viewModel.uiState.value.isSubmitEnabled)

        viewModel.onIntent(ExerciseCreatorIntent.OnNameChanged(" "))
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    @Test
    fun startsWithDefaultMark() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialName = "")

        assertEquals(ExerciseAccent.Default, viewModel.uiState.value.exercise.accent)
        assertEquals(ExerciseGlyph.Default, viewModel.uiState.value.exercise.glyph)
    }

    /** Маркер к имени отношения не имеет, поэтому выбор цвета не должен трогать доступность сабмита. */
    @Test
    fun changeMarkUpdatesDraftWithoutTouchingSubmit() = runTest(testDispatcher) {
        val viewModel = createViewModel(initialName = "")
        assertFalse(viewModel.uiState.value.isSubmitEnabled)

        viewModel.onIntent(ExerciseCreatorIntent.OnAccentChanged(ExerciseAccent.Teal))
        viewModel.onIntent(ExerciseCreatorIntent.OnGlyphChanged(ExerciseGlyph.DumbbellPress))

        assertEquals(ExerciseAccent.Teal, viewModel.uiState.value.exercise.accent)
        assertEquals(ExerciseGlyph.DumbbellPress, viewModel.uiState.value.exercise.glyph)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
    }

    /** Выбор цвета не исправляет неудачную запись, поэтому баннер ошибки он гасить не должен. */
    @Test
    fun changeMarkAfterFailureKeepsErrorBanner() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository().apply { createExerciseError = IllegalStateException("boom") }
        val viewModel = createViewModel(initialName = "Жим стоя", workoutExerciseRepository = repository)
        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()
        assertTrue(viewModel.uiState.value.isError)

        viewModel.onIntent(ExerciseCreatorIntent.OnAccentChanged(ExerciseAccent.Coral))

        assertTrue(viewModel.uiState.value.isError)
        assertEquals(ExerciseAccent.Coral, viewModel.uiState.value.exercise.accent)
    }

    @Test
    fun submitWritesChosenMarkToLibrary() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository()
        val viewModel = createViewModel(initialName = "Жим гантелей", workoutExerciseRepository = repository)
        viewModel.onIntent(ExerciseCreatorIntent.OnAccentChanged(ExerciseAccent.Teal))
        viewModel.onIntent(ExerciseCreatorIntent.OnGlyphChanged(ExerciseGlyph.DumbbellPress))

        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        val created = repository.createdExercises.single()
        assertEquals(ExerciseAccent.Teal, created.accent)
        assertEquals(ExerciseGlyph.DumbbellPress, created.glyph)
        // Наружу владельцу отдаётся ровно то, что легло в библиотеку.
        assertEquals(created, viewModel.uiState.value.exercise)
    }

    @Test
    fun submitWithBlankNameIsNoOp() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository()
        val viewModel = createViewModel(initialName = "   ", workoutExerciseRepository = repository)

        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        assertTrue(repository.createdExercises.isEmpty())
        assertFalse(viewModel.uiState.value.isCreated)
    }

    @Test
    fun submitSavesTrimmedExerciseAndKeepsItInState() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository()
        val viewModel = createViewModel(initialName = "", workoutExerciseRepository = repository)
        val generatedId = viewModel.uiState.value.exercise.id
        viewModel.onIntent(ExerciseCreatorIntent.OnNameChanged("  Жим Арнольда "))
        viewModel.onIntent(ExerciseCreatorIntent.OnDescriptionChanged(" Гантели, сидя. "))

        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        val created = repository.createdExercises.single()
        assertEquals(generatedId, created.id)
        assertEquals("Жим Арнольда", created.name)
        assertEquals("Гантели, сидя.", created.description)

        val state = viewModel.uiState.value
        // Наружу уходит state.exercise, поэтому он обязан совпасть с записанным в библиотеку.
        assertEquals(created, state.exercise)
        assertTrue(state.isCreated)
        assertFalse(state.isSaving)
    }

    @Test
    fun submitStoresBlankDescriptionAsNull() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository()
        val viewModel = createViewModel(initialName = "Жим Арнольда", workoutExerciseRepository = repository)
        viewModel.onIntent(ExerciseCreatorIntent.OnDescriptionChanged("  "))

        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        assertNull(repository.createdExercises.single().description)
        assertNull(viewModel.uiState.value.exercise.description)
    }

    @Test
    fun submitTwiceCreatesExerciseOnlyOnce() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository()
        val viewModel = createViewModel(initialName = "Жим Арнольда", workoutExerciseRepository = repository)

        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        assertFalse(viewModel.uiState.value.isSubmitEnabled)
        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        assertEquals(1, repository.createdExercises.size)
    }

    @Test
    fun submitFailureKeepsFormSurfacesErrorAndReenablesSubmit() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository().apply { createExerciseError = IllegalStateException("write failed") }
        val viewModel = createViewModel(initialName = "Жим Арнольда", workoutExerciseRepository = repository)

        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isError)
        assertFalse(state.isSaving)
        assertFalse(state.isCreated)
        assertTrue(state.isSubmitEnabled)
        assertEquals("Жим Арнольда", state.exercise.name)
    }

    @Test
    fun retryAfterFailureSucceeds() = runTest(testDispatcher) {
        val repository = FakeWorkoutExerciseRepository().apply { createExerciseError = IllegalStateException("write failed") }
        val viewModel = createViewModel(initialName = "Жим Арнольда", workoutExerciseRepository = repository)
        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        repository.createExerciseError = null
        viewModel.onIntent(ExerciseCreatorIntent.OnCreateClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isCreated)
        assertFalse(state.isError)
        assertEquals(state.exercise, repository.createdExercises.single())
    }

    private fun createViewModel(
        initialName: String,
        workoutExerciseRepository: FakeWorkoutExerciseRepository = FakeWorkoutExerciseRepository(),
    ): ExerciseCreatorViewModel = ExerciseCreatorViewModel(
        initialName = initialName,
        workoutExerciseRepository = workoutExerciseRepository,
    )
}

private val ExerciseCreatorUiState.isSubmitEnabled: Boolean
    get() = (content as? ExerciseCreatorContent.Editing)?.isSubmitEnabled == true

private val ExerciseCreatorUiState.isSaving: Boolean
    get() = content is ExerciseCreatorContent.Saving

private val ExerciseCreatorUiState.isCreated: Boolean
    get() = content is ExerciseCreatorContent.Created

private val ExerciseCreatorUiState.isError: Boolean
    get() = (content as? ExerciseCreatorContent.Editing)?.error != null
