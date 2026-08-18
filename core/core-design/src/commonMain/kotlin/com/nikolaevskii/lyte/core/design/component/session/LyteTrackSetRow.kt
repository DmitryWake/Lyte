package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepper
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepperSize
import com.nikolaevskii.lyte.core.design.format.lyteSetValueLabel
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_reps
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_weight_name
import com.nikolaevskii.lyte.core.design.generated.resources.set_number
import com.nikolaevskii.lyte.core.design.generated.resources.set_of_total
import com.nikolaevskii.lyte.core.design.generated.resources.set_reference_last
import com.nikolaevskii.lyte.core.design.generated.resources.set_reference_target
import com.nikolaevskii.lyte.core.design.generated.resources.set_skipped
import com.nikolaevskii.lyte.core.design.generated.resources.set_target
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private val TrackSetRestingMinHeight = 36.dp
private val TrackSetRestingPaddingHorizontal = 16.dp
private val TrackSetRestingPaddingVertical = 9.dp
private val TrackSetRestingGap = 10.dp
private val TrackSetIndexWidth = 12.dp
private val TrackSetIconSize = 17.dp
private val TrackSetIndexTextSize = 12.sp
private val TrackSetValueTextSize = 14.5.sp
private val TrackSetValueTracking = (-0.2).sp
private val TrackSetTargetTextSize = 13.5.sp
private val TrackSetSkippedTextSize = 13.sp
private val TrackSetNoteTextSize = 12.sp

/** Строка спокойного подхода держит 36dp: интерлиньяж задан явно, иначе её распирает стилем текста. */
private val TrackSetRestingLineHeight = 18.sp

private val TrackSetCardPadding = 18.dp
private val TrackSetCardRingWidth = 2.dp
private val TrackSetCardHeaderGap = 8.dp
private val TrackSetCardTitleTextSize = 14.5.sp
private val TrackSetCardTitleTracking = (-0.1).sp
private val TrackSetCardTotalTextSize = 12.sp
private val TrackSetReferenceIndent = 25.dp
private val TrackSetReferenceTopGap = 6.dp
private val TrackSetReferenceRowGap = 2.dp
private val TrackSetReferenceGap = 8.dp
private val TrackSetReferenceLabelTextSize = 12.sp
private val TrackSetReferenceValueTextSize = 12.5.sp
private val TrackSetReferenceLeaderHeight = 1.dp
private const val TrackSetReferenceLeaderAlpha = 0.6f
private val TrackSetSteppersTopGap = 14.dp
private val TrackSetStepperRowGap = 10.dp
private val TrackSetStepperGap = 12.dp
private val TrackSetStepperCaptionWidth = 40.dp
private val TrackSetContentTopGap = 16.dp

/**
 * Пол повторов — тот же, что у планирования подхода (`SetEditRowMinReps`): подход на ноль повторов
 * это пропуск, а для пропуска на экране есть отдельная кнопка.
 */
private const val TrackSetRepsMin = 1.0

private val TrackSetPreviewGap = 6.dp
private val TrackSetPreviewPadding = 16.dp

/**
 * Один подход на экране тренировки. Форму выбирает [state]:
 * [LyteTrackSetState.Resting] — спокойная строка 36dp с исходом справа,
 * [LyteTrackSetState.Current] — фокус-карточка со степперами повторов и веса.
 *
 * Спокойные строки намеренно узкие и без слова «Подход»: внутри списка позиция сама говорит, какой
 * это подход, и именно отказ от слова позволяет семи отработанным подходам поместиться на экране
 * рядом с фокус-карточкой.
 *
 * [content] — слот под заметку или чип внизу фокус-карточки; в спокойной строке он не рисуется
 * (её заметка — часть [LyteTrackSetState.Resting]).
 */
@Composable
fun LyteTrackSetRow(
    number: Int,
    state: LyteTrackSetState,
    onRepsChange: (Int) -> Unit = {},
    onWeightChange: (Double) -> Unit = {},
    content: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is LyteTrackSetState.Current -> CurrentSetCard(
            number = number,
            state = state,
            onRepsChange = onRepsChange,
            onWeightChange = onWeightChange,
            content = content,
            modifier = modifier,
        )

        is LyteTrackSetState.Resting -> RestingSetRow(number = number, state = state, modifier = modifier)
    }
}

