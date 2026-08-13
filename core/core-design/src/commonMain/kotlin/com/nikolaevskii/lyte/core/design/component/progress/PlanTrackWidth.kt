package com.nikolaevskii.lyte.core.design.component.progress

import androidx.compose.ui.unit.Dp

/**
 * Ширина трека плана в узком слоте — или `null`, если сегменты в него уже не помещаются.
 *
 * [LyteProgressTrack] сам ширину не выбирает: он делит отданную ему ширину между сегментами поровну.
 * В сводке сессии во всю ширину этого достаточно, а вот в слоте карточки упражнения — нет: зазоры
 * съедают фиксированную долю, и с ростом числа подходов сегмент вырождается. При слоте 56dp и зазоре
 * 4dp восемь подходов дают сегмент 3.5dp — при высоте 5dp это уже не пилюля, а точка, и трек
 * перестаёт читаться как «сколько подходов».
 *
 * Поэтому сегмент зажат с двух сторон:
 * - [maxSegmentWidth] — чтобы при одном-трёх подходах трек не растягивался на весь слот, а выглядел
 *   так же, как раньше: лишнее место достаётся только тесным трекам;
 * - [minSegmentWidth] — порог читаемости. Ниже него функция возвращает `null`, и вызывающая сторона
 *   **не рисует трек вовсе**: он избыточен к подписи «N подходов», и когда его нельзя охватить
 *   взглядом, он превращается в шум. Число подходов при этом не теряется — оно в подписи.
 *
 * Возвращается ширина ровно под [setCount] сегментов вместе с зазорами, поэтому слот стоит
 * резервировать целиком (трек прижимается к его началу): тогда подписи у всех карточек списка
 * начинаются с одного места, независимо от числа подходов.
 */
internal fun lytePlanTrackWidth(
    setCount: Int,
    slotWidth: Dp,
    minSegmentWidth: Dp,
    maxSegmentWidth: Dp,
    segmentGap: Dp = LyteProgressTrackSegmentGap,
): Dp? {
    if (setCount <= 0) return null
    val gaps = segmentGap * (setCount - 1)
    val available = (slotWidth - gaps) / setCount
    if (available < minSegmentWidth) return null
    val segment = available.coerceAtMost(maxSegmentWidth)
    return segment * setCount + gaps
}
