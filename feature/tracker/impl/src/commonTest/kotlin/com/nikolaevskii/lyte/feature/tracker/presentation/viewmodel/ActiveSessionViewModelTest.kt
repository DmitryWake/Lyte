package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.history.HistorySessionDetailsRoute
import com.nikolaevskii.lyte.feature.history.HistoryTabGraph
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.completed
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionOverlayUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionMutationError
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState.ActiveSessionContent
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
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
import com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetState
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import kotlin.test.assertNull
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
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0))
                ),
                sessionExercise(
                    id = "e2",
                    name = "Тяга",
                    sets = listOf(sessionSet(id = "s2", targetCount = 10, targetWeight = 50.0))
                ),
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

        assertEquals(
            listOf(Triple<String, Int, Double?>("s1", 9, 62.5)),
            repository.completeSetCalls
        )
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
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))
                ),
                sessionExercise(
                    id = "e2",
                    name = "Тяга",
                    sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0))
                ),
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
                sessionExercise(
                    id = "e1",
                    name = "Брусья",
                    sets = listOf(sessionSet(id = "s1", targetCount = 12, targetWeight = null))
                ),
            ),
        )
        val repository = repository(session)
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals(
            listOf(Triple<String, Int, Double?>("s1", 12, null)),
            repository.completeSetCalls
        )
    }

    @Test
    fun addedWeightOnBodyweightSetIsSaved() = activeSessionTest {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Подтягивания",
                    sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = null))
                ),
            ),
        )
        val repository = repository(session)
        val viewModel = viewModel(repository = repository)
        runCurrent()

        // У цели веса нет, но пояс на подходе был — вес обязан доехать до факта.
        viewModel.onIntent(ActiveSessionIntent.OnDraftWeightChanged(10.0))
        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals(
            listOf(Triple<String, Int, Double?>("s1", 8, 10.0)),
            repository.completeSetCalls
        )
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
        assertEquals(
            ActiveSessionOverlayUiModel.NoteSheet(draft = ""),
            viewModel.uiState.value.tracking.overlay
        )

        viewModel.onIntent(ActiveSessionIntent.OnNoteDraftChanged("тяжело"))
        assertEquals(
            ActiveSessionOverlayUiModel.NoteSheet(draft = "тяжело"),
            viewModel.uiState.value.tracking.overlay
        )

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
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))
                ),
                sessionExercise(
                    id = "e2",
                    name = "Тяга",
                    sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0))
                ),
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
                    sets = listOf(
                        sessionSet(
                            id = "s1",
                            targetCount = 10,
                            targetWeight = 60.0,
                            result = completed(count = 10, weight = 60.0)
                        )
                    ),
                ),
                sessionExercise(
                    id = "e2",
                    name = "Тяга",
                    sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0))
                ),
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
    fun endEarlyConfirmedFinishesAndNavigatesToSessionDetails() = activeSessionTest {
        val repository = repository(twoSetSession())
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository = repository, navigator = navigator)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnEndEarlyClicked)
        viewModel.onIntent(ActiveSessionIntent.OnEndEarlyConfirmed)
        runCurrent()

        assertEquals(listOf("session-1"), repository.finishSessionCalls)
        assertEquals(finishNavigation(), navigator.commandLog)
    }

    @Test
    fun finishFromAllDoneNavigatesToSessionDetails() = activeSessionTest {
        val repository = repository(allDoneSession())
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository = repository, navigator = navigator)
        runCurrent()
        // Все подходы разрешены — экран-итог, и он несёт сводку сессии целиком.
        val content = viewModel.uiState.value.content
        assertTrue(content is ActiveSessionContent.AllDone)
        assertEquals("Push Day", content.programName)
        assertEquals(1, content.completedCount)
        assertEquals(2, content.totalCount)
        assertEquals(listOf(LyteProgressTone.Met, LyteProgressTone.Skipped), content.setTones)

        viewModel.onIntent(ActiveSessionIntent.OnFinishClicked)
        runCurrent()

        assertEquals(listOf("session-1"), repository.finishSessionCalls)
        assertEquals(finishNavigation(), navigator.commandLog)
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
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))
                ),
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
        // Флаг занятости поднят синхронно — на нём держатся и guard, и погашенные кнопки записи.
        assertTrue(viewModel.uiState.value.isMutating)
        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        assertEquals(1, repository.completeSetCalls.size)
        assertFalse(viewModel.uiState.value.isMutating)
    }

    @Test
    fun mutationFailureSurfacesErrorAndKeepsSession() = activeSessionTest {
        val repository = repository(twoSetSession()).apply {
            completeSetError = IllegalStateException("db down")
        }
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(ActiveSessionMutationError.Write, state.mutationError)
        assertFalse(state.isMutating)
    }

    /** Провал записи и следующая удачная: баннер обязан погаснуть, иначе он врёт про текущее состояние. */
    @Test
    fun successfulWriteAfterFailureClearsMutationBanner() = activeSessionTest {
        val repository = repository(twoSetSession()).apply {
            completeSetError = IllegalStateException("db down")
        }
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()
        assertEquals(ActiveSessionMutationError.Write, viewModel.uiState.value.mutationError)

        repository.completeSetError = null
        viewModel.onIntent(ActiveSessionIntent.OnSkipSetClicked)
        runCurrent()

        assertEquals(null, viewModel.uiState.value.mutationError)
    }

    /**
     * Провал сохранения заметки оставляет шторку открытой с набранным текстом: закрывает её только
     * удачная запись (через пересборку контента). Иначе набранное исчезало бы, а баннер сообщал бы
     * лишь «не удалось сохранить» — без единого способа вернуть текст.
     */
    @Test
    fun failedNoteSaveKeepsSheetWithDraft() = activeSessionTest {
        val repository = repository(twoSetSession()).apply {
            saveSetNoteError = IllegalStateException("db down")
        }
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked)
        viewModel.onIntent(ActiveSessionIntent.OnNoteDraftChanged("пояс затянуть туже"))
        viewModel.onIntent(ActiveSessionIntent.OnSaveNoteClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(ActiveSessionMutationError.Write, state.mutationError)
        assertEquals(ActiveSessionOverlayUiModel.NoteSheet(draft = "пояс затянуть туже"), state.tracking.overlay)

        // Повтор по той же шторке проходит: черновик никуда не делся.
        repository.saveSetNoteError = null
        viewModel.onIntent(ActiveSessionIntent.OnSaveNoteClicked)
        runCurrent()

        assertEquals(listOf("s1" to "пояс затянуть туже"), repository.saveNoteCalls)
        assertEquals(ActiveSessionOverlayUiModel.None, viewModel.uiState.value.tracking.overlay)
        assertEquals("пояс затянуть туже", viewModel.uiState.value.tracking.current.note)
    }

    /**
     * Пока идёт запись, шторки не открываются. Иначе шторка провисела бы поверх чужой мутации, а
     * пересборка контента по её завершении захлопнула бы её вместе с набранным черновиком.
     */
    @Test
    fun sheetsDoNotOpenDuringMutation() = activeSessionTest {
        val repository = repository(twoSetSession())
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        viewModel.onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked)
        assertEquals(ActiveSessionOverlayUiModel.None, viewModel.uiState.value.tracking.overlay)

        viewModel.onIntent(ActiveSessionIntent.OnOpenExerciseSheetClicked)
        assertEquals(ActiveSessionOverlayUiModel.None, viewModel.uiState.value.tracking.overlay)

        // Запись закончилась — шторка снова открывается.
        runCurrent()
        viewModel.onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked)
        assertEquals(ActiveSessionOverlayUiModel.NoteSheet(draft = ""), viewModel.uiState.value.tracking.overlay)
    }

    /**
     * Выбор упражнения, не принятый из-за занятости, не должен пропасть молча: шторка остаётся открытой,
     * и повтор доводит переключение до БД.
     */
    @Test
    fun exerciseSwitchDuringMutationIsNotSwallowed() = activeSessionTest {
        val repository = repository(twoExerciseSession())
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnOpenExerciseSheetClicked)
        // Запись подхода стартует, пока шторка открыта: тап по строке в этот момент не примут.
        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        viewModel.onIntent(ActiveSessionIntent.OnExerciseSelected("e2"))

        assertEquals(emptyList(), repository.setCurrentExerciseCalls)
        assertEquals(ActiveSessionOverlayUiModel.ExerciseSheet, viewModel.uiState.value.tracking.overlay)

        // Запись закончилась: шторку открываем заново и повторяем выбор — он доходит до БД.
        runCurrent()
        viewModel.onIntent(ActiveSessionIntent.OnOpenExerciseSheetClicked)
        viewModel.onIntent(ActiveSessionIntent.OnExerciseSelected("e2"))
        runCurrent()

        assertEquals(listOf(SESSION_ID to "e2"), repository.setCurrentExerciseCalls)
        assertEquals(ActiveSessionOverlayUiModel.None, viewModel.uiState.value.tracking.overlay)
        assertEquals("e2", viewModel.uiState.value.tracking.current.exerciseId)
    }

    /**
     * Провал сохранения на экране-итоге: [ActiveSessionContent.AllDone] не умеет держать ошибку у себя,
     * поэтому она приходит сквозным полем состояния. Без него баннер оставался бы no-op'ом, а кнопка —
     * просто мёртвой на вид.
     */
    @Test
    fun finishFailureOnAllDoneSurfacesError() = activeSessionTest {
        val repository = repository(allDoneSession()).apply {
            finishSessionError = IllegalStateException("db down")
        }
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository = repository, navigator = navigator)
        runCurrent()
        assertTrue(viewModel.uiState.value.content is ActiveSessionContent.AllDone)

        viewModel.onIntent(ActiveSessionIntent.OnFinishClicked)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(ActiveSessionMutationError.Finish, state.mutationError)
        // Кнопка снова живая: сессия цела и незавершена, сохранение можно повторить.
        assertFalse(state.isMutating)
        assertTrue(state.content is ActiveSessionContent.AllDone)
        assertEquals(emptyList(), navigator.commandLog)
    }

    /**
     * Досрочное завершение проваливается на арме трекинга — и баннер обязан сказать про тренировку.
     * Пока текст выбирал арм, здесь печаталось «не удалось сохранить изменение»: неправда, подходы
     * записаны, не удалось именно завершение.
     */
    @Test
    fun endEarlyFailureReportsFinishError() = activeSessionTest {
        val repository = repository(twoSetSession()).apply {
            finishSessionError = IllegalStateException("db down")
        }
        val viewModel = viewModel(repository = repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnEndEarlyClicked)
        viewModel.onIntent(ActiveSessionIntent.OnEndEarlyConfirmed)
        runCurrent()

        val state = viewModel.uiState.value
        assertEquals(ActiveSessionMutationError.Finish, state.mutationError)
        assertTrue(state.content is ActiveSessionContent.Tracking)
    }

    /**
     * Тело теста + отмена секундомеров созданных VM до очистки runTest (иначе цикл-тик зависает).
     *
     * Отмена — в `finally`: провалившееся тело теста иначе не доходит до неё, и вместо красного теста
     * с внятным сообщением получается вечно крутящийся в виртуальном времени `delay`-цикл и повисший
     * прогон. Красный тест обязан падать, а не вешать гейт.
     */
    /**
     * Ориентиры читаются один раз за загрузку. Контент трекинга пересобирается на каждый тап
     * «Готово»/«Пропустить»/сохранение заметки, и запрос внутри этого пути стоил бы обращения к БД на
     * каждый тап — при том, что факты прошлых сессий за время текущей не меняются.
     */
    @Test
    fun previousResultsAreQueriedOncePerLoad() = activeSessionTest {
        val repository = repository(twoSetSession())
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onIntent(ActiveSessionIntent.OnCompleteSetClicked)
        runCurrent()
        viewModel.onIntent(ActiveSessionIntent.OnSkipSetClicked)
        runCurrent()

        assertEquals(expected = 1, actual = repository.previousSetResultsCalls)
    }

    @Test
    fun previousResultIsShownWhenItDivergedFromTarget() = activeSessionTest {
        val repository = repository(twoSetSession())
        repository.previousSetResults = mapOf("s1" to SessionSetValueEntity(count = 8, weight = 57.5))
        val viewModel = viewModel(repository)
        runCurrent()

        assertEquals(
            expected = LyteSetValue(reps = 8, weight = 57.5),
            actual = currentTrackSet(viewModel).last,
        )
    }

    /** Совпавший с целью ориентир — две одинаковые строки подряд; они не сообщают ничего. */
    @Test
    fun previousResultEqualToTargetIsHidden() = activeSessionTest {
        val repository = repository(twoSetSession())
        repository.previousSetResults = mapOf("s1" to SessionSetValueEntity(count = 10, weight = 60.0))
        val viewModel = viewModel(repository)
        runCurrent()

        assertNull(currentTrackSet(viewModel).last, "Ориентир совпал с целью и не должен рисоваться")
    }

    /** У подхода без истории строки нет — пустого или нулевого ориентира не бывает. */
    @Test
    fun setWithoutHistoryHasNoReference() = activeSessionTest {
        val viewModel = viewModel(repository(twoSetSession()))
        runCurrent()

        assertNull(currentTrackSet(viewModel).last)
    }

    /** Сбой чтения ориентиров не ломает экран: подсказка не содержимое, степперы работают. */
    @Test
    fun failedPreviousResultsQueryLeavesScreenUsable() = activeSessionTest {
        val repository = repository(twoSetSession())
        repository.previousSetResultsError = IllegalStateException("БД недоступна")
        val viewModel = viewModel(repository)
        runCurrent()

        val content = viewModel.uiState.value.content
        assertTrue(content is ActiveSessionUiState.ActiveSessionContent.Tracking)
        assertNull(currentTrackSet(viewModel).last)
    }

    private fun currentTrackSet(viewModel: ActiveSessionViewModel): LyteTrackSetState.Current {
        val content = viewModel.uiState.value.content as ActiveSessionUiState.ActiveSessionContent.Tracking
        return content.trackSets.filterIsInstance<LyteTrackSetState.Current>().single()
    }

    private fun activeSessionTest(body: TestScope.() -> Unit) = runTest(testDispatcher) {
        try {
            body()
        } finally {
            createdViewModels.forEach { viewModel -> viewModel.viewModelScope.cancel() }
            runCurrent()
        }
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

    /** Сессия, у которой все подходы разрешены: экран сразу показывает итог, писать остаётся только финиш. */
    private fun allDoneSession(): WorkoutSessionEntity = workoutSession(
        id = SESSION_ID,
        exercises = listOf(
            sessionExercise(
                id = "e1",
                name = "Жим",
                sets = listOf(
                    sessionSet(
                        id = "s1",
                        targetCount = 10,
                        targetWeight = 60.0,
                        result = completed(count = 10, weight = 60.0)
                    ),
                    sessionSet(id = "s2", targetCount = 10, targetWeight = 60.0, result = SessionSetResultEntity.Skipped),
                ),
            ),
        ),
    )

    /**
     * Два упражнения, у первого — два подхода: запись одного подхода не переводит сессию на второе
     * упражнение сама, поэтому переключение шторкой остаётся осмысленным действием.
     */
    private fun twoExerciseSession(): WorkoutSessionEntity = workoutSession(
        id = SESSION_ID,
        exercises = listOf(
            sessionExercise(
                id = "e1",
                name = "Жим",
                sets = listOf(
                    sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0),
                    sessionSet(id = "s2", targetCount = 10, targetWeight = 60.0),
                ),
            ),
            sessionExercise(
                id = "e2",
                name = "Тяга",
                sets = listOf(sessionSet(id = "s3", targetCount = 12, targetWeight = 50.0)),
            ),
        ),
    )

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
        options = LyteNavOptions(
            popUpTo = ActiveSessionRoute(sessionId = SESSION_ID),
            popUpToInclusive = true
        ),
    )

    /**
     * Финиш: сперва выкинуть завершённую сессию из стека трекера (иначе вкладка сохранилась бы на ней),
     * затем переключиться на «Историю» и положить детали поверх её списка — «назад» уходит в список.
     */
    private fun finishNavigation(): List<NavCommand> = listOf(
        landingReplacingSession(),
        NavCommand.SwitchTab(graphRoute = HistoryTabGraph),
        NavCommand.Forward(
            route = HistorySessionDetailsRoute(sessionId = SESSION_ID),
            options = null
        ),
    )

    private companion object {
        const val SESSION_ID = "session-1"
    }
}

/** Контент как трекинг — большинство проверок про активный подход; падает, если состояние другое. */
private val ActiveSessionUiState.tracking: ActiveSessionContent.Tracking
    get() = content as ActiveSessionContent.Tracking