@Composable
private fun RestingSetRow(
    number: Int,
    state: LyteTrackSetState.Resting,
    modifier: Modifier = Modifier,
) {
    val colors = restingToneColors(state.tone)
    Surface(
        modifier = modifier.defaultMinSize(minHeight = TrackSetRestingMinHeight),
        shape = MaterialTheme.shapes.large,
        color = colors.background,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TrackSetRestingGap),
            modifier = Modifier.padding(
                horizontal = TrackSetRestingPaddingHorizontal,
                vertical = TrackSetRestingPaddingVertical,
            ),
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium
                    .copy(
                        fontSize = TrackSetIndexTextSize,
                        lineHeight = TrackSetRestingLineHeight,
                        letterSpacing = 0.sp,
                    )
                    .withTabularNums(),
                color = colors.foreground,
                maxLines = 1,
                // Минимум, а не фиксированная ширина: двузначный номер подхода не должен обрезаться.
                modifier = Modifier.widthIn(min = TrackSetIndexWidth),
            )
            Icon(
                imageVector = colors.icon,
                contentDescription = null,
                tint = colors.foreground,
                modifier = Modifier.size(TrackSetIconSize),
            )
            if (state.note == null) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Text(
                    text = state.note,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = TrackSetNoteTextSize,
                        lineHeight = TrackSetRestingLineHeight,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            RestingSetValue(state = state, foreground = colors.foreground)
        }
    }
}

@Composable
private fun RestingSetValue(state: LyteTrackSetState.Resting, foreground: Color) {
    val label = state.value?.let { value -> lyteSetValueLabel(value) }.orEmpty()
    when (state.tone) {
        LyteProgressTone.Met, LyteProgressTone.Positive, LyteProgressTone.Negative -> Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
                .copy(
                    fontSize = TrackSetValueTextSize,
                    lineHeight = TrackSetRestingLineHeight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = TrackSetValueTracking,
                )
                .withTabularNums(),
            color = foreground,
            maxLines = 1,
        )

        LyteProgressTone.Skipped -> Text(
            text = stringResource(Res.string.set_skipped),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = TrackSetSkippedTextSize,
                lineHeight = TrackSetRestingLineHeight,
                fontWeight = FontWeight.Medium,
            ),
            color = foreground,
            maxLines = 1,
        )

        LyteProgressTone.Todo -> Text(
            text = stringResource(Res.string.set_target, label),
            style = MaterialTheme.typography.labelLarge
                .copy(fontSize = TrackSetTargetTextSize, lineHeight = TrackSetRestingLineHeight)
                .withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun CurrentSetCard(
    number: Int,
    state: LyteTrackSetState.Current,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    content: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level2,
        // Обводка Compose рисуется внутрь границ — то же, что `inset` box-shadow в макете: её не
        // срежет скроллер, когда карточка прижата к нижнему краю списка.
        border = BorderStroke(TrackSetCardRingWidth, MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(TrackSetCardPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(TrackSetCardHeaderGap),
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
                        fontSize = TrackSetCardTitleTextSize,
                        letterSpacing = TrackSetCardTitleTracking,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.set_of_total, state.total),
                    style = MaterialTheme.typography.bodySmall
                        .copy(fontSize = TrackSetCardTotalTextSize)
                        .withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CurrentSetReferences(target = state.target, last = state.last)
            Column(
                verticalArrangement = Arrangement.spacedBy(TrackSetStepperRowGap),
                modifier = Modifier.padding(top = TrackSetSteppersTopGap),
            ) {
                StepperRow(caption = stringResource(Res.string.set_caption_reps)) {
                    LyteStepper(
                        value = state.reps.toDouble(),
                        onValueChange = { onRepsChange(it.roundToInt()) },
                        step = state.repsStep.toDouble(),
                        min = TrackSetRepsMin,
                        size = LyteStepperSize.Medium,
                        allowDecimal = false,
                        fillMaxWidth = true,
                    )
                }
                // Степпер веса есть всегда, в том числе у цели «свой вес»: иначе к подтягиваниям
                // нечем добавить пояс. Ноль — это «пока без веса», а не «веса не бывает».
                StepperRow(caption = stringResource(Res.string.set_caption_weight_name)) {
                    LyteStepper(
                        value = state.weight,
                        onValueChange = onWeightChange,
                        step = state.weightStep,
                        unit = stringResource(Res.string.diff_weight),
                        size = LyteStepperSize.Medium,
                        fillMaxWidth = true,
                    )
                }
            }
            content?.let {
                Box(modifier = Modifier.padding(top = TrackSetContentTopGap)) { it() }
            }
        }
    }
}

/**
 * Ориентиры текущего подхода — двумя подписанными строками, а не одним прогоном «цель … · в прошлый
 * раз …»: склейка переносилась по словам и переставала читаться.
 */
@Composable
private fun CurrentSetReferences(target: LyteSetValue?, last: LyteSetValue?) {
    if (target == null && last == null) {
        return
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(TrackSetReferenceRowGap),
        modifier = Modifier.padding(top = TrackSetReferenceTopGap, start = TrackSetReferenceIndent),
    ) {
        target?.let { value ->
            ReferenceRow(label = stringResource(Res.string.set_reference_target), value = lyteSetValueLabel(value))
        }
        last?.let { value ->
            ReferenceRow(label = stringResource(Res.string.set_reference_last), value = lyteSetValueLabel(value))
        }
    }
}

