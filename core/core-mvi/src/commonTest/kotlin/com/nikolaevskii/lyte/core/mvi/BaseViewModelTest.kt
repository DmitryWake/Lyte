package com.nikolaevskii.lyte.core.mvi

import kotlin.test.Test
import kotlin.test.assertEquals

class BaseViewModelTest {

    private data class CounterUiState(val count: Int = 0) : UiState

    private sealed interface CounterIntent : UiIntent {
        data object Increment : CounterIntent
    }

    private class CounterViewModel : BaseViewModel<CounterUiState, CounterIntent>() {

        override fun onIntent(intent: CounterIntent) {
            when (intent) {
                CounterIntent.Increment -> updateState { copy(count = count + 1) }
            }
        }

        override fun getInitialState(): CounterUiState = CounterUiState()
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
}
