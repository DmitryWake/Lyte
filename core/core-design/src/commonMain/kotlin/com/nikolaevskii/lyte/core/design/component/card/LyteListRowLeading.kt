package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.ui.graphics.vector.ImageVector
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Ведущий элемент строки [LyteListRow] — маркер **или** иконка. Sealed-тип, а не пара
 * взаимоисключающих nullable-полей: строка с маркером и иконкой одновременно бессмысленна, и
 * компонент не должен разбираться в приоритетах по месту.
 */
sealed interface LyteListRowLeading {

    /** Строка про упражнение или программу: круг-маркер с цветом и знаком движения. */
    data class Mark(val accent: LyteAccent, val glyph: LyteExerciseGlyph) : LyteListRowLeading

    /** Строка про что-то другое (настройка, ссылка): обычная иконка словаря. */
    data class Icon(val icon: ImageVector) : LyteListRowLeading
}
