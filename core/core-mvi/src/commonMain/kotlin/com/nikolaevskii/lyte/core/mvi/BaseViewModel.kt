package com.nikolaevskii.lyte.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<State : UiState, Intent : UiIntent> : ViewModel(), CoroutineScope {

    // viewModelScope.coroutineContext уже содержит SupervisorJob, привязанный к onCleared() —
    // добавление отдельного `+ SupervisorJob()` заменило бы этот Job элемент на новый,
    // ни к чему не привязанный (CoroutineContext.plus заменяет элементы с одинаковым Key),
    // и корутины из launch{}/async{} переживали бы очистку ViewModel.
    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext

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
