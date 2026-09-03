package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Минимальные зоны касания. Токен, а не константа внутри компонента: правило «палец не должен
 * промахиваться» касается всего кита, а не одного контрола — промах по «Пропустить» вместо «Готово»
 * стоит записанного подхода.
 *
 * [min] — нижняя граница для любого интерактивного элемента (48dp, минимум M3 и WCAG 2.5.5 AAA).
 * [stepper] — крупная кнопка ± в форме, где степпер — герой экрана: 56dp, потому что по ней бьют
 * подряд и вслепую, не глядя на экран.
 */
data class LyteHitTarget(
    val min: Dp,
    val stepper: Dp,
)

internal val LyteDefaultHitTarget = LyteHitTarget(
    min = 48.dp,
    stepper = 56.dp,
)

internal val LocalLyteHitTarget = staticCompositionLocalOf { LyteDefaultHitTarget }
