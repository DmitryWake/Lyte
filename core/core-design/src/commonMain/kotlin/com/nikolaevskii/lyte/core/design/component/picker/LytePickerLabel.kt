package com.nikolaevskii.lyte.core.design.component.picker

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val PickerLabelGap = 8.dp

/**
 * Подпись над сеткой пикера. Тише заголовка и не `LyteOverline`: это не рубрика раздела, а имя
 * поля формы — обычный регистр, без разрядки. Общая на оба пикера, чтобы «Цвет» и «Знак» в одной
 * шторке не разъезжались.
 */
@Composable
internal fun LytePickerLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = PickerLabelGap),
    )
}
