package com.nikolaevskii.lyte.core.design.component.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_reps
import com.nikolaevskii.lyte.core.design.generated.resources.diff_skipped
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.theme.LyteExtendedColors
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import org.jetbrains.compose.resources.stringResource

enum class LyteDiffTone { Met, Positive, Negative, Neutral, Skipped }

private val SET_NOTATION_REGEX = Regex("^([\\d.,]+)\\s*[×x]\\s*([\\d.,]+)$")
private const val SET_VALUE_SEPARATOR = "·"
private val DiffRowPaddingHorizontal = 16.dp
private val DiffRowPaddingVertical = 12.dp
private val DiffRowGap = 12.dp
private val DiffValueGap = 8.dp
private val DiffIndexWidth = 20.dp
private val DiffNumberTracking = (-0.2).sp
private const val DiffIndexAlpha = 0.7f
private const val DiffArrowAlpha = 0.55f
private const val DiffNoteAlpha = 0.85f
private const val DiffMutedNumberAlpha = 0.55f
private const val DiffUnitAlpha = 0.7f
private const val DiffMutedUnitAlpha = 0.45f
private const val DiffSeparatorAlpha = 0.5f
private const val DiffMutedSeparatorAlpha = 0.35f

/**
 * Строка результата подхода «план→факт» (спецификация экрана деталей сессии).
 * [tone]: met (попал точно) · positive (превысил) · negative (недобрал) · neutral · skipped.
 * [target]/[actual] в нотации «10×60» рендерятся с явными единицами (повт/кг), чтобы числа
 * не путались; при [LyteDiffTone.Skipped] вместо них показывается «пропущено».
 */
@Composable
fun LyteDiffRow(
    index: Int,
    tone: LyteDiffTone,
    target: String? = null,
    actual: String? = null,
    note: String? = null,
    modifier: Modifier = Modifier,
) {
    val (foreground, background) = diffColors(tone)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = background,
        contentColor = foreground,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DiffRowGap),
            modifier = Modifier.padding(horizontal = DiffRowPaddingHorizontal, vertical = DiffRowPaddingVertical),
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelMedium.withTabularNums(),
                color = foreground.copy(alpha = DiffIndexAlpha),
                modifier = Modifier.width(DiffIndexWidth),
            )
            if (tone == LyteDiffTone.Skipped) {
                Text(
                    text = stringResource(Res.string.diff_skipped),
                    style = MaterialTheme.typography.bodyMedium,
                    color = foreground,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DiffValueGap),
                    modifier = Modifier.weight(1f),
                ) {
                    target?.let { SetValueText(notation = it, foreground = foreground, muted = true) }
                    Text(text = "→", color = foreground.copy(alpha = DiffArrowAlpha), style = MaterialTheme.typography.bodyMedium)
                    actual?.let { SetValueText(notation = it, foreground = foreground, muted = false) }
                }
            }
            note?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = foreground.copy(alpha = DiffNoteAlpha),
                    fontStyle = FontStyle.Italic,
                )
            }
        }
    }
}

/** Разбирает нотацию «10×60» на (повторы, вес); `null`, если строка не соответствует формату. */
private fun parseSetNotation(notation: String): Pair<String, String>? {
    val match = SET_NOTATION_REGEX.find(notation) ?: return null
    val (reps, weight) = match.destructured
    return reps to weight
}

@Composable
private fun SetValueText(notation: String, foreground: Color, muted: Boolean) {
    val numberStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = DiffNumberTracking).withTabularNums()
    val unitStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
    val numberAlpha = if (muted) DiffMutedNumberAlpha else 1f
    val parsed = parseSetNotation(notation)
    if (parsed == null) {
        Text(text = notation, style = numberStyle, color = foreground.copy(alpha = numberAlpha))
        return
    }
    val (reps, weight) = parsed
    val unitAlpha = if (muted) DiffMutedUnitAlpha else DiffUnitAlpha
    val separatorAlpha = if (muted) DiffMutedSeparatorAlpha else DiffSeparatorAlpha
    Row(verticalAlignment = Alignment.Bottom) {
        Text(text = reps, style = numberStyle, color = foreground.copy(alpha = numberAlpha))
        Text(
            text = stringResource(Res.string.diff_reps),
            style = unitStyle,
            color = foreground.copy(alpha = unitAlpha),
            modifier = Modifier.padding(start = 2.dp),
        )
        Text(
            text = SET_VALUE_SEPARATOR,
            style = unitStyle,
            color = foreground.copy(alpha = separatorAlpha),
            modifier = Modifier.padding(horizontal = 5.dp),
        )
        Text(text = weight, style = numberStyle, color = foreground.copy(alpha = numberAlpha))
        Text(
            text = stringResource(Res.string.diff_weight),
            style = unitStyle,
            color = foreground.copy(alpha = unitAlpha),
            modifier = Modifier.padding(start = 2.dp),
        )
    }
}

@Composable
private fun diffColors(tone: LyteDiffTone): Pair<Color, Color> {
    val extended: LyteExtendedColors = LyteTheme.extendedColors
    return when (tone) {
        LyteDiffTone.Met -> extended.diffMet to extended.diffMetBg
        LyteDiffTone.Positive -> extended.diffPositive to extended.diffPositiveBg
        LyteDiffTone.Negative -> extended.diffNegative to extended.diffNegativeBg
        LyteDiffTone.Neutral -> extended.diffNeutral to extended.diffNeutralBg
        LyteDiffTone.Skipped -> extended.diffSkipped to extended.diffSkippedBg
    }
}

@Preview
@Composable
private fun LyteDiffRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteDiffRow(index = 1, tone = LyteDiffTone.Met, target = "10×60", actual = "10×60")
            LyteDiffRow(index = 2, tone = LyteDiffTone.Positive, target = "10×60", actual = "12×60")
            LyteDiffRow(index = 3, tone = LyteDiffTone.Negative, target = "10×60", actual = "8×60", note = "тяжело")
            LyteDiffRow(index = 4, tone = LyteDiffTone.Skipped)
        }
    }
}
