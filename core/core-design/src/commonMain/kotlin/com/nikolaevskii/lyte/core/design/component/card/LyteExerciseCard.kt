package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.font.FontWeight
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
private val ExerciseCardBadgeSize = 24.dp
private val ExerciseCardBadgeGap = 6.dp
private val ExerciseCardBadgeTextSize = 12.5.sp
private val ExerciseCardPlanSpacing = 8.dp
private val ExerciseCardSummarySpacing = 4.dp
private val ExerciseCardPillGap = 5.dp
private val ExerciseCardPillPaddingHorizontal = 10.dp
private val ExerciseCardPillPaddingVertical = 4.dp
private val ExerciseCardActionSize = 38.dp
private val ExerciseCardPillTextSize = 12.5.sp
private const val ExerciseCardTitleMaxLines = 2

/**
 * Строка упражнения: название (до 2 строк) и — снизу — раскладка подходов. [setLabels] (готовые
 * подписи подходов от вызывающей стороны) рендерятся переносящимся рядом пилюль и имеют приоритет над
 * компактным [summary]; единицы/формат числа выбирает вызывающая сторона.
 *
 * Ведущий элемент строки и доступные действия задаёт [variant]:
 * [LyteExerciseCardVariant.Editor] — drag-хэндл + кнопки «редактировать»/«убрать» (редактор
 * программы); [LyteExerciseCardVariant.Preview] — порядковый номер упражнения без действий (read-only
 * превью, спека 4.2). План при [LyteExerciseCardVariant.Editor] выравнивается под название (за
 * хэндлом), при [LyteExerciseCardVariant.Preview] — во всю ширину.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LyteExerciseCard(
    title: String,
    variant: LyteExerciseCardVariant,
    summary: String? = null,
    setLabels: List<String>? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val contentStartPadding = when (variant) {
        is LyteExerciseCardVariant.Editor -> ExerciseCardContentStart
        is LyteExerciseCardVariant.Preview -> 0.dp
    }

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
                when (variant) {
                    is LyteExerciseCardVariant.Preview -> IndexBadge(index = variant.index)
                    is LyteExerciseCardVariant.Editor -> Icon(
                        imageVector = LyteIcons.GripVertical,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = variant.dragHandleModifier
                            .padding(end = ExerciseCardHandleGap)
                            .size(ExerciseCardHandleSize),
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
                if (variant is LyteExerciseCardVariant.Editor) {
                    variant.onEdit?.let { onEdit ->
                        LyteIconButton(
                            icon = LyteIcons.Edit,
                            contentDescription = stringResource(Res.string.a11y_edit_sets),
                            onClick = onEdit,
                            size = ExerciseCardActionSize,
                        )
                    }
                    variant.onRemove?.let { onRemove ->
                        LyteIconButton(
                            icon = LyteIcons.Delete,
                            contentDescription = stringResource(Res.string.a11y_remove_from_program),
                            onClick = onRemove,
                            size = ExerciseCardActionSize,
                        )
                    }
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

@Composable
private fun IndexBadge(index: Int) {
    Surface(
        shape = LyteTheme.extendedShapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.padding(end = ExerciseCardBadgeGap),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(ExerciseCardBadgeSize),
        ) {
            Text(
                text = index.toString(),
                style = MaterialTheme.typography.labelLarge
                    .copy(fontSize = ExerciseCardBadgeTextSize, fontWeight = FontWeight.Bold)
                    .withTabularNums(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
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
                variant = LyteExerciseCardVariant.Editor(onEdit = {}, onRemove = {}),
                onClick = {},
            )
            LyteExerciseCard(
                title = "Подтягивания",
                summary = "3×10 · свой вес",
                variant = LyteExerciseCardVariant.Editor(onRemove = {}),
                onClick = {},
            )
            LyteExerciseCard(
                title = "Жим гантелей на наклонной",
                setLabels = listOf("10×24 кг", "10×26 кг", "8×26 кг"),
                variant = LyteExerciseCardVariant.Preview(index = 2),
            )
        }
    }
}
