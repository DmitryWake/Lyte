package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutRepEntity
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutPreviewViewModelTest {

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
    fun loadsProgramOnInit() = runTest(testDispatcher) {
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialWorkout = pushDay()))

        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        val program = assertNotNull(state.program)
        assertEquals("Push Day", program.programName)
        assertEquals(2, program.exerciseCount)
        assertEquals(3, program.setCount)
        assertEquals(listOf("Жим лёжа", "Отжимания на брусьях"), program.exercises.map { it.name })
        assertEquals(listOf(1, 2), program.exercises.map { it.number })
    }

    @Test
    fun missingProgramSurfacesError() = runTest(testDispatcher) {
        // getWorkout возвращает null (программу удалили/заархивировали) — checkNotNull падает в ошибку.
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialWorkout = null))

        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertNull(state.program)
    }

    @Test
    fun failedLoadSurfacesError() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutError = IllegalStateException("boom") }
        val viewModel = viewModel(repository = repository)

        runCurrent()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("boom", state.errorMessage)
    }

    @Test
    fun backPopsBackStack() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(navigator = navigator)
        runCurrent()

        viewModel.onIntent(WorkoutPreviewIntent.OnBack)

        assertEquals(listOf<NavCommand>(NavCommand.Back), navigator.commandLog)
    }

    @Test
    fun startCreatesSessionAndReplacesStackWithSessionScreen() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val sessionRepository = FakeWorkoutSessionRepository().apply { startedSessionId = "session-9" }
        val viewModel = viewModel(
            repository = FakeWorkoutRepository(initialWorkout = pushDay()),
            sessionRepository = sessionRepository,
            navigator = navigator,
        )
        runCurrent()

        viewModel.onIntent(WorkoutPreviewIntent.OnStartClicked)
        runCurrent()

        assertEquals(1, sessionRepository.startSessionCalls.size)
        assertEquals(
            listOf<NavCommand>(
                NavCommand.Forward(
                    route = ActiveSessionRoute(sessionId = "session-9"),
                    options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
                ),
            ),
            navigator.commandLog,
        )
    }

    @Test
    fun startWhenActiveSessionExistsOpensThatSession() = runTest(testDispatcher) {
        // Инвариант БД «не больше одной активной сессии» кидает при старте — открываем существующую.
        val navigator = FakeLyteNavigator()
        val sessionRepository = FakeWorkoutSessionRepository().apply {
            startSessionError = IllegalStateException("active session exists")
            activeSession = workoutSession(
                id = "existing-1",
                exercises = listOf(
                    sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
                ),
            )
        }
        val viewModel = viewModel(
            repository = FakeWorkoutRepository(initialWorkout = pushDay()),
            sessionRepository = sessionRepository,
            navigator = navigator,
        )
        runCurrent()

        viewModel.onIntent(WorkoutPreviewIntent.OnStartClicked)
        runCurrent()

        assertEquals(
            listOf<NavCommand>(
                NavCommand.Forward(
                    route = ActiveSessionRoute(sessionId = "existing-1"),
                    options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
                ),
            ),
            navigator.commandLog,
        )
    }

    @Test
    fun doubleTapStartCreatesSessionOnce() = runTest(testDispatcher) {
        val sessionRepository = FakeWorkoutSessionRepository()
        val viewModel = viewModel(
            repository = FakeWorkoutRepository(initialWorkout = pushDay()),
            sessionRepository = sessionRepository,
        )
        runCurrent()

        viewModel.onIntent(WorkoutPreviewIntent.OnStartClicked)
        viewModel.onIntent(WorkoutPreviewIntent.OnStartClicked)
        runCurrent()

        assertEquals(1, sessionRepository.startSessionCalls.size)
    }

    @Test
    fun startFailureSurfacesError() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val sessionRepository = FakeWorkoutSessionRepository().apply {
            startSessionError = IllegalStateException("boom")
        }
        val viewModel = viewModel(
            repository = FakeWorkoutRepository(initialWorkout = pushDay()),
            sessionRepository = sessionRepository,
            navigator = navigator,
        )
        runCurrent()

        viewModel.onIntent(WorkoutPreviewIntent.OnStartClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals("boom", state.errorMessage)
        assertFalse(state.isStarting)
        assertTrue(navigator.commandLog.isEmpty())
    }

    private fun viewModel(
        repository: FakeWorkoutRepository = FakeWorkoutRepository(),
        sessionRepository: FakeWorkoutSessionRepository = FakeWorkoutSessionRepository(),
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): WorkoutPreviewViewModel = WorkoutPreviewViewModel(
        programId = PROGRAM_ID,
        workoutRepository = repository,
        workoutSessionRepository = sessionRepository,
        lyteNavigator = navigator,
    )

    private fun pushDay(): WorkoutEntity = WorkoutEntity(
        id = PROGRAM_ID,
        name = "Push Day",
        description = null,
        exercises = listOf(
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = "e1", name = "Жим лёжа"),
                reps = listOf(
                    WorkoutRepEntity(count = 8, weight = 80.0),
                    WorkoutRepEntity(count = 8, weight = 80.0),
                ),
            ),
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = "e2", name = "Отжимания на брусьях"),
                reps = listOf(WorkoutRepEntity(count = 12, weight = null)),
            ),
        ),
    )

    private companion object {
        const val PROGRAM_ID = "w1"
    }
}
