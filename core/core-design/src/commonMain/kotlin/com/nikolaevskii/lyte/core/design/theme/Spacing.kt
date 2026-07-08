package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Сетка отступов с шагом 4dp. */
data class LyteSpacing(
    val s0: Dp,
    val s1: Dp,
    val s2: Dp,
    val s3: Dp,
    val s4: Dp,
    val s5: Dp,
    val s6: Dp,
    val s8: Dp,
    val s10: Dp,
    val s12: Dp,
    val s16: Dp,
    val s20: Dp,
    val s24: Dp,
)

internal val LyteDefaultSpacing = LyteSpacing(
    s0 = 0.dp,
    s1 = 4.dp,
    s2 = 8.dp,
    s3 = 12.dp,
    s4 = 16.dp,
    s5 = 20.dp,
    s6 = 24.dp,
    s8 = 32.dp,
    s10 = 40.dp,
    s12 = 48.dp,
    s16 = 64.dp,
    s20 = 80.dp,
    s24 = 96.dp,
)

internal val LocalLyteSpacing = staticCompositionLocalOf { LyteDefaultSpacing }
