package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepper
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepperSize
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_skipped
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_reps
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_weight
import com.nikolaevskii.lyte.core.design.generated.resources.set_last_time
import com.nikolaevskii.lyte.core.design.generated.resources.set_number
import com.nikolaevskii.lyte.core.design.generated.resources.set_target
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

enum class LyteTrackSetState { Current, DoneHit, DoneMiss, DoneSkip, Todo }

private val TrackSetOutlineWidth = 1.5.dp
private val TrackSetCurrentPaddingTop = 16.dp
private val TrackSetCurrentPaddingHorizontal = 18.dp
private val TrackSetCurrentPaddingBottom = 18.dp
private val TrackSetHeaderSpacing = 14.dp
private val TrackSetHeaderIconGap = 8.dp
private val TrackSetIconSize = 20.dp
private val TrackSetStepperGap = 12.dp
private val TrackSetStepperColumnGap = 6.dp
private val TrackSetCompactPaddingHorizontal = 16.dp
private val TrackSetCompactPaddingVertical = 13.dp
private val TrackSetCompactGap = 10.dp
private val TrackSetHeaderTextSize = 15.sp
private val TrackSetLastTextSize = 12.5.sp
private val TrackSetValueTextSize = 15.sp
private val TrackSetLabelTextSize = 14.sp
private const val TrackSetTodoAlpha = 0.7f

/**
 * Один подход в трекере активной сессии. Две формы:
 * [LyteTrackSetState.Current] — приподнятая фокус-карточка со степперами повторов/веса,
 * опциональной ссылкой «в прошлый раз» и слотом [content] под заметку;
 * остальные состояния — компактная строка с результатом (done-hit/miss/skip) либо целью (todo).
 */
@Composable
fun LyteTrackSetRow(
    number: Int,
    state: LyteTrackSetState,
    reps: Int = 0,
    weight: Double = 0.0,
    target: String? = null,
    last: String? = null,
    onRepsChange: (Int) -> Unit = {},
    onWeightChange: (Double) -> Unit = {},
    repsStep: Int = 1,
    weightStep: Double = 2.5,
    content: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (state == LyteTrackSetState.Current) {
        CurrentSetCard(
            number = number,
            reps = reps,
            weight = weight,
            last = last,
            onRepsChange = onRepsChange,
            onWeightChange = onWeightChange,
            repsStep = repsStep,
            weightStep = weightStep,
            content = content,
            modifier = modifier,
        )
    } else {
        CompactSetRow(number = number, state = state, reps = reps, weight = weight, target = target, modifier = modifier)
    }
}

@Composable
private fun CurrentSetCard(
    number: Int,
    reps: Int,
    weight: Double,
    last: String?,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    repsStep: Int,
    weightStep: Double,
    content: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level2,
        border = BorderStroke(TrackSetOutlineWidth, MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier = Modifier.padding(
                start = TrackSetCurrentPaddingHorizontal,
                end = TrackSetCurrentPaddingHorizontal,
                top = TrackSetCurrentPaddingTop,
                bottom = TrackSetCurrentPaddingBottom,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = TrackSetHeaderSpacing),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TrackSetHeaderIconGap),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = LyteIcons.CircleDot,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(TrackSetIconSize),
                    )
                    Text(
                        text = stringResource(Res.string.set_number, number),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = TrackSetHeaderTextSize, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                last?.let {
                    Text(
                        text = stringResource(Res.string.set_last_time, it),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = TrackSetLastTextSize).withTabularNums(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(TrackSetStepperGap)) {
                StepperColumn(caption = stringResource(Res.string.set_caption_reps)) {
                    LyteStepper(
                        value = reps.toDouble(),
                        onValueChange = { onRepsChange(it.roundToInt()) },
                        step = repsStep.toDouble(),
                        size = LyteStepperSize.Small,
                        allowDecimal = false,
                        fillMaxWidth = true,
                    )
                }
                StepperColumn(caption = stringResource(Res.string.set_caption_weight)) {
                    LyteStepper(
                        value = weight,
                        onValueChange = onWeightChange,
                        step = weightStep,
                        size = LyteStepperSize.Small,
                        fillMaxWidth = true,
                    )
                }
            }
            content?.invoke()
        }
    }
}

@Composable
private fun RowScope.StepperColumn(caption: String, stepper: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TrackSetStepperColumnGap),
        modifier = Modifier.weight(1f),
    ) {
        LyteOverline(text = caption)
        stepper()
    }
}

@Composable
private fun CompactSetRow(
    number: Int,
    state: LyteTrackSetState,
    reps: Int,
    weight: Double,
    target: String?,
    modifier: Modifier = Modifier,
) {
    val (icon, iconColor) = compactIcon(state)
    val alpha = if (state == LyteTrackSetState.Todo) TrackSetTodoAlpha else 1f
    Surface(
        modifier = modifier.alpha(alpha),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TrackSetCompactGap),
            modifier = Modifier.padding(horizontal = TrackSetCompactPaddingHorizontal, vertical = TrackSetCompactPaddingVertical),
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(TrackSetIconSize))
            Text(
                text = stringResource(Res.string.set_number, number),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = TrackSetHeaderTextSize),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            CompactSetValue(state = state, reps = reps, weight = weight, target = target)
        }
    }
}

@Composable
private fun CompactSetValue(state: LyteTrackSetState, reps: Int, weight: Double, target: String?) {
    val extended = LyteTheme.extendedColors
    when (state) {
        LyteTrackSetState.DoneSkip -> Text(
            text = stringResource(Res.string.diff_skipped),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = TrackSetLabelTextSize, fontWeight = FontWeight.Medium),
            color = extended.diffSkipped,
        )

        LyteTrackSetState.DoneHit -> Text(
            text = setValueLabel(reps = reps, weight = weight),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = TrackSetValueTextSize, fontWeight = FontWeight.Bold).withTabularNums(),
            color = MaterialTheme.colorScheme.primary,
        )

        LyteTrackSetState.DoneMiss -> Text(
            text = setValueLabel(reps = reps, weight = weight),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = TrackSetValueTextSize, fontWeight = FontWeight.Bold).withTabularNums(),
            color = extended.diffNegative,
        )

        LyteTrackSetState.Todo -> Text(
            text = stringResource(Res.string.set_target, target.orEmpty()),
            style = MaterialTheme.typography.titleSmall.copy(fontSize = TrackSetLabelTextSize).withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LyteTrackSetState.Current -> Unit
    }
}

@Composable
private fun compactIcon(state: LyteTrackSetState): Pair<ImageVector, Color> = when (state) {
    LyteTrackSetState.DoneSkip -> LyteIcons.CircleMinus to LyteTheme.extendedColors.diffSkipped
    LyteTrackSetState.DoneMiss -> LyteIcons.CircleX to LyteTheme.extendedColors.diffNegative
    LyteTrackSetState.DoneHit -> LyteIcons.CircleCheck to MaterialTheme.colorScheme.primary
    LyteTrackSetState.Todo -> LyteIcons.Circle to MaterialTheme.colorScheme.outlineVariant
    LyteTrackSetState.Current -> LyteIcons.Circle to MaterialTheme.colorScheme.outlineVariant
}

@Preview
@Composable
private fun LyteTrackSetRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteTrackSetRow(number = 1, state = LyteTrackSetState.DoneHit, reps = 10, weight = 60.0)
            LyteTrackSetRow(number = 2, state = LyteTrackSetState.Current, reps = 10, weight = 62.5, last = "10×60")
            LyteTrackSetRow(number = 3, state = LyteTrackSetState.Todo, target = "10×62.5")
        }
    }
}
