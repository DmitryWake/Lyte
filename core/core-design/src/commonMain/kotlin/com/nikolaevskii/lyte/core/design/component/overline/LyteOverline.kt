package com.nikolaevskii.lyte.core.design.component.overline

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme

private val OverlineTracking = 1.4.sp

/**
 * Микро-заголовок капсом: заголовки секций списков («Июль», «Упражнения»), подписи полей степпера
 * («Повт», «Кг»), контекст прогресса («Упражнение 2 из 5»). Всегда над тем, что подписывает; текст
 * приводится к верхнему регистру самим компонентом.
 */
@Composable
fun LyteOverline(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = OverlineTracking),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun LyteOverlinePreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteOverline(text = "Упражнения")
            LyteOverline(text = "Упражнение 2 из 5")
        }
    }
}
