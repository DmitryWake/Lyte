package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Токены движения (кривые и длительности M3). Переходы быстрые и осмысленные, без пружин и
 * отскоков: это инструмент, которым пользуются между подходами, а не развлекательное приложение.
 *
 * Длительности — миллисекунды, поэтому ложатся прямо в `tween(durationMillis = …, easing = …)`.
 * Все анимации дизайн-системы и экранов берут значения отсюда, а не подбирают свои.
 */
data class LyteMotion(
    val durationShort: Int,
    val durationMedium: Int,
    val durationLong: Int,
    val easingStandard: Easing,
    val easingEmphasized: Easing,
    val easingDecelerate: Easing,
    val easingAccelerate: Easing,
)

// Аргументы позиционные — это те же четыре числа и в том же порядке, что в CSS `cubic-bezier()`.
internal val LyteDefaultMotion = LyteMotion(
    durationShort = 150,
    durationMedium = 250,
    durationLong = 400,
    easingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    easingEmphasized = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f),
    easingDecelerate = CubicBezierEasing(0f, 0f, 0f, 1f),
    easingAccelerate = CubicBezierEasing(0.3f, 0f, 1f, 1f),
)

internal val LocalLyteMotion = staticCompositionLocalOf { LyteDefaultMotion }
