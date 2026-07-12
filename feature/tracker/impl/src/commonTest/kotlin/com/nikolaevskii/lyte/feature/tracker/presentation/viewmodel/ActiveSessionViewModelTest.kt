package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.completed
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionOverlayUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState.ActiveSessionContent
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    // Секундомер VM тикает бесконечным циклом на viewModelScope — по завершении тела теста отменяем
    // его, иначе runTest зависнет на очистке, докручивая delay-цикл в виртуальном времени.
    private val createdViewModels = mutableListOf<ActiveSessionViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsSessionAndPrefillsDraftsFromCurrentTarget() = activeSessionTest {
        val clock = FakeClock(Instant.fromEpochMilliseconds(65_000))
        val viewModel = viewModel(repository = repository(twoSetSession()), clock = clock)

        runCurrent()

        val tracking = viewModel.uiState.value.tracking
        assertEquals("s1", tracking.current.currentSetId)
        assertEquals(10, tracking.draftReps)
        assertEquals(60.0, tracking.draftWeight)
        // elapsed = now - startedAt = 65s.
        assertEquals(65, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun restoresMidProgressHonoringCurrentExercise() = activeSessionTest {
        val session = workoutSession(
            currentExerciseId = "e2",
            exercises = listOf(
                sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0))),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 10, targetWeight = 50.0))),
            ),
        )
        val viewModel = viewModel(repository = repository(session))

        runCurrent()

        assertEquals("e2", viewModel.uiState.value.tracking.current.exerciseId)
    }

    @Test
    fun timerTicksAsClockAdvances() = activeSessionTest {
        val clock = FakeClock(Instant.fromEpochMilliseconds(0))
        val viewModel = viewModel(repository = repository(twoSetSession()), clock = clock)
        runCurrent()

        clock.current = Instant.fromEpochMilliseconds(3_000)
        advanceTimeBy(3_100)
        runCurrent()

        assertEquals(3, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun timerSelfCorrectsAfterLargeClockJump() = activeSessionTest {
        val clock = FakeClock(Instant.fromEpochMilliseconds(0))
        val viewModel = viewModel(repository = repository(twoSetSession()), clock = clock)
        runCurrent()

        // Приложение было в фоне: часы ушли вперёд на час, а не копились по +1 в цикле.
        clock.current = Instant.fromEpochMilliseconds(3_600_000)
        advanceTimeBy(1_100)
        runCurrent()

        assertEquals(3_600, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun completeSetWritesDraftAndRefillsForNextSet() = activeSessionTest {
        val repository = repository(twoSetSession())
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnDraftRepsChanged(9))
        viewModel.onIntent(ActiveSessionIntent.OnDraftWeightChanged(62.5))
        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals(listOf(Triple<String, Int, Double?>("s1", 9, 62.5)), repository.completeSetCalls)
        val tracking = viewModel.uiState.value.tracking
        // Текущим стал второй подход, драфты перезаполнились его целью (8×62.5).
        assertEquals("s2", tracking.current.currentSetId)
        assertEquals(8, tracking.draftReps)
        assertEquals(62.5, tracking.draftWeight)
    }

    @Test
    fun completingLastSetAdvancesToNextExerciseWithoutPersistingSelection() = activeSessionTest {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0))),
            ),
        )
        val repository = repository(session)
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals("e2", viewModel.uiState.value.tracking.current.exerciseId)
        // Автопереход не пишет текущее упражнение в БД.
        assertTrue(repository.setCurrentExerciseCalls.isEmpty())
    }

    @Test
    fun completingBodyweightSetPassesNullWeight() = activeSessionTest {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(id = "e1", name = "Брусья", sets = listOf(sessionSet(id = "s1", targetCount = 12, targetWeight = null))),
            ),
        )
        val repository = repository(session)
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals(listOf(Triple<String, Int, Double?>("s1", 12, null)), repository.completeSetCalls)
    }

    @Test
    fun skipMarksSetSkipped() = activeSessionTest {
        val repository = repository(twoSetSession())
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnSkipSetClicked)
        runCurrent()

        assertEquals(listOf("s1"), repository.skipSetCalls)
        assertEquals("s2", viewModel.uiState.value.tracking.current.currentSetId)
    }

    @Test
    fun noteFlowOpensEditsSavesAndClosesOverlay() = activeSessionTest {
        val repository = repository(twoSetSession())
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked)
        assertEquals(ActiveSessionOverlayUiModel.NoteSheet(draft = ""), viewModel.uiState.value.tracking.overlay)

        viewModel.onIntent(ActiveSessionIntent.OnNoteDraftChanged("тяжело"))
        assertEquals(ActiveSessionOverlayUiModel.NoteSheet(draft = "тяжело"), viewModel.uiState.value.tracking.overlay)

        viewModel.onIntent(ActiveSessionIntent.OnSaveNoteClicked)
        runCurrent()

        assertEquals(listOf("s1" to "тяжело"), repository.saveNoteCalls)
        assertEquals(ActiveSessionOverlayUiModel.None, viewModel.uiState.value.tracking.overlay)
        assertEquals("тяжело", viewModel.uiState.value.tracking.current.note)
    }

    @Test
    fun selectingExercisePersistsSelectionAndClosesOverlay() = activeSessionTest {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0))),
            ),
        )
        val repository = repository(session)
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnOpenExerciseSheetClicked)
        viewModel.onIntent(ActiveSessionIntent.OnExerciseSelected("e2"))
        runCurrent()

        assertEquals(listOf("session-1" to "e2"), repository.setCurrentExerciseCalls)
        assertEquals(ActiveSessionOverlayUiModel.None, viewModel.uiState.value.tracking.overlay)
        assertEquals("e2", viewModel.uiState.value.tracking.current.exerciseId)
    }

    @Test
    fun selectingDoneExerciseIsNoOp() = activeSessionTest {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0))),
                ),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0))),
            ),
        )
        val repository = repository(session)
        val viewModel = viewModel(repository = repository)
        runCurrent()
        viewModel.onIntent(ActiveSessionIntent.OnOpenExerciseSheetClicked)

        viewModel.onIntent(ActiveSessionIntent.OnExerciseSelected("e1"))
        runCurrent()

        assertTrue(repository.setCurrentExerciseCalls.isEmpty())
    }

    @Test
    fun endEarlyConfirmedFinishesAndNavigatesToLandingReplacingStack() = activeSessionTest {
        val repository = repository(twoSetSession())
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository = repository, navigator = navigator)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnEndEarlyClicked)
        viewModel.onIntent(ActiveSessionIntent.OnEndEarlyConfirmed)
        runCurrent()

        assertEquals(listOf("session-1"), repository.finishSessionCalls)
        assertEquals(listOf(landingReplacingSession()), navigator.commandLog)
    }

    @Test
    fun finishFromAllDoneNavigatesToLanding() = activeSessionTest {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0))),
                ),
            ),
        )
        val repository = repository(session)
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository = repository, navigator = navigator)
        runCurrent()
        // Все подходы разрешены — экран-итог.
        assertTrue(viewModel.uiState.value.content is ActiveSessionContent.AllDone)

        viewModel.onIntent(ActiveSessionIntent.OnFinishClicked)
        runCurrent()

        assertEquals(listOf("session-1"), repository.finishSessionCalls)
        assertEquals(listOf(landingReplacingSession()), navigator.commandLog)
    }

    @Test
    fun missingSessionSurfacesError() = activeSessionTest {
        val viewModel = viewModel(repository = FakeWorkoutSessionRepository(initialSession = null))

        runCurrent()

        assertEquals(ActiveSessionContent.Error, viewModel.uiState.value.content)
    }

    @Test
    fun alreadyFinishedSessionRedirectsToLanding() = activeSessionTest {
        val session = workoutSession(
            finishedAtMillis = 1_000,
            exercises = listOf(
                sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
            ),
        )
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository = repository(session), navigator = navigator)

        runCurrent()

        // Завершённая сессия не разворачивается в контент — только редирект.
        assertEquals(ActiveSessionContent.Loading, viewModel.uiState.value.content)
        assertEquals(listOf(landingReplacingSession()), navigator.commandLog)
    }

    @Test
    fun doubleTapCompleteWritesOnce() = activeSessionTest {
        val repository = repository(twoSetSession())
        val viewModel = viewModel(repository = repository)
        runCurrent()

        // Два тапа до того, как первая мутация успела перечитать сессию.
        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals(1, repository.completeSetCalls.size)
    }

    @Test
    fun mutationFailureSurfacesErrorAndKeepsSession() = activeSessionTest {
        val repository = repository(twoSetSession()).apply { completeSetError = IllegalStateException("db down") }
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.tracking.hasMutationError)
        assertFalse(state.isMutating)
    }

    /** Тело теста + отмена секундомеров созданных VM до очистки runTest (иначе цикл-тик зависает). */
    private fun activeSessionTest(body: TestScope.() -> Unit) = runTest(testDispatcher) {
        body()
        createdViewModels.forEach { viewModel -> viewModel.viewModelScope.cancel() }
        runCurrent()
    }

    private fun viewModel(
        repository: FakeWorkoutSessionRepository,
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
        clock: FakeClock = FakeClock(Instant.fromEpochMilliseconds(0)),
    ): ActiveSessionViewModel = ActiveSessionViewModel(
        sessionId = SESSION_ID,
        workoutSessionRepository = repository,
        lyteNavigator = navigator,
        clock = clock,
    ).also { viewModel -> createdViewModels += viewModel }

    private fun repository(session: WorkoutSessionEntity): FakeWorkoutSessionRepository =
        FakeWorkoutSessionRepository(initialSession = session)

    private fun twoSetSession(): WorkoutSessionEntity = workoutSession(
        id = SESSION_ID,
        exercises = listOf(
            sessionExercise(
                id = "e1",
                name = "Жим лёжа",
                sets = listOf(
                    sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0),
                    sessionSet(id = "s2", targetCount = 8, targetWeight = 62.5),
                ),
            ),
        ),
    )

    private fun landingReplacingSession(): NavCommand = NavCommand.Forward(
        route = TrackerLandingRoute,
        options = LyteNavOptions(popUpTo = ActiveSessionRoute(sessionId = SESSION_ID), popUpToInclusive = true),
    )

    private companion object {
        const val SESSION_ID = "session-1"
    }
}

/** Контент как трекинг — большинство проверок про активный подход; падает, если состояние другое. */
private val ActiveSessionUiState.tracking: ActiveSessionContent.Tracking
    get() = content as ActiveSessionContent.Tracking
