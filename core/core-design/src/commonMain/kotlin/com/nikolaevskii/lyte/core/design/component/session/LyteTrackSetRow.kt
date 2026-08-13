package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepper
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepperSize
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_reps
import com.nikolaevskii.lyte.core.design.generated.resources.set_field_weight
import com.nikolaevskii.lyte.core.design.generated.resources.set_number
import com.nikolaevskii.lyte.core.design.generated.resources.set_of_count
import com.nikolaevskii.lyte.core.design.generated.resources.set_outcome_exceeded
import com.nikolaevskii.lyte.core.design.generated.resources.set_outcome_met
import com.nikolaevskii.lyte.core.design.generated.resources.set_outcome_missed
import com.nikolaevskii.lyte.core.design.generated.resources.set_reference_last_time
import com.nikolaevskii.lyte.core.design.generated.resources.set_reference_target
import com.nikolaevskii.lyte.core.design.generated.resources.set_skipped
import com.nikolaevskii.lyte.core.design.generated.resources.set_target
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

/**
 * Высота спокойной строки задана жёстко, а не паддингами: стили значений наследуют разный
 * `lineHeight` (24/20/16sp), и от паддингов строки разных тонов встали бы на разную высоту.
 */
private val TrackSetQuietHeight = 36.dp
private val TrackSetQuietPaddingHorizontal = 16.dp
private val TrackSetQuietGap = 10.dp
private val TrackSetIndexWidth = 12.dp
private val TrackSetIconSize = 17.dp
private val TrackSetFocusPadding = 18.dp
private val TrackSetFocusOutlineWidth = 2.dp
private val TrackSetHeaderGap = 8.dp
private val TrackSetReferenceTopPadding = 6.dp
private val TrackSetReferenceStartPadding = 25.dp
private val TrackSetReferenceGap = 2.dp
private val TrackSetReferenceLeaderGap = 8.dp
private val TrackSetLeaderThickness = 1.dp
private val TrackSetLeaderInset = 3.dp
private val TrackSetStepperTopPadding = 14.dp
private val TrackSetStepperRowGap = 10.dp
private val TrackSetStepperCaptionGap = 12.dp
private val TrackSetStepperCaptionWidth = 40.dp
private val TrackSetFocusContentTopPadding = 14.dp
private val TrackSetIndexTextSize = 12.sp
private val TrackSetNoteTextSize = 12.sp
private val TrackSetValueTextSize = 14.5.sp
private val TrackSetValueTracking = (-0.2).sp
private val TrackSetSkippedTextSize = 13.sp
private val TrackSetTargetTextSize = 13.5.sp
private val TrackSetHeaderTextSize = 14.5.sp
private val TrackSetHeaderTracking = (-0.1).sp
private val TrackSetCounterTextSize = 12.sp
private val TrackSetReferenceLabelTextSize = 12.sp
private val TrackSetReferenceValueTextSize = 12.5.sp
private const val TrackSetLeaderAlpha = 0.6f

/**
 * Один подход на экране трекинга. Две взаимоисключающие формы, см. [LyteTrackSetState]:
 *
 * - [LyteTrackSetState.Quiet] — спокойная строка 36dp: индекс, иконка направления, опциональная
 *   заметка и значение справа. Направление несёт **иконка**, а не крестик: недобор до цели — это
 *   направление, а не провал, и ✗ рядом с подходом, который человек реально сделал, читается
 *   наказанием.
 * - [LyteTrackSetState.Focus] — приподнятая фокус-карточка со степперами, строками-ориентирами и
 *   слотом [focusContent] под заметку.
 *
 * Обводка фокус-карточки задаётся `Surface.border` — она рисуется **внутрь** границ, поэтому её не
 * срежет скроллер, когда карточка стоит у нижнего края списка (в вебе того же добивались
 * `inset box-shadow`).
 */
@Composable
fun LyteTrackSetRow(
    number: Int,
    state: LyteTrackSetState,
    onRepsChange: (Int) -> Unit = {},
    onWeightChange: (Double) -> Unit = {},
    repsStep: Int = 1,
    weightStep: Double = 2.5,
    focusContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is LyteTrackSetState.Quiet -> QuietSetRow(number = number, state = state, modifier = modifier)

        is LyteTrackSetState.Focus -> FocusSetCard(
            number = number,
            state = state,
            onRepsChange = onRepsChange,
            onWeightChange = onWeightChange,
            repsStep = repsStep,
            weightStep = weightStep,
            focusContent = focusContent,
            modifier = modifier,
        )
    }
}

