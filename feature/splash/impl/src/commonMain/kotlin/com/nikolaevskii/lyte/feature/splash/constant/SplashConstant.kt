package com.nikolaevskii.lyte.feature.splash.constant

object SplashConstant {

    /** Минимальная длительность фазы [SplashPhaseUiModel.Blinking], даже если стартовые процессы завершились быстрее. */
    const val SPLASH_MIN_LOADING_DURATION_MS = 2000L

    /** Длительность анимации перехода [SplashPhaseUiModel.Blinking] → [SplashPhaseUiModel.Revealing] (см. `SplashScreen.kt`). */
    const val SPLASH_REVEAL_DURATION_MS = 600L
}