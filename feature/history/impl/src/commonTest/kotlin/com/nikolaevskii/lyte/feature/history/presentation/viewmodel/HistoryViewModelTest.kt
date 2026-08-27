package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.history.HistorySessionDetailsRoute
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
import kotlin.time.Clock
import kotlin.time.Instant

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

    // VM берёт системную таймзону в точке использования, а тест-данные строятся в UTC, поэтому даты
    // взяты в середине месяца в полдень: сдвиг любой реальной зоны (±14 ч) не переносит их в соседний
    // месяц, и утверждение не зависит от зоны машины. Точная логика разбора даты — в тесте маппера,
    // где зона задаётся явно.
    @Test
    fun loadsSessionsGroupedByMonthOnInit() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(
            finishedSessions = listOf(
                finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 15, 12, 0), durationMinutes = 52),
                finishedSession("2", "Pull Day", LocalDateTime(2026, Month.JUNE, 15, 12, 0), durationMinutes = 58),
            ),
        )
        val viewModel = viewModel(repository)

        runCurrent()

        val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
        assertEquals(listOf(2026 to 7, 2026 to 6), content.groups.map { it.year to it.monthNumber })
    }

    @Test
    fun emptyRepositoryProducesEmptyState() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeSessionHistoryRepository())

        runCurrent()

        assertEquals(HistoryUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun screenShownReloadsSessions() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(finishedSessions = emptyList())
        val viewModel = viewModel(repository)
        runCurrent()
        assertEquals(HistoryUiState.Empty, viewModel.uiState.value)
        repository.finishedSessions = listOf(
            finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52),
        )

        viewModel.onIntent(HistoryIntent.OnScreenShown)
        runCurrent()

        val content = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
        assertEquals("Push Day", content.groups.single().sessions.single().programName)
    }

    @Test
    fun failedLoadSurfacesError() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository().apply { getFinishedSessionsError = IllegalStateException("boom") }
        val viewModel = viewModel(repository)

        runCurrent()

        val error = assertIs<HistoryUiState.Error>(viewModel.uiState.value)
        assertEquals("boom", error.message)
    }

    @Test
    fun refreshFailureKeepsExistingContent() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(
            finishedSessions = listOf(
                finishedSession("1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52),
            ),
        )
        val viewModel = viewModel(repository)
        runCurrent()
        val loaded = assertIs<HistoryUiState.Content>(viewModel.uiState.value)
        repository.getFinishedSessionsError = IllegalStateException("boom")

        viewModel.onIntent(HistoryIntent.OnScreenShown)
        runCurrent()

        // Ошибка перечитывания не затирает уже показанный список.
        assertEquals(loaded, viewModel.uiState.value)
    }

    @Test
    fun sessionClickNavigatesToDetailsWithoutChangingState() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(
            finishedSessions = listOf(
                finishedSession("s1", "Push Day", LocalDateTime(2026, Month.JULY, 2, 12, 0), durationMinutes = 52),
            ),
        )
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository, navigator)
        runCurrent()
        val stateBefore = viewModel.uiState.value

        viewModel.onIntent(HistoryIntent.OnSessionClicked(id = "s1"))

        assertEquals(listOf<NavCommand>(NavCommand.Forward(HistorySessionDetailsRoute(sessionId = "s1"), null)), navigator.commandLog)
        assertEquals(stateBefore, viewModel.uiState.value)
    }

    private fun viewModel(
        repository: FakeSessionHistoryRepository,
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): HistoryViewModel = HistoryViewModel(
        sessionHistoryRepository = repository,
        lyteNavigator = navigator,
        clock = FixedClock(TEST_NOW),
    )

    /** Часы прибиты: относительная дата карточки не должна зависеть от дня запуска тестов. */
    private class FixedClock(private val now: Instant) : Clock {
        override fun now(): Instant = now
    }

    private companion object {
        val TEST_NOW: Instant = Instant.parse("2026-08-20T12:00:00Z")
    }
}
