package com.nikolaevskii.lyte.core.design.component.interaction

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.nikolaevskii.lyte.core.design.LyteTheme

/** Масштаб нажатой кнопки/чипа. Степпер жмётся сильнее — он крупнее и жмут его вслепую. */
internal const val LytePressScale = 0.97f
internal const val LytePressScaleStrong = 0.94f

/**
 * Уменьшение контрола на время нажатия — тактильный отклик дизайн-системы.
 *
 * Своя реализация, потому что подключиться к M3 негде: `Indication` (штатная точка расширения
 * отклика) недоступна — `Button`/`FilterChip`/`IconButton` принимают только `interactionSource`, а
 * подмена `LocalIndication` их не достаёт, они зовут `ripple()` явно. M3 даёт лишь state layer
 * (8% наведение / 12% нажатие) — он остаётся как есть, масштаба в нём нет. Поэтому единственный
 * крючок — слой поверх компонента, ведомый тем же [interactionSource].
 */
@Composable
internal fun Modifier.lytePressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = LytePressScale,
    enabled: Boolean = true,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val motion = LyteTheme.motion
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = tween(durationMillis = motion.durationShort, easing = motion.easingStandard),
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
