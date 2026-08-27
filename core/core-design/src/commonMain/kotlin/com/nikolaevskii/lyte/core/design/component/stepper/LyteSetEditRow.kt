package com.nikolaevskii.lyte.core.design.component.stepper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_remove_set
import com.nikolaevskii.lyte.core.design.generated.resources.diff_weight
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_reps
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_weight_name
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private val SetEditRowPaddingHorizontal = 16.dp
private val SetEditRowPaddingTop = 14.dp
private val SetEditRowPaddingBottom = 16.dp
private val SetEditRowHeaderGap = 8.dp
private val SetEditRowDeleteButtonSize = 36.dp
private val SetEditRowSteppersTopGap = 12.dp
private val SetEditRowStepperRowGap = 10.dp

/** Кегль заголовка карточки: 14.5/600 из макета — в шкалу M3 не попадает, поэтому copy() поверх. */
private val SetEditRowTitleSize = 14.5.sp
private val SetEditRowTitleTracking = (-0.1).sp

private const val SetEditRowRepsStep = 1.0
private const val SetEditRowWeightStep = 2.5
private const val SetEditRowMinReps = 1.0

private val SetEditRowSpecimenGap = 10.dp
private val SetEditRowSpecimenPadding = 16.dp

/**
 * Карточка одного планового подхода программы (3.4): заголовок (номер/подпись подхода — определяет
 * вызывающая сторона), кнопка удаления, степперы повторов и веса. В отличие от
 * [com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetRow] не завязана на состояние
 * активной сессии — здесь все подходы редактируемы одновременно (планирование программы, а не
 * трекинг выполнения).
 *
 * Степперы идут в столбик с подписью слева ([LyteStepperRow]), а не двумя колонками рядом:
 * планирование и трекинг ставят повторы с весом одинаково, а рядом две пары кнопок ± умещаются
 * только за счёт ширины самих степперов.
 *
 * [onRemove] `null` — кнопки удаления нет. Так карточка выглядит у единственного подхода:
 * упражнение в программе без единого подхода бессмысленно, а мёртвый тап хуже отсутствия кнопки.
 */
@Composable
fun LyteSetEditRow(
    title: String,
    reps: Int,
    weight: Double,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    onRemove: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = LyteTheme.extendedShapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level1,
    ) {
        Column(
            modifier = Modifier.padding(
                start = SetEditRowPaddingHorizontal,
                end = SetEditRowPaddingHorizontal,
                top = SetEditRowPaddingTop,
                bottom = SetEditRowPaddingBottom,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SetEditRowHeaderGap),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = SetEditRowTitleSize,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = SetEditRowTitleTracking,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                onRemove?.let { remove ->
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = stringResource(Res.string.a11y_remove_set),
                        onClick = remove,
                        size = SetEditRowDeleteButtonSize,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(SetEditRowStepperRowGap),
                modifier = Modifier.padding(top = SetEditRowSteppersTopGap),
            ) {
                LyteStepperRow(caption = stringResource(Res.string.set_caption_reps)) {
                    LyteStepper(
                        value = reps.toDouble(),
                        onValueChange = { onRepsChange(it.roundToInt()) },
                        step = SetEditRowRepsStep,
                        min = SetEditRowMinReps,
                        size = LyteStepperSize.Medium,
                        allowDecimal = false,
                        fillMaxWidth = true,
                    )
                }
                LyteStepperRow(caption = stringResource(Res.string.set_caption_weight_name)) {
                    LyteStepper(
                        value = weight,
                        onValueChange = onWeightChange,
                        step = SetEditRowWeightStep,
                        unit = stringResource(Res.string.diff_weight),
                        size = LyteStepperSize.Medium,
                        fillMaxWidth = true,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LyteSetEditRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(SetEditRowSpecimenGap),
            modifier = Modifier.padding(SetEditRowSpecimenPadding),
        ) {
            LyteSetEditRow(title = "Подход 1", reps = 8, weight = 80.0, onRepsChange = {}, onWeightChange = {}, onRemove = {})
            LyteSetEditRow(title = "Подход 2", reps = 8, weight = 77.5, onRepsChange = {}, onWeightChange = {}, onRemove = {})
        }
    }
}

/** Единственный подход программы: удалить его нельзя, поэтому кнопки в шапке нет. */
@Preview
@Composable
private fun LyteSetEditRowSinglePreview() {
    LyteTheme {
        Column(modifier = Modifier.padding(SetEditRowSpecimenPadding)) {
            LyteSetEditRow(
                title = "Подход 1",
                reps = 12,
                weight = 0.0,
                onRepsChange = {},
                onWeightChange = {},
                onRemove = null,
            )
        }
    }
}
