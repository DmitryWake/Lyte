package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
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
class WorkoutListViewModelTest {

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
    fun loadsWorkoutsOnInit() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)), viewModel.uiState.value.programs)
    }

    @Test
    fun refreshReloadsWorkoutsList() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        repository.items = listOf(program(id = "w1", name = "Push Day V2", exerciseCount = 4))

        viewModel.onIntent(WorkoutListIntent.OnScreenShown)
        runCurrent()

        assertEquals(listOf(program(id = "w1", name = "Push Day V2", exerciseCount = 4)), viewModel.uiState.value.programs)
    }

    @Test
    fun failedLoadSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutsError = IllegalStateException("boom") }
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("boom", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.programs.isEmpty())
    }

    @Test
    fun requestDeleteShowsConfirmationWithoutDeleting() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        assertEquals("w1", viewModel.uiState.value.pendingDeleteId)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun cancelDeleteClearsPendingDeleteWithoutDeleting() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        viewModel.onIntent(WorkoutListIntent.OnDeleteDismissed)

        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun confirmDeleteRemovesProgramAndReloadsList() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(
            initialItems = listOf(
                program(id = "w1", name = "Push Day", exerciseCount = 5),
                program(id = "w2", name = "Pull Day", exerciseCount = 4),
            ),
        )
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()
        viewModel.onIntent(WorkoutListIntent.OnDeleteProgramClicked(id = "w1"))

        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()

        assertEquals(listOf("w1"), repository.deletedIds)
        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertEquals(listOf("w2"), viewModel.uiState.value.programs.map { it.id })
    }

    @Test
    fun confirmDeleteWithoutPendingIdDoesNothing() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = FakeLyteNavigator())
        runCurrent()

        viewModel.onIntent(WorkoutListIntent.OnDeleteConfirmed)
        runCurrent()

        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test
    fun openDetailsNavigatesToDetailsRouteWithProgramIdAndDoesNotChangeState() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(WorkoutListIntent.OnProgramClicked(id = "w1"))

        assertEquals(listOf<Pair<Any, LyteNavOptions?>>(WorkoutDetailsRoute(id = "w1") to null), navigator.navigateCalls)
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    @Test
    fun createProgramNavigatesToDetailsRouteWithNullId() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository(initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)))
        val navigator = FakeLyteNavigator()
        val viewModel = WorkoutListViewModel(workoutRepository = repository, lyteNavigator = navigator)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(WorkoutListIntent.OnCreateProgramClicked)

        assertEquals(listOf<Pair<Any, LyteNavOptions?>>(WorkoutDetailsRoute(id = null) to null), navigator.navigateCalls)
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    private fun program(id: String, name: String, exerciseCount: Int): WorkoutItemEntity =
        WorkoutItemEntity(id = id, name = name, description = null, exerciseCount = exerciseCount)
}
