package com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

/**
 * Лендинг — гейт активной сессии (спека 4.3): на входе проверяем БД и, если сессия есть, уводим на её
 * маршрут вместо отрисовки лендинга. [isCheckingSession] = true на старте: пока идёт проверка, экран
 * пуст — без вспышки «Нет активной сессии» перед редиректом (локальный запрос быстрый, спиннер бы
 * только мигал).
 */
data class TrackerLandingUiState(
    val isCheckingSession: Boolean = true,
) : UiState

sealed interface TrackerLandingIntent : UiIntent {

    /** Перейти к выбору программы для тренировки. */
    data object OpenWorkoutPicker : TrackerLandingIntent
}
