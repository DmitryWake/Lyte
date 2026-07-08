package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.withTabularNums

private val ExerciseStripGap = 8.dp
private val ExerciseStripContentPadding = 20.dp
private val ExerciseStripCardMinWidth = 96.dp
private val ExerciseStripCardPaddingHorizontal = 14.dp
private val ExerciseStripCardPaddingVertical = 10.dp
private val ExerciseStripCardGap = 7.dp
private val ExerciseStripCountGap = 6.dp
private val ExerciseStripCheckSize = 14.dp
private val ExerciseStripNameSize = 13.sp
private val ExerciseStripCountSize = 12.sp
private const val ExerciseStripCountAlpha = 0.85f

/**
 * Горизонтальная прокручиваемая полоса упражнений сессии: у каждого — число выполненных подходов
 * и статус (done · current · todo). «Контекстный» вид активной сессии; тап переключает упражнение.
 */
@Composable
fun LyteExerciseStrip(
    items: List<LyteExerciseStripItem>,
    onSelect: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(ExerciseStripGap),
        contentPadding = PaddingValues(horizontal = ExerciseStripContentPadding),
        modifier = modifier,
    ) {
        itemsIndexed(items) { index, item ->
            ExerciseStripCard(item = item, onClick = { onSelect(index) })
        }
    }
}

@Composable
private fun ExerciseStripCard(item: LyteExerciseStripItem, onClick: () -> Unit) {
    val active = item.status == LyteExerciseStripStatus.Current
    val complete = item.status == LyteExerciseStripStatus.Done
    val nameColor: Color = when {
        active -> MaterialTheme.colorScheme.onPrimaryContainer
        complete -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface
    }
    val countColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (active) LyteTheme.elevation.level2 else 0.dp,
        modifier = Modifier.widthIn(min = ExerciseStripCardMinWidth),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ExerciseStripCardGap),
            modifier = Modifier.padding(horizontal = ExerciseStripCardPaddingHorizontal, vertical = ExerciseStripCardPaddingVertical),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = ExerciseStripNameSize),
                color = nameColor,
                maxLines = 1,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ExerciseStripCountGap),
            ) {
                if (complete) {
                    Icon(
                        imageVector = LyteIcons.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(ExerciseStripCheckSize),
                    )
                }
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = ExerciseStripCountSize).withTabularNums(),
                    color = countColor.copy(alpha = ExerciseStripCountAlpha),
                )
            }
        }
    }
}

@Preview
@Composable
private fun LyteExerciseStripPreview() {
    LyteTheme {
        LyteExerciseStrip(
            items = listOf(
                LyteExerciseStripItem(title = "Жим лёжа", subtitle = "3/3", status = LyteExerciseStripStatus.Done),
                LyteExerciseStripItem(title = "Разводка", subtitle = "1/4", status = LyteExerciseStripStatus.Current),
                LyteExerciseStripItem(title = "Брусья", subtitle = "0/3", status = LyteExerciseStripStatus.Todo),
            ),
            modifier = Modifier.padding(vertical = 16.dp),
        )
    }
}
