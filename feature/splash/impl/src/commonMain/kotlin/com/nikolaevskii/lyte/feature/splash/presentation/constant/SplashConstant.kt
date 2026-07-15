package com.nikolaevskii.lyte.feature.splash.presentation.constant

object SplashConstant {

    /** Минимальная длительность фазы [SplashUiState.Blinking], даже если стартовые процессы завершились быстрее. */
    const val SPLASH_MIN_LOADING_DURATION_MS = 2000L

    /** Длительность анимации перехода [SplashUiState.Blinking] → [SplashUiState.Revealing] (см. `SplashScreen.kt`). */
    const val SPLASH_REVEAL_DURATION_MS = 600L
}