package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.tracker.WorkoutPickerRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingUiState

/**
 * Корень вкладки «Трекер». Пока умеет только показать лендинг и увести к выбору программы; с приходом
 * флоу активной сессии (спека 4.3) здесь появится её проверка и переход на маршрут сессии.
 */
class TrackerLandingViewModel(
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<TrackerLandingUiState, TrackerLandingIntent>() {

    override fun onIntent(intent: TrackerLandingIntent) {
        when (intent) {
            // Экран выбора живёт в этой же вкладке — обычный forward-переход.
            TrackerLandingIntent.OpenWorkoutPicker -> lyteNavigator.navigate(WorkoutPickerRoute)
        }
    }

    override fun getInitialState(): TrackerLandingUiState = TrackerLandingUiState
}
