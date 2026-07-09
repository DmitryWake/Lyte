package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutDetailsViewModelTest {

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
    fun createModeStartsWithGeneratedIdBlankFormAndDoesNotLoad() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val viewModel = WorkoutDetailsViewModel(initialId = null, workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.id.isNotBlank())
        assertEquals("", state.name)
        assertTrue(state.exercises.isEmpty())
        assertFalse(state.isLoading)
        assertTrue(repository.getWorkoutCalls.isEmpty())
    }

    @Test
    fun editModeLoadsWorkoutIntoState() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                name = "Push Day",
                description = "Chest & triceps",
                exercises = listOf(exercise("e1", "Bench Press", 8 to 70.0), exercise("e2", "Dips", 12 to null)),
            )
        }
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("w1", state.id)
        assertEquals("Push Day", state.name)
        assertEquals("Chest & triceps", state.description)
        assertEquals(listOf("Bench Press", "Dips"), state.exercises.map { it.exercise.exercise.name })
        assertFalse(state.isLoading)
        assertEquals(listOf("w1"), repository.getWorkoutCalls)
    }

    @Test
    fun editModeMissingWorkoutSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val viewModel = WorkoutDetailsViewModel(initialId = "missing", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun editModeLoadFailureSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutError = IllegalStateException("boom") }
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        assertEquals("boom", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun changeNameUpdatesState() = runTest(testDispatcher) {
        val viewModel = WorkoutDetailsViewModel(initialId = null, workoutRepository = FakeWorkoutRepository(), lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.ChangeName("Leg Day"))

        assertEquals("Leg Day", viewModel.uiState.value.name)
    }

    @Test
    fun moveExerciseReordersList() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0), exercise("e3", "Row", 10 to 60.0)),
            )
        }
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.MoveExercise(fromIndex = 0, toIndex = 2))

        assertEquals(listOf("Bench", "Row", "Squat"), viewModel.uiState.value.exercises.map { it.exercise.exercise.name })
    }

    @Test
    fun moveExerciseWithOutOfBoundsIndexIsNoOp() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0)))
        }
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.MoveExercise(fromIndex = 5, toIndex = 0))

        assertEquals(listOf("Squat", "Bench"), viewModel.uiState.value.exercises.map { it.exercise.exercise.name })
    }

    @Test
    fun removeExerciseRemovesOnlyTargetedItem() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(
                id = "w1",
                exercises = listOf(exercise("e1", "Squat", 8 to 90.0), exercise("e2", "Bench", 8 to 70.0), exercise("e3", "Row", 10 to 60.0)),
            )
        }
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.RemoveExercise(index = 1))

        assertEquals(listOf("Squat", "Row"), viewModel.uiState.value.exercises.map { it.exercise.exercise.name })
    }

    @Test
    fun addExerciseAndEditExerciseSetsAreStubsAndDoNotChangeState() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", exercises = listOf(exercise("e1", "Squat", 8 to 90.0)))
        }
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(WorkoutDetailsIntent.AddExercise)
        viewModel.onIntent(WorkoutDetailsIntent.EditExerciseSets(index = 0))

        assertEquals(stateBefore, viewModel.uiState.value)
    }

    @Test
    fun saveInCreateModeCallsCreateWorkoutAndNavigatesBack() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutDetailsViewModel(initialId = null, workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        val generatedId = viewModel.uiState.value.id
        viewModel.onIntent(WorkoutDetailsIntent.ChangeName("New Program"))

        viewModel.onIntent(WorkoutDetailsIntent.Save)
        runCurrent()

        assertEquals(listOf(WorkoutEntity(id = generatedId, name = "New Program", description = null, exercises = emptyList())), repository.createdWorkouts)
        assertTrue(repository.editedWorkouts.isEmpty())
        assertEquals(1, navigator.backCallCount)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun saveInEditModePreservesDescriptionAndCallsEditWorkout() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply {
            workoutToReturn = workout(id = "w1", name = "Push Day", description = "Original description", exercises = listOf(exercise("e1", "Squat", 8 to 90.0)))
        }
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutDetailsViewModel(initialId = "w1", workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.ChangeName("Renamed"))

        viewModel.onIntent(WorkoutDetailsIntent.Save)
        runCurrent()

        val saved = repository.editedWorkouts.single()
        assertEquals("w1", saved.id)
        assertEquals("Renamed", saved.name)
        assertEquals("Original description", saved.description)
        assertTrue(repository.createdWorkouts.isEmpty())
        assertEquals(1, navigator.backCallCount)
    }

    @Test
    fun saveFailureKeepsFormAndSurfacesErrorWithoutNavigating() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { createWorkoutError = IllegalStateException("save failed") }
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutDetailsViewModel(initialId = null, workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        viewModel.onIntent(WorkoutDetailsIntent.ChangeName("New Program"))

        viewModel.onIntent(WorkoutDetailsIntent.Save)
        runCurrent()

        assertEquals("save failed", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals("New Program", viewModel.uiState.value.name)
        assertEquals(0, navigator.backCallCount)
    }

    @Test
    fun backCallsNavigatorBackWithoutTouchingRepository() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutDetailsViewModel(initialId = null, workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()

        viewModel.onIntent(WorkoutDetailsIntent.Back)

        assertEquals(1, navigator.backCallCount)
        assertTrue(repository.createdWorkouts.isEmpty())
        assertTrue(repository.editedWorkouts.isEmpty())
    }

    private fun workout(
        id: String,
        name: String = "Push Day",
        description: String? = null,
        exercises: List<WorkoutExerciseWithRepsEntity> = emptyList(),
    ): WorkoutEntity = WorkoutEntity(id = id, name = name, description = description, exercises = exercises)

    private fun exercise(id: String, name: String, vararg plan: Pair<Int, Double?>): WorkoutExerciseWithRepsEntity =
        WorkoutExerciseWithRepsEntity(
            exercise = WorkoutExerciseEntity(id = id, name = name),
            reps = plan.map { (count, weight) -> WorkoutRepEntity(count = count, weight = weight) },
        )
}
