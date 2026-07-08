package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.rest_add_30
import com.nikolaevskii.lyte.core.design.generated.resources.rest_skip
import com.nikolaevskii.lyte.core.design.generated.resources.rest_title
import org.jetbrains.compose.resources.stringResource

private const val SECONDS_PER_MINUTE = 60
private const val RestTimerStartAngle = -90f
private const val RestTimerFullSweep = 360f
private val RestTimerGap = 32.dp
private val RestTimerRingSize = 240.dp
private val RestTimerStrokeWidth = 16.dp
private val RestTimerButtonGap = 12.dp
private val RestTimerLabelTracking = 1.sp

/**
 * Полноэкранный обратный отсчёт отдыха — единственный намеренно «живой» элемент продукта
 * (см. motion-гайдлайны): непрерывно обновляющееся кольцо и число.
 */
@Composable
fun LyteRestTimerOverlay(
    secondsLeft: Int,
    totalSeconds: Int,
    onSkip: () -> Unit,
    onAdd30: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalSeconds > 0) (secondsLeft.toFloat() / totalSeconds).coerceIn(0f, 1f) else 0f
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val progressColor = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RestTimerGap, Alignment.CenterVertically),
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Text(
            text = stringResource(Res.string.rest_title).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = RestTimerLabelTracking,
        )
        Box(modifier = Modifier.size(RestTimerRingSize), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = Stroke(width = RestTimerStrokeWidth.toPx(), cap = StrokeCap.Round)
                val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
                val topLeft = Offset(stroke.width / 2, stroke.width / 2)
                drawArc(color = trackColor, startAngle = RestTimerStartAngle, sweepAngle = RestTimerFullSweep, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
                drawArc(color = progressColor, startAngle = RestTimerStartAngle, sweepAngle = RestTimerFullSweep * progress, useCenter = false, topLeft = topLeft, size = arcSize, style = stroke)
            }
            Text(
                text = formatRestTime(secondsLeft),
                style = LyteTheme.numericTypography.hero,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RestTimerButtonGap)) {
            LyteButton(text = stringResource(Res.string.rest_add_30), onClick = onAdd30, variant = LyteButtonVariant.Outlined)
            LyteButton(text = stringResource(Res.string.rest_skip), onClick = onSkip, variant = LyteButtonVariant.Tonal)
        }
    }
}

private fun formatRestTime(totalSeconds: Int): String {
    val minutes = (totalSeconds / SECONDS_PER_MINUTE).toString().padStart(2, '0')
    val seconds = (totalSeconds % SECONDS_PER_MINUTE).toString().padStart(2, '0')
    return "$minutes:$seconds"
}

@Preview
@Composable
private fun LyteRestTimerOverlayPreview() {
    LyteTheme {
        LyteRestTimerOverlay(secondsLeft = 42, totalSeconds = 90, onSkip = {}, onAdd30 = {})
    }
}
