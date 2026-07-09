package com.nikolaevskii.lyte.core.design.component.stepper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_remove_set
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_reps
import com.nikolaevskii.lyte.core.design.generated.resources.set_caption_weight
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource

private val SetEditRowPaddingHorizontal = 14.dp
private val SetEditRowPaddingTop = 12.dp
private val SetEditRowPaddingBottom = 14.dp
private val SetEditRowContentGap = 8.dp
private val SetEditRowDeleteButtonSize = 34.dp
private val SetEditRowStepperGap = 12.dp
private val SetEditRowColumnGap = 5.dp
private const val SetEditRowRepsStep = 1.0
private const val SetEditRowWeightStep = 2.5
private const val SetEditRowMinReps = 1.0

/**
 * Строка редактирования одного планового подхода программы: заголовок (номер/подпись подхода —
 * определяет вызывающая сторона), кнопка удаления, степперы повторов/веса. В отличие от
 * [com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetRow] не завязана на состояние
 * активной сессии — здесь все подходы редактируемы одновременно (планирование программы, а не
 * трекинг выполнения).
 */
@Composable
fun LyteSetEditRow(
    title: String,
    reps: Int,
    weight: Double,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = LyteTheme.extendedShapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(
                start = SetEditRowPaddingHorizontal,
                end = SetEditRowPaddingHorizontal,
                top = SetEditRowPaddingTop,
                bottom = SetEditRowPaddingBottom,
            ),
            verticalArrangement = Arrangement.spacedBy(SetEditRowContentGap),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                LyteIconButton(
                    icon = LyteIcons.Delete,
                    contentDescription = stringResource(Res.string.a11y_remove_set),
                    onClick = onRemove,
                    size = SetEditRowDeleteButtonSize,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(SetEditRowStepperGap)) {
                SetEditStepperColumn(caption = stringResource(Res.string.set_caption_reps)) {
                    LyteStepper(
                        value = reps.toDouble(),
                        onValueChange = { onRepsChange(it.roundToInt()) },
                        step = SetEditRowRepsStep,
                        min = SetEditRowMinReps,
                        size = LyteStepperSize.Small,
                        allowDecimal = false,
                        fillMaxWidth = true,
                    )
                }
                SetEditStepperColumn(caption = stringResource(Res.string.set_caption_weight)) {
                    LyteStepper(
                        value = weight,
                        onValueChange = onWeightChange,
                        step = SetEditRowWeightStep,
                        size = LyteStepperSize.Small,
                        fillMaxWidth = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.SetEditStepperColumn(caption: String, stepper: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SetEditRowColumnGap),
        modifier = Modifier.weight(1f),
    ) {
        LyteOverline(text = caption)
        stepper()
    }
}

@Preview
@Composable
private fun LyteSetEditRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteSetEditRow(title = "Подход 1", reps = 8, weight = 80.0, onRepsChange = {}, onWeightChange = {}, onRemove = {})
            LyteSetEditRow(title = "Подход 2", reps = 8, weight = 77.5, onRepsChange = {}, onWeightChange = {}, onRemove = {})
        }
    }
}
