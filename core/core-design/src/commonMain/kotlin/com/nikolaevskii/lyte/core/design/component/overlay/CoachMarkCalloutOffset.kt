package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/**
 * Куда встаёт каллаут [LyteCoachMark] относительно выреза. Всё в пикселях: функция вызывается из
 * `MeasureScope`, где размеры уже в них, и специально не зависит ни от плотности, ни от композиции —
 * поэтому проверяется числами (см. `CoachMarkCalloutOffsetTest`), а не только скриншотами.
 *
 * По горизонтали каллаут центрируется по вырезу и прижимается к полям [margin]: у цели с краю экрана
 * центрирование ушло бы за границу.
 *
 * По вертикали — **под вырезом, если он туда помещается целиком, иначе над ним**. Макет решает это
 * по прикидке высоты каллаута (`PHONE_H - (y + h) > 170`), здесь высота уже измерена, поэтому
 * прикидка не нужна. Если не помещается и сверху (высокая цель на низком экране), каллаут
 * прижимается к верхнему полю и ляжет на вырез: перекрытая подсветка лучше уехавшей за экран
 * подсказки, которую нечем прокрутить.
 */
internal fun lyteCoachMarkCalloutOffset(
    cutout: Rect,
    calloutSize: IntSize,
    containerSize: IntSize,
    gap: Int,
    margin: Int,
): IntOffset {
    val maxLeft = (containerSize.width - calloutSize.width - margin).coerceAtLeast(margin)
    val left = (cutout.center.x - calloutSize.width / 2f).roundToInt().coerceIn(margin, maxLeft)
    val belowTop = cutout.bottom.roundToInt() + gap
    val fitsBelow = belowTop + calloutSize.height + margin <= containerSize.height
    val top = if (fitsBelow) {
        belowTop
    } else {
        (cutout.top.roundToInt() - gap - calloutSize.height).coerceAtLeast(margin)
    }
    return IntOffset(x = left, y = top)
}