@Composable
private fun ReferenceRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrackSetReferenceGap),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = TrackSetReferenceLabelTextSize),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(
            modifier = Modifier
                .weight(1f)
                .height(TrackSetReferenceLeaderHeight)
                .background(
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = TrackSetReferenceLeaderAlpha),
                ),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium
                .copy(fontSize = TrackSetReferenceValueTextSize, letterSpacing = 0.sp)
                .withTabularNums(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/** Подпись слева фиксированной ширины — так кнопки ± обоих степперов стоят на одной вертикали. */
@Composable
private fun StepperRow(caption: String, stepper: @Composable () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TrackSetStepperGap),
        modifier = Modifier.fillMaxWidth(),
    ) {
        LyteOverline(text = caption, modifier = Modifier.width(TrackSetStepperCaptionWidth))
        Box(modifier = Modifier.weight(1f)) { stepper() }
    }
}

@Composable
private fun restingToneColors(tone: LyteProgressTone): TrackSetToneColors {
    val extended = LyteTheme.extendedColors
    return when (tone) {
        LyteProgressTone.Met -> TrackSetToneColors(
            icon = LyteIcons.CircleCheck,
            foreground = extended.diffMet,
            background = extended.diffMetBg,
        )

        LyteProgressTone.Positive -> TrackSetToneColors(
            icon = LyteIcons.CircleArrowUp,
            foreground = extended.diffPositive,
            background = extended.diffPositiveBg,
        )

        LyteProgressTone.Negative -> TrackSetToneColors(
            icon = LyteIcons.CircleArrowDown,
            foreground = extended.diffNegative,
            background = extended.diffNegativeBg,
        )

        LyteProgressTone.Skipped -> TrackSetToneColors(
            icon = LyteIcons.CircleMinus,
            foreground = extended.diffSkipped,
            background = extended.diffSkippedBg,
        )

        LyteProgressTone.Todo -> TrackSetToneColors(
            icon = LyteIcons.Circle,
            foreground = MaterialTheme.colorScheme.outline,
            background = MaterialTheme.colorScheme.surfaceContainerLow,
        )
    }
}

private data class TrackSetToneColors(
    val icon: ImageVector,
    val foreground: Color,
    val background: Color,
)

@Preview
@Composable
private fun LyteTrackSetRowRestingPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(TrackSetPreviewGap),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(TrackSetPreviewPadding),
        ) {
            LyteTrackSetRow(
                number = 1,
                state = LyteTrackSetState.Resting(
                    tone = LyteProgressTone.Met,
                    value = LyteSetValue(reps = 10, weight = 60.0),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LyteTrackSetRow(
                number = 2,
                state = LyteTrackSetState.Resting(
                    tone = LyteProgressTone.Positive,
                    value = LyteSetValue(reps = 12, weight = 60.0),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LyteTrackSetRow(
                number = 3,
                state = LyteTrackSetState.Resting(
                    tone = LyteProgressTone.Negative,
                    value = LyteSetValue(reps = 8, weight = 62.5),
                    note = "Локоть увёл вправо — снизил вес на последнем повторе",
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LyteTrackSetRow(
                number = 4,
                state = LyteTrackSetState.Resting(tone = LyteProgressTone.Skipped),
                modifier = Modifier.fillMaxWidth(),
            )
            LyteTrackSetRow(
                number = 5,
                state = LyteTrackSetState.Resting(
                    tone = LyteProgressTone.Todo,
                    value = LyteSetValue(reps = 10, weight = 62.5),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            LyteTrackSetRow(
                number = 6,
                state = LyteTrackSetState.Resting(
                    tone = LyteProgressTone.Met,
                    value = LyteSetValue(reps = 12),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun LyteTrackSetRowCurrentPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(TrackSetPreviewGap),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(TrackSetPreviewPadding),
        ) {
            LyteTrackSetRow(
                number = 3,
                state = LyteTrackSetState.Current(
                    total = 5,
                    reps = 10,
                    weight = 62.5,
                    target = LyteSetValue(reps = 10, weight = 62.5),
                    last = LyteSetValue(reps = 10, weight = 60.0),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            // Цель «свой вес»: степпер веса всё равно на месте и стоит на нуле — им и добавляют пояс.
            LyteTrackSetRow(
                number = 1,
                state = LyteTrackSetState.Current(total = 3, reps = 12, weight = 0.0, target = LyteSetValue(reps = 12)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
