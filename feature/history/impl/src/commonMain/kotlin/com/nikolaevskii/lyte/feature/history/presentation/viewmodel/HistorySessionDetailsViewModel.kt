package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.mvi.LyteNotFoundException
import com.nikolaevskii.lyte.core.mvi.toLyteError
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsUiState
import com.nikolaevskii.lyte.feature.history.presentation.model.toDetailsUiModel
import com.nikolaevskii.lyte.core.session.domain.repository.SessionHistoryRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class HistorySessionDetailsViewModel(
    private val sessionId: String,
    private val sessionHistoryRepository: SessionHistoryRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<HistorySessionDetailsUiState, HistorySessionDetailsIntent>() {

    init {
        launch { load() }
    }

    override fun onIntent(intent: HistorySessionDetailsIntent) {
        when (intent) {
            HistorySessionDetailsIntent.OnBackClicked -> lyteNavigator.back()

            HistorySessionDetailsIntent.OnDeleteClicked -> updateContent { copy(isDeleteDialogVisible = true) }

            HistorySessionDetailsIntent.OnDeleteDismissed -> updateContent { copy(isDeleteDialogVisible = false) }

            HistorySessionDetailsIntent.OnDeleteConfirmed -> launch { confirmDelete() }
        }
    }

    override fun getInitialState(): HistorySessionDetailsUiState = HistorySessionDetailsUiState.Loading

    private suspend fun load() {
        updateState { HistorySessionDetailsUiState.Loading }
        // Сессии с таким id нет — это LyteError.NotFound, а не «неизвестный сбой»: экран покажет
        // «сессия не найдена», а не общий текст ошибки.
        runCatching { sessionHistoryRepository.getSession(sessionId) ?: throw LyteNotFoundException() }
            .onSuccess { session ->
                // Зону берём в точке использования, а не фиксируем при создании VM.
                val details = session.toDetailsUiModel(TimeZone.currentSystemDefault())
                updateState { HistorySessionDetailsUiState.Content(details = details) }
            }
            .onFailure { error ->
                updateState { HistorySessionDetailsUiState.Error(error.toLyteError()) }
            }
    }

    /**
     * Удаление обёрнуто в runCatching (без него сбой DAO уронил бы процесс необработанным исключением
     * в viewModelScope). Успех — уходим назад, в список: он перечитывается при возврате сам. Провал —
     * баннер над деталями, а не подмена экрана: запись всё ещё на месте и её есть что показать.
     */
    private suspend fun confirmDelete() {
        updateContent { copy(isDeleteDialogVisible = false, actionError = null) }
        runCatching { sessionHistoryRepository.deleteSession(sessionId) }
            .onSuccess { lyteNavigator.back() }
            .onFailure { error -> updateContent { copy(actionError = error.toLyteError()) } }
    }

    private fun updateContent(
        transform: HistorySessionDetailsUiState.Content.() -> HistorySessionDetailsUiState.Content,
    ) {
        updateState {
            (this as? HistorySessionDetailsUiState.Content)?.transform() ?: this
        }
    }

    // Непойманный сбой корутины VM — общая ошибка экрана.
    override fun handleError(error: Throwable) {
        updateState { HistorySessionDetailsUiState.Error(error.toLyteError()) }
    }
}
