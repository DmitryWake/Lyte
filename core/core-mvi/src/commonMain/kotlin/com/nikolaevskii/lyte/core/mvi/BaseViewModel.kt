package com.nikolaevskii.lyte.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<State : UiState, Intent : UiIntent> : ViewModel(), CoroutineScope {

    // Единая воронка ошибок: любой непойманный сбой корутины, запущенной на этом скоупе (`launch { … }`),
    // приходит в [handleError]. `CancellationException` по контракту корутин в обработчик не передаётся,
    // поэтому отмена скоупа (очистка ViewModel, `collectLatest` и т.п.) ошибкой не считается — это
    // централизованно снимает проблему «runCatching глотает cancellation» без ручного rethrow в каждом VM.
    //
    // Ключ у CoroutineExceptionHandler свой (не Job), поэтому `+ handler` НЕ заменяет Job из
    // viewModelScope, привязанный к onCleared() — корутины по-прежнему отменяются при очистке VM.
    // (Отдельный `+ SupervisorJob()` заменил бы этот Job и оторвал бы корутины от очистки — так делать нельзя.)
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable -> handleError(throwable) }

    override val coroutineContext: CoroutineContext
        get() = viewModelScope.coroutineContext + exceptionHandler

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

    /**
     * Куда попадает любая непойманная ошибка корутин VM. По умолчанию — no-op (сбой не роняет процесс).
     * Наследник переопределяет и переводит состояние в свой Error-арм, напр.
     * `updateState { SomeUiState.Error(error.toLyteError()) }`.
     *
     * Экраны с несколькими разными исходами провала (загрузка vs сохранение) ловят конкретную операцию
     * сами (`runCatching`/`try`), а сюда пускают только неожиданное.
     */
    protected open fun handleError(error: Throwable) {
    }

}
