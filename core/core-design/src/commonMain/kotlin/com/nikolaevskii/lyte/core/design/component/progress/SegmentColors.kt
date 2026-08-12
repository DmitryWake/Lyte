package com.nikolaevskii.lyte.core.design.component.progress

import androidx.compose.ui.graphics.Color

/**
 * Заливка и обводка одного сегмента [LyteProgressTrack]. [ring] отличает «полый» сегмент —
 * пропущенный подход и промах рисуются контуром, а не цветом, чтобы не спорить с закрашенными.
 *
 * Пара, а не два отдельных параметра: цвет заливки и наличие обводки задаются одним и тем же
 * `when` по тону и по состоянию подхода, и разъезжаться им нельзя.
 */
internal data class SegmentColors(
    val fill: Color,
    val ring: Color? = null,
)
