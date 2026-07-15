package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.WorkoutPickerRoute
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Корень вкладки «Трекер» и гейт активной сессии: если в БД есть незавершённая сессия (в т.ч. после
 * смерти процесса), лендинг не показывается — стек вкладки заменяется её экраном. Иначе — лендинг с
 * переходом к выбору программы.
 */
class TrackerLandingViewModel(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<TrackerLandingUiState, TrackerLandingIntent>() {

    init {
        launch { checkActiveSession() }
    }

    override fun onIntent(intent: TrackerLandingIntent) {
        when (intent) {
            // Экран выбора живёт в этой же вкладке — обычный forward-переход.
            TrackerLandingIntent.OpenWorkoutPicker -> lyteNavigator.navigate(WorkoutPickerRoute)
        }
    }

    override fun getInitialState(): TrackerLandingUiState = TrackerLandingUiState.CheckingSession

    private suspend fun checkActiveSession() {
        val activeSession = try {
            workoutSessionRepository.getActiveSession()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Не смогли проверить — не блокируем вкладку, показываем обычный лендинг.
            null
        }
        if (activeSession != null) {
            lyteNavigator.navigate(
                route = ActiveSessionRoute(sessionId = activeSession.id),
                options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
            )
        } else {
            updateState { TrackerLandingUiState.NoActiveSession }
        }
    }
}
