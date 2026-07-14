package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.history.HistorySessionDetailsRoute
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryUiState
import com.nikolaevskii.lyte.feature.history.presentation.model.toMonthGroups
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

class HistoryViewModel(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<HistoryUiState, HistoryIntent>() {

    init {
        launch { loadSessions() }
    }

    override fun onIntent(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.OnScreenShown -> launch { loadSessions() }
            is HistoryIntent.OnSessionClicked ->
                lyteNavigator.navigate(HistorySessionDetailsRoute(sessionId = intent.id))
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
                // Зону берём в точке использования, а не фиксируем при создании VM: перечитывание
                // списка подхватит смену системной таймзоны.
                val groups = sessions.toMonthGroups(TimeZone.currentSystemDefault())
                updateState { if (groups.isEmpty()) HistoryUiState.Empty else HistoryUiState.Content(groups) }
            }
            .onFailure { error ->
                // Ошибка перечитывания при уже показанном списке не затирает его.
                updateState { if (this is HistoryUiState.Content) this else HistoryUiState.Error(error.message) }
            }
    }
}
