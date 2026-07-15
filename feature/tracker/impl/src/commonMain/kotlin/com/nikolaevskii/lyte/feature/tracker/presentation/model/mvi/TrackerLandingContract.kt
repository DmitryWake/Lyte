package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

/**
 * Лендинг — гейт активной сессии (спека 4.3): на входе проверяем БД и, если сессия есть, уводим на её
 * маршрут вместо отрисовки лендинга.
 */
sealed interface TrackerLandingUiState : UiState {

    /** Идёт проверка БД: экран пуст — найдётся сессия, уйдём на её маршрут без вспышки «Нет сессии». */
    data object CheckingSession : TrackerLandingUiState

    /** Активной сессии нет (в т.ч. проверка упала — вкладку не блокируем, показываем лендинг). */
    data object NoActiveSession : TrackerLandingUiState
}

sealed interface TrackerLandingIntent : UiIntent {

    /** Перейти к выбору программы для тренировки. */
    data object OpenWorkoutPicker : TrackerLandingIntent
}
