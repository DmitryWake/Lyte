package com.nikolaevskii.lyte.feature.splash.presentation.model

/** Фаза сборки логотипа на заставке. */
enum class SplashPhaseUiModel {

    /** Крупная пульсирующая точка — идут стартовые процессы. */
    Blinking,

    /** Точка уменьшается до размера в вордмарке, рядом выезжает текст «Lyte» — переход к основному экрану. */
    Revealing,
}