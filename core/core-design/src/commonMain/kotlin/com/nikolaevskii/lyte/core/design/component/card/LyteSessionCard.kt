package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.theme.withTabularNums

private val SessionCardPaddingHorizontal = 24.dp
private val SessionCardPaddingVertical = 20.dp
private val SessionCardGap = 12.dp
private val SessionCardSubtitleSpacing = 4.dp
private val CardTitleTracking = (-0.3).sp

/**
 * Строка истории тренировок: заголовок, короткая метка справа от него (напр. дата) и подпись
 * снизу. Тексты полностью формирует вызывающая сторона — компонент задаёт только раскладку/стиль.
 */
@Composable
fun LyteSessionCard(
    title: String,
    trailingLabel: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = LyteTheme.elevation.level1),
    ) {
        Column(modifier = Modifier.padding(horizontal = SessionCardPaddingHorizontal, vertical = SessionCardPaddingVertical)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(SessionCardGap),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = CardTitleTracking),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = trailingLabel,
                    style = MaterialTheme.typography.bodySmall.withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = SessionCardSubtitleSpacing),
            )
        }
    }
}

@Preview
@Composable
private fun LyteSessionCardPreview() {
    LyteTheme {
        LyteSessionCard(
            title = "Push Day",
            trailingLabel = "12 июн",
            subtitle = "48 мин · 24 подхода",
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
