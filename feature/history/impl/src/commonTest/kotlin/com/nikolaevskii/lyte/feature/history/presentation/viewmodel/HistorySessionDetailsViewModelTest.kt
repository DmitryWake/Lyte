package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.history.completed
import com.nikolaevskii.lyte.feature.history.finishedSessionEntity
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsUiState
import com.nikolaevskii.lyte.feature.history.sessionExercise
import com.nikolaevskii.lyte.feature.history.sessionSet
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HistorySessionDetailsViewModelTest {

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
    fun loadsSessionDetailsOnInit() = runTest(testDispatcher) {
        val session = finishedSessionEntity(
            id = SESSION_ID,
            programName = "Push Day",
            exercises = listOf(
                sessionExercise("e1", "Жим лёжа", listOf(sessionSet("s1", 8, 80.0, completed(8, 80.0)))),
            ),
        )
        val viewModel = viewModel(FakeSessionHistoryRepository(session = session))

        runCurrent()

        val content = assertIs<HistorySessionDetailsUiState.Content>(viewModel.uiState.value)
        assertEquals("Push Day", content.details.programName)
        assertEquals("Жим лёжа", content.details.exercises.single().exerciseName)
    }

    @Test
    fun missingSessionProducesNotFoundError() = runTest(testDispatcher) {
        val viewModel = viewModel(FakeSessionHistoryRepository(session = null))

        runCurrent()

        val error = assertIs<HistorySessionDetailsUiState.Error>(viewModel.uiState.value)
        assertEquals(LyteError.NotFound, error.error)
    }

    @Test
    fun failedLoadSurfacesError() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository().apply { getSessionError = IllegalStateException("boom") }
        val viewModel = viewModel(repository)

        runCurrent()

        val error = assertIs<HistorySessionDetailsUiState.Error>(viewModel.uiState.value)
        // Сырой текст Room/SQLite наружу не утекает — экран получает типизированную ошибку.
        assertIs<LyteError.Unknown>(error.error)
    }

    @Test
    fun deleteDialogOpensAndCloses() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(session = sampleSession())
        val viewModel = viewModel(repository)
        runCurrent()

        viewModel.onIntent(HistorySessionDetailsIntent.OnDeleteClicked)
        assertTrue(assertIs<HistorySessionDetailsUiState.Content>(viewModel.uiState.value).isDeleteDialogVisible)

        viewModel.onIntent(HistorySessionDetailsIntent.OnDeleteDismissed)
        val content = assertIs<HistorySessionDetailsUiState.Content>(viewModel.uiState.value)
        assertEquals(false, content.isDeleteDialogVisible)
        // Закрытие диалога — не удаление.
        assertEquals(emptyList(), repository.deletedSessionIds)
    }

    @Test
    fun confirmedDeleteRemovesSessionAndNavigatesBack() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(session = sampleSession())
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository, navigator)
        runCurrent()

        viewModel.onIntent(HistorySessionDetailsIntent.OnDeleteClicked)
        viewModel.onIntent(HistorySessionDetailsIntent.OnDeleteConfirmed)
        runCurrent()

        assertEquals(listOf(SESSION_ID), repository.deletedSessionIds)
        assertEquals(listOf<NavCommand>(NavCommand.Back), navigator.commandLog)
    }

    @Test
    fun failedDeleteKeepsDetailsAndDoesNotNavigate() = runTest(testDispatcher) {
        val repository = FakeSessionHistoryRepository(session = sampleSession()).apply {
            deleteSessionError = IllegalStateException("boom")
        }
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(repository, navigator)
        runCurrent()

        viewModel.onIntent(HistorySessionDetailsIntent.OnDeleteClicked)
        viewModel.onIntent(HistorySessionDetailsIntent.OnDeleteConfirmed)
        runCurrent()

        val content = assertIs<HistorySessionDetailsUiState.Content>(viewModel.uiState.value)
        // Детали остаются на экране, диалог закрыт, ошибка — баннером.
        assertEquals("Push Day", content.details.programName)
        assertEquals(false, content.isDeleteDialogVisible)
        assertIs<LyteError.Unknown>(content.actionError)
        assertEquals(emptyList(), navigator.commandLog)
    }

    @Test
    fun backClickNavigatesBack() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = viewModel(FakeSessionHistoryRepository(session = null), navigator)
        runCurrent()

        viewModel.onIntent(HistorySessionDetailsIntent.OnBackClicked)

        assertEquals(listOf<NavCommand>(NavCommand.Back), navigator.commandLog)
    }

    private fun viewModel(
        repository: FakeSessionHistoryRepository,
        navigator: FakeLyteNavigator = FakeLyteNavigator(),
    ): HistorySessionDetailsViewModel = HistorySessionDetailsViewModel(
        sessionId = SESSION_ID,
        sessionHistoryRepository = repository,
        lyteNavigator = navigator,
    )

    private fun sampleSession() = finishedSessionEntity(
        id = SESSION_ID,
        programName = "Push Day",
        exercises = listOf(
            sessionExercise("e1", "Жим лёжа", listOf(sessionSet("s1", 8, 80.0, completed(8, 80.0)))),
        ),
    )

    private companion object {
        const val SESSION_ID = "session-1"
    }
}
