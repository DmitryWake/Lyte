package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Компоузовский `shadowElevation` не воспроизводит один в один мягкие, размытые CSS-тени
 * дизайн-системы — значения ниже подобраны визуально под тот же уровень "воздушности".
 */
data class LyteElevation(
    val level1: Dp,
    val level2: Dp,
    val level3: Dp,
    val level4: Dp,
    val level5: Dp,
)

internal val LyteDefaultElevation = LyteElevation(
    level1 = 1.dp,
    level2 = 3.dp,
    level3 = 6.dp,
    level4 = 8.dp,
    level5 = 12.dp,
)

internal val LocalLyteElevation = staticCompositionLocalOf { LyteDefaultElevation }