@Composable
private fun QuietSetRow(
    number: Int,
    state: LyteTrackSetState.Quiet,
    modifier: Modifier = Modifier,
) {
    val (foreground, background) = quietToneColors(state.tone)
    Surface(
        // Строка — один логический объект: экранный диктор читает её целиком, а не по четырём кускам.
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.large,
        color = background,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TrackSetQuietGap),
            modifier = Modifier
                .height(TrackSetQuietHeight)
                .padding(horizontal = TrackSetQuietPaddingHorizontal),
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium
                    .copy(fontSize = TrackSetIndexTextSize, fontWeight = FontWeight.SemiBold)
                    .withTabularNums(),
                color = foreground,
                maxLines = 1,
                modifier = Modifier.widthIn(min = TrackSetIndexWidth),
            )
            Icon(
                imageVector = quietToneIcon(state.tone),
                contentDescription = quietToneDescription(state.tone),
                tint = foreground,
                modifier = Modifier.size(TrackSetIconSize),
            )
            Box(modifier = Modifier.weight(1f)) {
                state.note?.let { note ->
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = TrackSetNoteTextSize),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            QuietSetValue(tone = state.tone, value = state.value, color = foreground)
        }
    }
}

@Composable
private fun QuietSetValue(tone: LyteProgressTone, value: LyteSetValue?, color: Color) {
    when (tone) {
        LyteProgressTone.Skipped -> Text(
            text = stringResource(Res.string.set_skipped),
            style = MaterialTheme.typography.bodyMedium
                .copy(fontSize = TrackSetSkippedTextSize, fontWeight = FontWeight.Medium),
            color = color,
            maxLines = 1,
        )

        LyteProgressTone.Todo -> value?.let {
            Text(
                text = stringResource(Res.string.set_target, setValueLabel(value = it)),
                style = MaterialTheme.typography.titleSmall
                    .copy(fontSize = TrackSetTargetTextSize, fontWeight = FontWeight.SemiBold)
                    .withTabularNums(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }

        LyteProgressTone.Positive, LyteProgressTone.Met, LyteProgressTone.Negative -> value?.let {
            Text(
                text = setValueLabel(value = it),
                style = MaterialTheme.typography.titleMedium
                    .copy(
                        fontSize = TrackSetValueTextSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = TrackSetValueTracking,
                    )
                    .withTabularNums(),
                color = color,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FocusSetCard(
    number: Int,
    state: LyteTrackSetState.Focus,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    repsStep: Int,
    weightStep: Double,
    focusContent: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level2,
        border = BorderStroke(TrackSetFocusOutlineWidth, MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(TrackSetFocusPadding)) {
            FocusSetHeader(number = number, setCount = state.setCount)
            FocusSetReferences(references = state.references)
            FocusSetSteppers(
                state = state,
                onRepsChange = onRepsChange,
                onWeightChange = onWeightChange,
                repsStep = repsStep,
                weightStep = weightStep,
            )
            focusContent?.let { content ->
                Box(modifier = Modifier.padding(top = TrackSetFocusContentTopPadding)) { content() }
            }
        }
    }
}

@Composable
private fun FocusSetHeader(number: Int, setCount: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrackSetHeaderGap),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = LyteIcons.CircleDot,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(TrackSetIconSize),
        )
        Text(
            text = stringResource(Res.string.set_number, number),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = TrackSetHeaderTextSize,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = TrackSetHeaderTracking,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(Res.string.set_of_count, setCount),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = TrackSetCounterTextSize).withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FocusSetReferences(references: List<LyteTrackSetReference>) {
    if (references.isEmpty()) {
        return
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(TrackSetReferenceGap),
        modifier = Modifier.padding(top = TrackSetReferenceTopPadding, start = TrackSetReferenceStartPadding),
    ) {
        references.forEach { reference ->
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(TrackSetReferenceLeaderGap),
            ) {
                Text(
                    text = referenceLabel(reference),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = TrackSetReferenceLabelTextSize),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = TrackSetLeaderInset)
                        .height(TrackSetLeaderThickness)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = TrackSetLeaderAlpha)),
                )
                Text(
                    text = reference.value,
                    style = MaterialTheme.typography.labelMedium
                        .copy(fontSize = TrackSetReferenceValueTextSize, fontWeight = FontWeight.SemiBold)
                        .withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun FocusSetSteppers(
    state: LyteTrackSetState.Focus,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    repsStep: Int,
    weightStep: Double,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TrackSetStepperRowGap),
        modifier = Modifier.padding(top = TrackSetStepperTopPadding),
    ) {
        StepperRow(caption = stringResource(Res.string.set_caption_reps)) {
            LyteStepper(
                value = state.reps.toDouble(),
                onValueChange = { value -> onRepsChange(value.roundToInt()) },
                step = repsStep.toDouble(),
                size = LyteStepperSize.Small,
                allowDecimal = false,
                fillMaxWidth = true,
            )
        }
        state.weight?.let { weight ->
            StepperRow(caption = stringResource(Res.string.set_field_weight)) {
                LyteStepper(
                    value = weight,
                    onValueChange = onWeightChange,
                    step = weightStep,
                    unit = stringResource(Res.string.diff_weight),
                    size = LyteStepperSize.Small,
                    fillMaxWidth = true,
                )
            }
        }
    }
}

@Composable
private fun StepperRow(caption: String, stepper: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrackSetStepperCaptionGap),
    ) {
        Box(modifier = Modifier.width(TrackSetStepperCaptionWidth)) {
            LyteOverline(text = caption)
        }
        Box(modifier = Modifier.weight(1f)) { stepper() }
    }
}

