package com.nikolaevskii.lyte.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<State : UiState, Intent : UiIntent> : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext + SupervisorJob()

    val uiState: StateFlow<State>
        get() = _uiState

    protected val uiStateValue: State
        get() = _uiState.value

    private val _uiState: MutableStateFlow<State> by lazy {
        MutableStateFlow(getInitialState())
    }

    abstract fun onIntent(intent: Intent)
    protected abstract fun getInitialState(): State

    protected fun updateState(modifier: State.() -> State) {
        _uiState.update { it.modifier() }
    }

}
