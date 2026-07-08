package com.nikolaevskii.lyte.core.design.component.datadisplay

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.nikolaevskii.lyte.core.design.LyteTheme

enum class LyteStopwatchSize { Hero, Large }

private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3600

/** Числовой герой-дисплей общего времени сессии (спецификация 4.3, элемент 1). Всегда табличный. */
@Composable
fun LyteSessionStopwatch(
    seconds: Int,
    size: LyteStopwatchSize = LyteStopwatchSize.Hero,
    modifier: Modifier = Modifier,
) {
    val baseStyle = if (size == LyteStopwatchSize.Hero) LyteTheme.numericTypography.hero else LyteTheme.numericTypography.large
    Text(
        text = formatStopwatch(seconds),
        style = baseStyle.copy(fontWeight = FontWeight.Medium, textAlign = TextAlign.Center),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

private fun formatStopwatch(totalSeconds: Int): String {
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    val parts = if (hours > 0) listOf(hours, minutes, seconds) else listOf(minutes, seconds)
    return parts.joinToString(separator = ":") { it.toString().padStart(2, '0') }
}

@Preview
@Composable
private fun LyteSessionStopwatchPreview() {
    LyteTheme {
        LyteSessionStopwatch(seconds = 4225)
    }
}
