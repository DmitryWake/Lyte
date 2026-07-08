package com.nikolaevskii.lyte.core.design.component.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.theme.withTabularNums

enum class LyteBadgeTone { Neutral, Primary, Success, Ai }

enum class LyteBadgeSize { Small, Medium }

private val BadgeHeightSmall = 22.dp
private val BadgeHeightMedium = 30.dp
private val BadgeHorizontalPaddingSmall = 8.dp
private val BadgeHorizontalPaddingMedium = 14.dp
private val BadgeTextSizeMedium = 13.sp

/**
 * Компактная метка-пилюля для метаданных (счётчик подходов/упражнений), не уведомление-точка.
 * [LyteBadgeSize.Medium] — крупная табличная «stat-пилюля» (длительность, сводка подходов).
 * Цифры всегда табличные.
 */
@Composable
fun LyteBadge(
    text: String,
    tone: LyteBadgeTone = LyteBadgeTone.Neutral,
    size: LyteBadgeSize = LyteBadgeSize.Small,
    modifier: Modifier = Modifier,
) {
    val (container, content) = badgeColors(tone)
    val height = if (size == LyteBadgeSize.Small) BadgeHeightSmall else BadgeHeightMedium
    val horizontalPadding = if (size == LyteBadgeSize.Small) BadgeHorizontalPaddingSmall else BadgeHorizontalPaddingMedium

    Surface(
        modifier = modifier.height(height),
        shape = LyteTheme.extendedShapes.full,
        color = container,
        contentColor = content,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(PaddingValues(horizontal = horizontalPadding))) {
            Text(text = text, style = badgeTextStyle(size))
        }
    }
}

@Composable
private fun badgeTextStyle(size: LyteBadgeSize): TextStyle = when (size) {
    LyteBadgeSize.Small -> MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium).withTabularNums()
    LyteBadgeSize.Medium -> MaterialTheme.typography.labelLarge.copy(fontSize = BadgeTextSizeMedium).withTabularNums()
}

@Composable
private fun badgeColors(tone: LyteBadgeTone): Pair<Color, Color> {
    val extended = LyteTheme.extendedColors
    return when (tone) {
        LyteBadgeTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurfaceVariant
        LyteBadgeTone.Primary -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        LyteBadgeTone.Success -> extended.diffPositiveBg to extended.diffPositive
        LyteBadgeTone.Ai -> extended.aiAccentContainer to extended.aiAccent
    }
}

@Preview
@Composable
private fun LyteBadgePreview() {
    LyteTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteBadge(text = "5 упр.", tone = LyteBadgeTone.Neutral)
            LyteBadge(text = "14/15", tone = LyteBadgeTone.Primary)
            LyteBadge(text = "+2 кг", tone = LyteBadgeTone.Success)
            LyteBadge(text = "52 мин", tone = LyteBadgeTone.Neutral, size = LyteBadgeSize.Medium)
        }
    }
}
