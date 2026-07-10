package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

/**
 * У лендинга пока нет состояния: сессии в домене ещё нет, и показывать нечего, кроме «нет активной
 * сессии». Когда появится флоу активной сессии (спека 4.3), это станет `data class` с ветками
 * «проверяем» / «сессии нет», а сам экран — гейтом: найдя сессию, он уводит на её маршрут вместо
 * отрисовки лендинга.
 */
data object TrackerLandingUiState : UiState

sealed interface TrackerLandingIntent : UiIntent {

    /** Перейти к выбору программы для тренировки. */
    data object OpenWorkoutPicker : TrackerLandingIntent
}
