package com.nikolaevskii.lyte.core.mvi

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private data class CounterUiState(val count: Int = 0) : UiState

    private sealed interface CounterIntent : UiIntent {
        data object Increment : CounterIntent
    }

    private class CounterViewModel : BaseViewModel<CounterUiState, CounterIntent>() {

        val handledErrors = mutableListOf<Throwable>()

        override fun onIntent(intent: CounterIntent) {
            when (intent) {
                CounterIntent.Increment -> updateState { copy(count = count + 1) }
            }
        }

        override fun getInitialState(): CounterUiState = CounterUiState()

        override fun handleError(error: Throwable) {
            handledErrors += error
        }

        fun launchFailing(error: Throwable) {
            launch { throw error }
        }

        fun launchCancelling() {
            launch { throw CancellationException("cancelled") }
        }
    }

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateComesFromGetInitialState() {
        val viewModel = CounterViewModel()

        assertEquals(expected = CounterUiState(count = 0), actual = viewModel.uiState.value)
    }

    @Test
    fun updateStateAppliesCopyModifier() {
        val viewModel = CounterViewModel()

        viewModel.onIntent(CounterIntent.Increment)
        viewModel.onIntent(CounterIntent.Increment)

        assertEquals(expected = 2, actual = viewModel.uiState.value.count)
    }

    @Test
    fun uncaughtLaunchFailureIsRoutedToHandleError() = runTest(testDispatcher) {
        val viewModel = CounterViewModel()
        val failure = IllegalStateException("boom")

        viewModel.launchFailing(failure)
        advanceUntilIdle()

        assertEquals(expected = listOf<Throwable>(failure), actual = viewModel.handledErrors.toList())
    }

    @Test
    fun cancellationDoesNotReachHandleError() = runTest(testDispatcher) {
        val viewModel = CounterViewModel()

        viewModel.launchCancelling()
        advanceUntilIdle()

        assertTrue(viewModel.handledErrors.isEmpty())
    }

    @Test
    fun toLyteErrorMapsMarkerExceptions() {
        assertEquals(expected = LyteError.NotFound, actual = LyteNotFoundException().toLyteError())
        assertEquals(expected = LyteError.Storage, actual = LyteStorageException().toLyteError())

        val cause = IllegalArgumentException("x")
        assertEquals(expected = LyteError.Unknown(cause), actual = cause.toLyteError())
    }
}