@Composable
private fun quietToneColors(tone: LyteProgressTone): Pair<Color, Color> {
    val extended = LyteTheme.extendedColors
    return when (tone) {
        LyteProgressTone.Met -> extended.diffMet to extended.diffMetBg
        LyteProgressTone.Positive -> extended.diffPositive to extended.diffPositiveBg
        LyteProgressTone.Negative -> extended.diffNegative to extended.diffNegativeBg
        LyteProgressTone.Skipped -> extended.diffSkipped to extended.diffSkippedBg
        LyteProgressTone.Todo -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.surfaceContainerLow
    }
}

@Composable
private fun referenceLabel(reference: LyteTrackSetReference): String = when (reference) {
    is LyteTrackSetReference.Target -> stringResource(Res.string.set_reference_target)
    is LyteTrackSetReference.LastTime -> stringResource(Res.string.set_reference_last_time)
}

private fun quietToneIcon(tone: LyteProgressTone): ImageVector = when (tone) {
    LyteProgressTone.Met -> LyteIcons.CircleCheck
    LyteProgressTone.Positive -> LyteIcons.CircleArrowUp
    LyteProgressTone.Negative -> LyteIcons.CircleArrowDown
    LyteProgressTone.Skipped -> LyteIcons.CircleMinus
    LyteProgressTone.Todo -> LyteIcons.Circle
}

/**
 * Направление исхода в спокойной строке передаётся только иконкой и цветом, поэтому у трёх
 * выполненных тонов иконка подписана. У пропуска и будущего подхода подпись не нужна — исход уже
 * назван словами справа («пропущен», «цель …»), и озвучивать его дважды незачем.
 */
@Composable
private fun quietToneDescription(tone: LyteProgressTone): String? = when (tone) {
    LyteProgressTone.Met -> stringResource(Res.string.set_outcome_met)
    LyteProgressTone.Positive -> stringResource(Res.string.set_outcome_exceeded)
    LyteProgressTone.Negative -> stringResource(Res.string.set_outcome_missed)
    LyteProgressTone.Skipped, LyteProgressTone.Todo -> null
}

@Preview
@Composable
private fun LyteTrackSetRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(20.dp),
        ) {
            LyteTrackSetRow(
                number = 1,
                state = LyteTrackSetState.Quiet(
                    tone = LyteProgressTone.Met,
                    value = LyteSetValue(reps = 10, weight = 60.0),
                ),
            )
            LyteTrackSetRow(
                number = 2,
                state = LyteTrackSetState.Quiet(
                    tone = LyteProgressTone.Positive,
                    value = LyteSetValue(reps = 12, weight = 60.0),
                ),
            )
            LyteTrackSetRow(
                number = 3,
                state = LyteTrackSetState.Quiet(
                    tone = LyteProgressTone.Negative,
                    value = LyteSetValue(reps = 8, weight = 62.5),
                    note = "Последний повтор с подстраховкой, поясница подсаживалась",
                ),
            )
            LyteTrackSetRow(number = 4, state = LyteTrackSetState.Quiet(tone = LyteProgressTone.Skipped))
            LyteTrackSetRow(
                number = 5,
                state = LyteTrackSetState.Quiet(
                    tone = LyteProgressTone.Todo,
                    value = LyteSetValue(reps = 10, weight = 62.5),
                ),
            )
            LyteTrackSetRow(
                number = 10,
                state = LyteTrackSetState.Quiet(
                    tone = LyteProgressTone.Met,
                    value = LyteSetValue(reps = 12, weight = 102.5),
                ),
            )
            LyteTrackSetRow(
                number = 11,
                state = LyteTrackSetState.Quiet(tone = LyteProgressTone.Met, value = LyteSetValue(reps = 15)),
            )
        }
    }
}

@Preview
@Composable
private fun LyteTrackSetRowFocusPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(20.dp),
        ) {
            LyteTrackSetRow(
                number = 3,
                state = LyteTrackSetState.Focus(
                    setCount = 5,
                    reps = 10,
                    weight = 62.5,
                    references = listOf(
                        LyteTrackSetReference.Target(value = "10×62.5 кг"),
                        LyteTrackSetReference.LastTime(value = "10×60 кг"),
                    ),
                ),
            )
            LyteTrackSetRow(
                number = 1,
                state = LyteTrackSetState.Focus(
                    setCount = 3,
                    reps = 12,
                    references = listOf(LyteTrackSetReference.Target(value = "12 повт")),
                ),
            )
        }
    }
}
