package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsUiState
import com.nikolaevskii.lyte.feature.history.presentation.model.toDetailsUiModel
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class HistorySessionDetailsViewModel(
    private val sessionId: String,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<HistorySessionDetailsUiState, HistorySessionDetailsIntent>() {

    init {
        launch { load() }
    }

    override fun onIntent(intent: HistorySessionDetailsIntent) {
        when (intent) {
            HistorySessionDetailsIntent.OnBackClicked -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): HistorySessionDetailsUiState = HistorySessionDetailsUiState.Loading

    private suspend fun load() {
        updateState { HistorySessionDetailsUiState.Loading }
        runCatching { workoutSessionRepository.getSession(sessionId) }
            .onSuccess { session ->
                // Зону берём в точке использования, а не фиксируем при создании VM.
                val details = session?.toDetailsUiModel(TimeZone.currentSystemDefault())
                updateState {
                    if (details == null) {
                        HistorySessionDetailsUiState.Error(message = null)
                    } else {
                        HistorySessionDetailsUiState.Content(details = details)
                    }
                }
            }
            .onFailure { error -> updateState { HistorySessionDetailsUiState.Error(message = error.message) } }
    }
}
