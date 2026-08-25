package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.WorkoutPreviewRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ProgramPickerUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.WorkoutProgramUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingUiState
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.WorkoutTabGraph
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
class TrackerLandingViewModelTest {

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
    fun startsInCheckingState() = runTest(testDispatcher) {
        val viewModel = viewModel(sessionRepository = FakeWorkoutSessionRepository())

        // До прогона диспетчера гейт ещё проверяет сессию.
        assertTrue(viewModel.uiState.value is TrackerLandingUiState.CheckingSession)
    }

    @Test
    fun noActiveSessionShowsLandingWithoutNavigating() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(sessionRepository = FakeWorkoutSessionRepository(), navigator = navigator)

        runCurrent()

        assertTrue(viewModel.uiState.value is TrackerLandingUiState.NoActiveSession)
        assertTrue(navigator.commandLog.isEmpty())
    }

    @Test
    fun activeSessionRedirectsReplacingLanding() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val sessionRepository = FakeWorkoutSessionRepository().apply { activeSession = activeSession() }
        val viewModel = viewModel(sessionRepository = sessionRepository, navigator = navigator)

        runCurrent()

        // Гейт нашёл активную сессию — уводит на её экран, заменяя лендинг в стеке.
        assertTrue(viewModel.uiState.value is TrackerLandingUiState.CheckingSession)
        assertEquals(
            listOf<NavCommand>(
                NavCommand.Forward(
                    route = ActiveSessionRoute(sessionId = "active-1"),
                    options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
                ),
            ),
            navigator.commandLog,
        )
    }

    @Test
    fun sessionCheckFailureFallsBackToLanding() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val sessionRepository = FakeWorkoutSessionRepository().apply {
            getActiveSessionError = IllegalStateException("db down")
        }
        val viewModel = viewModel(sessionRepository = sessionRepository, navigator = navigator)

        runCurrent()

        // Не смогли проверить — вкладку не блокируем, показываем обычный лендинг.
        assertTrue(viewModel.uiState.value is TrackerLandingUiState.NoActiveSession)
        assertTrue(navigator.commandLog.isEmpty())
    }

    @Test
    fun startOpensPickerAndLoadsPrograms() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val workoutRepository = FakeWorkoutRepository(
            initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)),
        )
        val viewModel = viewModel(workoutRepository = workoutRepository, navigator = navigator)
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OnStartClicked)

        // Шторка открывается сразу, программы догружаются следующим шагом диспетчера.
        assertEquals(
            TrackerLandingUiState.NoActiveSession(picker = ProgramPickerUiModel.Loading),
            viewModel.uiState.value,
        )

        runCurrent()

        assertEquals(
            TrackerLandingUiState.NoActiveSession(
                picker = ProgramPickerUiModel.Programs(
                    listOf(WorkoutProgramUiModel(id = "w1", name = "Push Day", exerciseCount = 5)),
                ),
            ),
            viewModel.uiState.value,
        )
        assertTrue(navigator.commandLog.isEmpty())
    }

    @Test
    fun emptyLibraryShowsEmptyPicker() = runTest(testDispatcher) {
        val viewModel = viewModel(workoutRepository = FakeWorkoutRepository())
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OnStartClicked)
        runCurrent()

        assertEquals(
            TrackerLandingUiState.NoActiveSession(picker = ProgramPickerUiModel.Empty),
            viewModel.uiState.value,
        )
    }

    @Test
    fun failedLoadShowsErrorInsidePicker() = runTest(testDispatcher) {
        val workoutRepository = FakeWorkoutRepository().apply {
            getWorkoutsError = IllegalStateException("boom")
        }
        val viewModel = viewModel(workoutRepository = workoutRepository)
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OnStartClicked)
        runCurrent()

        // Сбой загрузки уходит в воронку handleError и остаётся внутри шторки: лендинг живой.
        val state = viewModel.uiState.value
        assertTrue(state is TrackerLandingUiState.NoActiveSession && state.picker is ProgramPickerUiModel.Error)
    }

    @Test
    fun dismissedPickerIsNotReopenedByLoadResult() = runTest(testDispatcher) {
        val viewModel = viewModel(
            workoutRepository = FakeWorkoutRepository(
                initialItems = listOf(program(id = "w1", name = "Push Day", exerciseCount = 5)),
            ),
        )
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OnStartClicked)
        viewModel.onIntent(TrackerLandingIntent.OnPickerDismissed)
        runCurrent()

        assertEquals(TrackerLandingUiState.NoActiveSession(), viewModel.uiState.value)
    }

    @Test
    fun programClickClosesPickerAndOpensPreview() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OnStartClicked)
        viewModel.onIntent(TrackerLandingIntent.OnProgramClicked(id = "w1"))

        assertEquals(TrackerLandingUiState.NoActiveSession(), viewModel.uiState.value)
        assertEquals(
            listOf<NavCommand>(NavCommand.Forward(route = WorkoutPreviewRoute(programId = "w1"))),
            navigator.commandLog,
        )
    }

    @Test
    fun createProgramSwitchesTabAndOpensEditor() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OnStartClicked)
        viewModel.onIntent(TrackerLandingIntent.OnCreateProgramClicked)

        // Порядок важен: сначала переключение вкладки (с сохранением стека уходящей), и только
        // потом редактор — иначе он лёг бы в стек трекера.
        assertEquals(TrackerLandingUiState.NoActiveSession(), viewModel.uiState.value)
        assertEquals(
            listOf<NavCommand>(
                NavCommand.SwitchTab(graphRoute = WorkoutTabGraph),
                NavCommand.Forward(route = WorkoutDetailsRoute(id = null)),
            ),
            navigator.commandLog,
        )
    }

    private fun viewModel(
        sessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
        workoutRepository: FakeWorkoutRepository = FakeWorkoutRepository(),
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): TrackerLandingViewModel = TrackerLandingViewModel(
        workoutSessionRepository = sessionRepository,
        workoutRepository = workoutRepository,
        lyteNavigator = navigator,
    )

    private fun program(id: String, name: String, exerciseCount: Int): WorkoutItemEntity =
        WorkoutItemEntity(id = id, name = name, description = null, exerciseCount = exerciseCount)

    private fun activeSession() = workoutSession(
        id = "active-1",
        exercises = listOf(
            sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
        ),
    )
}
