package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerIntent
import com.nikolaevskii.lyte.feature.workout.WorkoutTabGraph
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutItemEntity
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
class WorkoutPickerViewModelTest {

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
    fun loadsProgramsOnInit() = runTest(testDispatcher) {
        val programs = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5))
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialItems = programs))

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(programs, viewModel.uiState.value.programs)
    }

    @Test
    fun emptyRepositoryLeavesProgramsEmpty() = runTest(testDispatcher) {
        val viewModel = viewModel(repository = FakeWorkoutRepository())

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.programs.isEmpty())
    }

    @Test
    fun failedLoadSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutsError = IllegalStateException("boom") }
        val viewModel = viewModel(repository = repository)

        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("boom", viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.programs.isEmpty())
    }

    @Test
    fun programClickIsStillAStub() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(WorkoutPickerIntent.OnProgramClicked(id = "w1"))
        runCurrent()

        assertTrue(navigator.commandLog.isEmpty())
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    @Test
    fun createProgramPopsPickerBeforeSwitchingToWorkoutTab() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(WorkoutPickerIntent.OnCreateProgramClicked)

        // Порядок критичен: switchTab сохраняет стек уходящей вкладки, поэтому экран выбора должен
        // быть снят до переключения — иначе возврат на «Трекер» покажет его, а не главный экран.
        assertEquals(
            listOf<NavCommand>(NavCommand.Back, NavCommand.SwitchTab(graphRoute = WorkoutTabGraph)),
            navigator.commandLog,
        )
    }

    @Test
    fun backPopsBackStack() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(WorkoutPickerIntent.OnBack)

        assertEquals(listOf<NavCommand>(NavCommand.Back), navigator.commandLog)
    }

    private fun viewModel(
        repository: FakeWorkoutRepository = FakeWorkoutRepository(),
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): WorkoutPickerViewModel = WorkoutPickerViewModel(
        workoutRepository = repository,
        lyteNavigator = navigator,
    )

    private fun program(id: String, name: String, exerciseCount: Int): WorkoutItemEntity =
        WorkoutItemEntity(id = id, name = name, description = null, exerciseCount = exerciseCount)
}
