package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.feature.history.TEST_TIME_ZONE
import com.nikolaevskii.lyte.feature.history.finishedSession
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

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
    fun loadsSessionsGroupedByMonthOnInit() = runTest(testDispatcher) {
        val repository = FakeWorkoutSessionRepository(
            finishedSessions = listOf(
                finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52, completedSetCount = 15, totalSetCount = 16),
                finishedSession("2", "Pull Day", LocalDateTime(2026, Month.JUNE, 30, 12, 0), durationMinutes = 58, completedSetCount = 17, totalSetCount = 17),
            ),
        )
        val viewModel = HistoryViewModel(workoutSessionRepository = repository, timeZone = TEST_TIME_ZONE)

        runCurrent()

        val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
        assertEquals(listOf(2026 to 7, 2026 to 6), content.groups.map { it.year to it.monthNumber })
    }

    @Test
    fun emptyRepositoryProducesEmptyState() = runTest(testDispatcher) {
        val viewModel = HistoryViewModel(workoutSessionRepository = FakeWorkoutSessionRepository(), timeZone = TEST_TIME_ZONE)

        runCurrent()

        assertEquals(HistoryUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun screenShownReloadsSessions() = runTest(testDispatcher) {
        val repository = FakeWorkoutSessionRepository(finishedSessions = emptyList())
        val viewModel = HistoryViewModel(workoutSessionRepository = repository, timeZone = TEST_TIME_ZONE)
        runCurrent()
        assertEquals(HistoryUiState.Empty, viewModel.uiState.value)
        repository.finishedSessions = listOf(
            finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52, completedSetCount = 15, totalSetCount = 16),
        )

        viewModel.onIntent(HistoryIntent.OnScreenShown)
        runCurrent()

        val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
        assertEquals("Push Day", content.groups.single().sessions.single().programName)
    }

    @Test
    fun failedLoadSurfacesError() = runTest(testDispatcher) {
        val repository = FakeWorkoutSessionRepository().apply { getFinishedSessionsError = IllegalStateException("boom") }
        val viewModel = HistoryViewModel(workoutSessionRepository = repository, timeZone = TEST_TIME_ZONE)

        runCurrent()

        val error = assertIs<HistoryUiState.Error>(viewModel.uiState.value)
        assertEquals("boom", error.message)
    }

    @Test
    fun refreshFailureKeepsExistingContent() = runTest(testDispatcher) {
        val repository = FakeWorkoutSessionRepository(
            finishedSessions = listOf(
                finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52, completedSetCount = 15, totalSetCount = 16),
            ),
        )
        val viewModel = HistoryViewModel(workoutSessionRepository = repository, timeZone = TEST_TIME_ZONE)
        runCurrent()
        val loaded = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
        repository.getFinishedSessionsError = IllegalStateException("boom")

        viewModel.onIntent(HistoryIntent.OnScreenShown)
        runCurrent()

        // Ошибка перечитывания не затирает уже показанный список.
        assertEquals(loaded, viewModel.uiState.value)
    }

    @Test
    fun sessionClickDoesNotChangeState() = runTest(testDispatcher) {
        val repository = FakeWorkoutSessionRepository(
            finishedSessions = listOf(
                finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52, completedSetCount = 15, totalSetCount = 16),
            ),
        )
        val viewModel = HistoryViewModel(workoutSessionRepository = repository, timeZone = TEST_TIME_ZONE)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(HistoryIntent.OnSessionClicked(id = "1"))

        assertEquals(stateBefore, viewModel.uiState.value)
    }
}
