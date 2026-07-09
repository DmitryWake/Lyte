package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_edit_sets
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_remove_from_program
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import org.jetbrains.compose.resources.stringResource

private val ExerciseCardPaddingTop = 12.dp
private val ExerciseCardPaddingBottom = 14.dp
private val ExerciseCardPaddingHorizontal = 10.dp
private val ExerciseCardRowGap = 4.dp
private val ExerciseCardHandleSize = 20.dp
private val ExerciseCardHandleGap = 8.dp
private val ExerciseCardContentStart = 28.dp
private val ExerciseCardPlanSpacing = 8.dp
private val ExerciseCardSummarySpacing = 4.dp
private val ExerciseCardPillGap = 5.dp
private val ExerciseCardPillPaddingHorizontal = 10.dp
private val ExerciseCardPillPaddingVertical = 4.dp
private val ExerciseCardActionSize = 38.dp
private val ExerciseCardPillTextSize = 12.5.sp
private const val ExerciseCardTitleMaxLines = 2

/**
 * Строка упражнения в редакторе программы: drag-хэндл, название (до 2 строк), действия
 * «редактировать» и «убрать», и — снизу — раскладка подходов. [setLabels] (готовые подписи
 * подходов от вызывающей стороны) рендерятся переносящимся рядом пилюль и имеют приоритет над
 * компактным [summary]; единицы/формат числа выбирает вызывающая сторона.
 *
 * Сам компонент жестов не реализует: [dragHandleModifier] — точка подключения для
 * `pointerInput`/`detectDragGestures` вызывающей стороны, привязанная к хэндлу, а не ко всей строке,
 * чтобы drag не конфликтовал со скроллом списка.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyteExerciseCard(
    title: String,
    summary: String? = null,
    setLabels: List<String>? = null,
    draggable: Boolean = true,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    dragHandleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
) {
    val contentStartPadding = if (draggable) ExerciseCardContentStart else 0.dp

    Surface(
        modifier = modifier,
        shape = LyteTheme.extendedShapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level1,
    ) {
        Column(
            modifier = Modifier.padding(
                start = ExerciseCardPaddingHorizontal,
                top = ExerciseCardPaddingTop,
                end = ExerciseCardPaddingHorizontal,
                bottom = ExerciseCardPaddingBottom,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ExerciseCardRowGap),
            ) {
                if (draggable) {
                    Icon(
                        imageVector = LyteIcons.GripVertical,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = dragHandleModifier.padding(end = ExerciseCardHandleGap).size(ExerciseCardHandleSize),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = ExerciseCardTitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .then(clickableTitleModifier(onClick)),
                )
                onEdit?.let {
                    LyteIconButton(
                        icon = LyteIcons.Edit,
                        contentDescription = stringResource(Res.string.a11y_edit_sets),
                        onClick = it,
                        size = ExerciseCardActionSize,
                    )
                }
                onRemove?.let {
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = stringResource(Res.string.a11y_remove_from_program),
                        onClick = it,
                        size = ExerciseCardActionSize,
                    )
                }
            }
            when {
                !setLabels.isNullOrEmpty() -> FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(ExerciseCardPillGap),
                    verticalArrangement = Arrangement.spacedBy(ExerciseCardPillGap),
                    modifier = Modifier.padding(top = ExerciseCardPlanSpacing, start = contentStartPadding),
                ) {
                    setLabels.forEach { label -> SetPill(label = label) }
                }

                summary != null -> Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium.withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = ExerciseCardSummarySpacing, start = contentStartPadding),
                )
            }
        }
    }
}

@Composable
private fun clickableTitleModifier(onClick: (() -> Unit)?): Modifier {
    if (onClick == null) return Modifier
    val interactionSource = remember { MutableInteractionSource() }
    return Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@Composable
private fun SetPill(label: String) {
    Surface(shape = LyteTheme.extendedShapes.full, color = MaterialTheme.colorScheme.surfaceContainer) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = ExerciseCardPillTextSize).withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = ExerciseCardPillPaddingHorizontal, vertical = ExerciseCardPillPaddingVertical),
        )
    }
}

@Preview
@Composable
private fun LyteExerciseCardPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteExerciseCard(
                title = "Жим лёжа",
                setLabels = listOf("10×60 кг", "10×60 кг", "8×62.5 кг"),
                onClick = {},
                onEdit = {},
                onRemove = {},
            )
            LyteExerciseCard(
                title = "Подтягивания",
                summary = "3×10 · свой вес",
                onClick = {},
                onRemove = {},
            )
        }
    }
}
