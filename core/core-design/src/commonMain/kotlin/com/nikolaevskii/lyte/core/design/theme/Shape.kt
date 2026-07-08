package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val LyteShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Радиусы за пределами шкалы M3: промежуточные ступени карточек и шитов
 * («чем крупнее контейнер — тем крупнее радиус»).
 */
data class LyteExtendedShapes(
    val largeIncreased: Shape,
    val extraLargeIncreased: Shape,
    val full: Shape,
)

internal val LyteDefaultExtendedShapes = LyteExtendedShapes(
    largeIncreased = RoundedCornerShape(20.dp),
    extraLargeIncreased = RoundedCornerShape(32.dp),
    full = CircleShape,
)

internal val LocalLyteExtendedShapes = staticCompositionLocalOf { LyteDefaultExtendedShapes }
