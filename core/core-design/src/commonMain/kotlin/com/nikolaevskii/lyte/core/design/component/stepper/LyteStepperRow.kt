package com.nikolaevskii.lyte.core.design.component.stepper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline

private val StepperRowGap = 12.dp
private val StepperRowCaptionWidth = 40.dp

/**
 * Ряд «подпись слева — степпер справа»: `Повт` и `Вес` в фокус-карточке подхода (4.3) и в карточке
 * планового подхода (3.4). Подпись сидит в слоте **фиксированной** ширины, а не по своему тексту:
 * иначе кнопки ± двух составленных друг под другом степперов разъезжались бы по горизонтали, а по
 * ним пользователь целится вслепую.
 *
 * Общий на два компонента намеренно: одно и то же «поставь повторы и вес» не должно выглядеть на
 * планировании и на трекинге по-разному — тот же приём, что `CardTypography` у заголовков карточек.
 */
@Composable
internal fun LyteStepperRow(caption: String, stepper: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StepperRowGap),
        modifier = Modifier.fillMaxWidth(),
    ) {
        LyteOverline(text = caption, modifier = Modifier.width(StepperRowCaptionWidth))
        Box(modifier = Modifier.weight(1f)) { stepper() }
    }
}
