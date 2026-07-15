package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.WorkoutPickerRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingUiState
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
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
    fun openWorkoutPickerNavigatesForwardWithinTheSameTab() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(TrackerLandingIntent.OpenWorkoutPicker)

        assertEquals(
            NavCommand.Forward(route = WorkoutPickerRoute, options = null),
            navigator.commandLog.last(),
        )
    }

    private fun viewModel(
        sessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): TrackerLandingViewModel = TrackerLandingViewModel(
        workoutSessionRepository = sessionRepository,
        lyteNavigator = navigator,
    )

    private fun activeSession() = workoutSession(
        id = "active-1",
        exercises = listOf(
            sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
        ),
    )
}
