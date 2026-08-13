package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Заголовки карточек и строк списка. Вынесены из компонентов в общее место, чтобы четыре карточки
 * v2 не разъехались по кеглю и трекингу: анатомия у них одна, и заголовок в ней — та деталь, по
 * которой разница заметнее всего.
 *
 * Размеры и трекинг — из дизайн-бандла и в шкалу M3 не попадают (15.5sp, −0.1/−0.2), поэтому
 * строятся `copy()` поверх ближайшего токена, а не заводятся новой группой токенов темы.
 */

/** Трекинг заголовка карточки — плотнее, чем у `titleMedium`: 16sp иначе выглядит разреженным. */
private val CardTitleTracking = (-0.2).sp

private val RowTitleSize = 15.5.sp
private val RowTitleLineHeight = 20.sp
private val RowTitleTracking = (-0.1).sp

/** Название программы или сессии в карточке: 16/600. */
internal val cardTitleStyle: TextStyle
    @Composable get() = MaterialTheme.typography.titleMedium.copy(letterSpacing = CardTitleTracking)

/** Название упражнения в карточке: 15.5/600 — на полкегля тише названия программы. */
internal val rowTitleStrongStyle: TextStyle
    @Composable get() = rowTitleStyle(weight = FontWeight.SemiBold)

/** Заголовок строки списка: 15.5/500 — строка в списке легче карточки. */
internal val rowTitleMediumStyle: TextStyle
    @Composable get() = rowTitleStyle(weight = FontWeight.Medium)

@Composable
private fun rowTitleStyle(weight: FontWeight): TextStyle = MaterialTheme.typography.titleMedium.copy(
    fontSize = RowTitleSize,
    lineHeight = RowTitleLineHeight,
    fontWeight = weight,
    letterSpacing = RowTitleTracking,
)
