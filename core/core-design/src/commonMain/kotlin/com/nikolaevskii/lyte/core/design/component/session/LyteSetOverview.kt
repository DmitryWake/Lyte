package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.theme.withTabularNums

private val SetOverviewWidth = 100.dp
private val SetOverviewCurrentWidth = 108.dp
private val SetOverviewGap = 8.dp
private val SetOverviewContentPadding = 20.dp
private val SetOverviewInnerGap = 4.dp
private val SetOverviewPaddingTop = 9.dp
private val SetOverviewPaddingBottom = 10.dp
private val SetOverviewPaddingHorizontal = 8.dp
private val SetOverviewCaptionSize = 10.5.sp
private val SetOverviewCaptionTracking = 0.6.sp
private val SetOverviewValueSize = 13.5.sp
private const val SetOverviewCaptionAlpha = 0.75f

/**
 * Горизонтальная прокручиваемая карусель плашек подходов активного упражнения. Каждая плашка
 * целиком залита своим тоном-результатом, текущая — шире и с тенью. Спутник «крупным планом»
 * текущего подхода — [LyteTrackSetRow]. Подписи/значения приходят готовыми из [LyteSetOverviewItem].
 */
@Composable
fun LyteSetOverview(
    sets: List<LyteSetOverviewItem>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SetOverviewGap),
        contentPadding = PaddingValues(horizontal = SetOverviewContentPadding),
        modifier = modifier,
    ) {
        items(sets) { item ->
            SetPlaque(item = item)
        }
    }
}

@Composable
private fun SetPlaque(item: LyteSetOverviewItem) {
    val isCurrent = item.tone == LyteSetOverviewTone.Current
    val (background, foreground) = setOverviewTone(item.tone)

    Surface(
        shape = MaterialTheme.shapes.large,
        color = background,
        shadowElevation = if (isCurrent) LyteTheme.elevation.level1 else 0.dp,
        modifier = Modifier.width(if (isCurrent) SetOverviewCurrentWidth else SetOverviewWidth),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SetOverviewInnerGap),
            modifier = Modifier.padding(
                top = SetOverviewPaddingTop,
                bottom = SetOverviewPaddingBottom,
                start = SetOverviewPaddingHorizontal,
                end = SetOverviewPaddingHorizontal,
            ),
        ) {
            Text(
                text = item.caption.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = SetOverviewCaptionSize, letterSpacing = SetOverviewCaptionTracking),
                color = foreground.copy(alpha = SetOverviewCaptionAlpha),
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = SetOverviewValueSize, fontWeight = FontWeight.Bold).withTabularNums(),
                color = foreground,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun setOverviewTone(tone: LyteSetOverviewTone): Pair<Color, Color> {
    val extended = LyteTheme.extendedColors
    return when (tone) {
        LyteSetOverviewTone.Current -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        LyteSetOverviewTone.Hit -> extended.diffMetBg to extended.diffMet
        LyteSetOverviewTone.Exceed -> extended.diffPositiveBg to extended.diffPositive
        LyteSetOverviewTone.Miss -> extended.diffNegativeBg to extended.diffNegative
        LyteSetOverviewTone.Skip -> extended.diffSkippedBg to extended.diffSkipped
        LyteSetOverviewTone.Todo -> MaterialTheme.colorScheme.surfaceContainerLow to MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Preview
@Composable
private fun LyteSetOverviewPreview() {
    LyteTheme {
        LyteSetOverview(
            sets = listOf(
                LyteSetOverviewItem(caption = "№1", value = "10×60 кг", tone = LyteSetOverviewTone.Hit),
                LyteSetOverviewItem(caption = "№2", value = "12×60 кг", tone = LyteSetOverviewTone.Exceed),
                LyteSetOverviewItem(caption = "Сейчас", value = "10×62.5 кг", tone = LyteSetOverviewTone.Current),
                LyteSetOverviewItem(caption = "№4", value = "10×62.5 кг", tone = LyteSetOverviewTone.Todo),
            ),
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
}
