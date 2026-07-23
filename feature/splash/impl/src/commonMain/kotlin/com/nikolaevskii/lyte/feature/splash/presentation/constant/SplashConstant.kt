package com.nikolaevskii.lyte.feature.splash.presentation.constant

object SplashConstant {

    /** Минимальная длительность фазы [SplashUiState.Loading], даже если стартовые процессы завершились быстрее. */
    const val SPLASH_MIN_LOADING_DURATION_MS = 1600L

    /** Длительность outro-анимации [SplashUiState.Exiting] (растворение вордмарка) перед навигацией (см. `SplashScreen.kt`). */
    const val SPLASH_EXIT_DURATION_MS = 450L
}