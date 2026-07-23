package com.nikolaevskii.lyte.feature.splash.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

sealed interface SplashUiState : UiState {

    /** Идут стартовые процессы: вордмарк «Lyte.» на месте, лаймовая точка мягко «дышит». */
    data object Loading : SplashUiState

    /** Инициализация прошла: вордмарк плавно уходит (растворение + лёгкий подъём), затем — навигация. */
    data object Exiting : SplashUiState

    /** Инициализация упала: вордмарк + сообщение + «Повторить». */
    data object Error : SplashUiState
}

sealed interface SplashIntent : UiIntent {
    data object Retry : SplashIntent
}
