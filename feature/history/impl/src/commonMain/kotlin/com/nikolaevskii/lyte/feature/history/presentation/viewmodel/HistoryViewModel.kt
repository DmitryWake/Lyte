package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryUiState
import com.nikolaevskii.lyte.feature.history.presentation.model.toMonthGroups
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class HistoryViewModel(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val timeZone: TimeZone,
) : BaseViewModel<HistoryUiState, HistoryIntent>() {

    init {
        launch { loadSessions() }
    }

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.OnScreenShown -> launch { loadSessions() }
            // Детали сессии (5.2) — вне объёма текущей задачи; интент зарезервирован под переход.
            is HistoryIntent.OnSessionClicked -> Unit
        }
    }

    override fun getInitialState(): HistoryUiState = HistoryUiState.Loading

    private suspend fun loadSessions() {
        // Уже показываем список — не мигаем индикатором на фоновом перечитывании.
        if (uiStateValue !is HistoryUiState.Content) {
            updateState { HistoryUiState.Loading }
        }
        runCatching { workoutSessionRepository.getFinishedSessions() }
            .onSuccess { sessions ->
                val groups = sessions.toMonthGroups(timeZone)
                updateState { if (groups.isEmpty()) HistoryUiState.Empty else HistoryUiState.Content(groups) }
            }
            .onFailure { error ->
                // Ошибка перечитывания при уже показанном списке не затирает его.
                updateState { if (this is HistoryUiState.Content) this else HistoryUiState.Error(error.message) }
            }
    }
}
