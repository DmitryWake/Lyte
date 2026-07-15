package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.WorkoutPreviewRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerIntent
import com.nikolaevskii.lyte.feature.workout.WorkoutTabGraph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutProgramUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerUiState
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

        assertEquals(
            WorkoutPickerUiState.Content(
                listOf(WorkoutProgramUiModel(id = "w1", name = "Push Day", exerciseCount = 5)),
            ),
            viewModel.uiState.value,
        )
    }

    @Test
    fun emptyRepositoryLeavesProgramsEmpty() = runTest(testDispatcher) {
        val viewModel = viewModel(repository = FakeWorkoutRepository())

        runCurrent()

        assertEquals(WorkoutPickerUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun failedLoadSurfacesErrorMessage() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutsError = IllegalStateException("boom") }
        val viewModel = viewModel(repository = repository)

        runCurrent()

        assertTrue(viewModel.uiState.value is WorkoutPickerUiState.Error)
    }

    @Test
    fun programClickOpensPreview() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(WorkoutPickerIntent.OnProgramClicked(id = "w1"))
        runCurrent()

        assertEquals(
            listOf<NavCommand>(NavCommand.Forward(route = WorkoutPreviewRoute(programId = "w1"))),
            navigator.commandLog,
        )
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
