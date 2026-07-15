package com.nikolaevskii.lyte.feature.splash.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

sealed interface SplashUiState : UiState {

    /** Идут стартовые процессы — крупная пульсирующая точка. */
    data object Blinking : SplashUiState

    /** Инициализация прошла: точка сжимается, рядом выезжает вордмарк, затем — навигация. */
    data object Revealing : SplashUiState

    /** Инициализация упала: вордмарк + сообщение + «Повторить». */
    data object Error : SplashUiState
}

sealed interface SplashIntent : UiIntent {
    data object Retry : SplashIntent
}
