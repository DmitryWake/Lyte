package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Раскладку каллаута проверяем числами, а не только скриншотами: превью показывает две позиции из
 * четырёх, а решают её границы — «помещается под вырезом», «упёрся в поле экрана», «не помещается
 * ни под, ни над». Промах по любой из них уводит подсказку за экран, откуда её нечем достать.
 *
 * Все величины — пиксели, как в `MeasureScope`.
 */
class CoachMarkCalloutOffsetTest {

    @Test
    fun putsCalloutUnderCutoutWhenItFits() {
        val offset = offsetFor(cutout = Rect(left = 100f, top = 200f, right = 300f, bottom = 260f))

        // Под вырезом: 260 + зазор 14. По горизонтали центр выреза 200 минус половина ширины 150.
        assertEquals(IntOffset(x = 50, y = 274), offset)
    }

    @Test
    fun putsCalloutAboveCutoutWhenItDoesNotFitUnder() {
        // Под вырезом остаётся 900 - 860 = 40px — каллаут туда не влезает.
        val offset = offsetFor(cutout = Rect(left = 100f, top = 800f, right = 300f, bottom = 860f))

        // Над вырезом: 800 - зазор 14 - высота 200.
        assertEquals(586, offset.y)
    }

    @Test
    fun keepsCalloutUnderCutoutWhenItFitsExactly() {
        // Ровно впритык: 660 + 14 + 200 + 12 == 886 <= 900. Граница должна остаться «под вырезом».
        val offset = offsetFor(cutout = Rect(left = 100f, top = 600f, right = 300f, bottom = 674f))

        assertEquals(688, offset.y)
    }

    @Test
    fun clampsCalloutToScreenMarginsInsteadOfCenteringOnEdgeTarget() {
        val nearLeft = offsetFor(cutout = Rect(left = 0f, top = 100f, right = 60f, bottom = 160f))
        val nearRight = offsetFor(cutout = Rect(left = 340f, top = 100f, right = 400f, bottom = 160f))

        assertEquals(MARGIN, nearLeft.x)
        // Правое поле: 400 (ширина экрана) - 300 (каллаут) - 12.
        assertEquals(88, nearRight.x)
    }

    @Test
    fun pinsCalloutToTopMarginWhenItFitsNeitherUnderNorAboveTheCutout() {
        // Высокая цель на низком экране: снизу 30px, сверху 40px — каллаут не помещается никуда.
        val offset = offsetFor(
            cutout = Rect(left = 100f, top = 40f, right = 300f, bottom = 270f),
            containerHeight = 300,
        )

        assertEquals(MARGIN, offset.y)
    }

    @Test
    fun survivesContainerNarrowerThanCallout() {
        // Экран уже каллаута: верхняя граница прижима оказалась бы меньше нижней, и наивный
        // coerceIn упал бы с IllegalArgumentException.
        val offset = offsetFor(
            cutout = Rect(left = 10f, top = 100f, right = 90f, bottom = 160f),
            containerWidth = 200,
        )

        assertEquals(MARGIN, offset.x)
    }

    private fun offsetFor(
        cutout: Rect,
        containerWidth: Int = 400,
        containerHeight: Int = 900,
    ): IntOffset = lyteCoachMarkCalloutOffset(
        cutout = cutout,
        calloutSize = IntSize(width = CALLOUT_WIDTH, height = CALLOUT_HEIGHT),
        containerSize = IntSize(width = containerWidth, height = containerHeight),
        gap = GAP,
        margin = MARGIN,
    )

    private companion object {
        const val CALLOUT_WIDTH = 300
        const val CALLOUT_HEIGHT = 200
        const val GAP = 14
        const val MARGIN = 12
    }
}
