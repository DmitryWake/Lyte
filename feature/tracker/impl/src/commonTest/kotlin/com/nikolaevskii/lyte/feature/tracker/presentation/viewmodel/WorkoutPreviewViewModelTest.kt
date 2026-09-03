package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewUiState
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
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

        val program = (viewModel.uiState.value as WorkoutPreviewUiState.Content).program
        assertEquals("Push Day", program.programName)
        assertEquals(2, program.exerciseCount)
        assertEquals(listOf("Жим лёжа", "Отжимания на брусьях"), program.exercises.map { it.name })
        assertEquals(listOf(1, 2), program.exercises.map { it.number })
    }

    @Test
    fun missingProgramSurfacesError() = runTest(testDispatcher) {
        // getWorkout отдаёт null и для удалённой программы (архивную он не возвращает) — экран
        // обязан показать «нет такой», а не пустой состав с кнопкой «Начать».
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialWorkout = null))

        runCurrent()

        assertEquals(WorkoutPreviewUiState.Error(LyteError.NotFound), viewModel.uiState.value)
    }

    @Test
    fun failedLoadSurfacesError() = runTest(testDispatcher) {
        val repository = FakeWorkoutRepository().apply { getWorkoutError = IllegalStateException("boom") }
        val viewModel = viewModel(repository = repository)

        runCurrent()

        assertTrue(viewModel.uiState.value is WorkoutPreviewUiState.Error)
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
    fun startAfterProgramDeletedReplacesContentWithNotFound() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val sessionRepository = FakeWorkoutSessionRepository()
        val repository = FakeWorkoutRepository(initialWorkout = pushDay())
        val viewModel = viewModel(
            repository = repository,
            sessionRepository = sessionRepository,
            navigator = navigator,
        )
        runCurrent()
        // Программу удалили во вкладке «Программы», пока превью висело в стеке трекера.
        repository.workout = null

        viewModel.onIntent(WorkoutPreviewIntent.OnStartClicked)
        runCurrent()

        // Не баннер над устаревшим составом: запускать нечего, поэтому экран целиком меняется.
        assertEquals(WorkoutPreviewUiState.Error(LyteError.NotFound), viewModel.uiState.value)
        assertTrue(sessionRepository.startSessionCalls.isEmpty())
        assertTrue(navigator.commandLog.isEmpty())
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

        val content = viewModel.uiState.value as WorkoutPreviewUiState.Content
        assertNotNull(content.startError)
        assertFalse(content.isStarting)
        assertTrue(navigator.commandLog.isEmpty())
    }

    @Test
    fun exerciseTapOpensInfoSheetWithThatExercise() = runTest(testDispatcher) {
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialWorkout = pushDay()))
        runCurrent()

        viewModel.onIntent(WorkoutPreviewIntent.OnExerciseClicked(number = 2))

        val content = viewModel.uiState.value as WorkoutPreviewUiState.Content
        assertEquals(content.program.exercises[1], content.exerciseInfo)
    }

    @Test
    fun dismissClosesInfoSheet() = runTest(testDispatcher) {
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialWorkout = pushDay()))
        runCurrent()
        viewModel.onIntent(WorkoutPreviewIntent.OnExerciseClicked(number = 1))

        viewModel.onIntent(WorkoutPreviewIntent.OnExerciseInfoDismissed)

        assertNull((viewModel.uiState.value as WorkoutPreviewUiState.Content).exerciseInfo)
    }

    @Test
    fun tapOnUnknownExerciseLeavesStateUntouched() = runTest(testDispatcher) {
        // Номера, которому нет упражнения, быть не должно — но открывать пустую шторку тем более нечем.
        val viewModel = viewModel(repository = FakeWorkoutRepository(initialWorkout = pushDay()))
        runCurrent()
        val before = viewModel.uiState.value

        viewModel.onIntent(WorkoutPreviewIntent.OnExerciseClicked(number = 42))

        assertEquals(before, viewModel.uiState.value)
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
                exercise = WorkoutExerciseEntity(
                    id = "e1",
                    name = "Жим лёжа",
                    description = "Штанга, горизонтальная скамья",
                ),
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
