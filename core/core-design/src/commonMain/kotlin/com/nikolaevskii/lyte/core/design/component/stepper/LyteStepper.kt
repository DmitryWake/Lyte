package com.nikolaevskii.lyte.core.design.component.stepper

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.stepper_edit
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import kotlin.math.round
import org.jetbrains.compose.resources.stringResource

enum class LyteStepperSize { Large, Small }

private val StepperButtonSizeLarge = 56.dp
private val StepperButtonSizeSmall = 48.dp
private val StepperWidthLarge = 248.dp
private val StepperWidthSmall = 196.dp
private val StepperIconSize = 24.dp
private const val StepperPressScale = 0.94f
private const val StepperDisabledAlpha = 0.38f

/**
 * Крупный контрол ± для числового ввода (повторы, вес): значение меняется и кнопками ±, и
 * **ручным вводом** (тап по числу → tap-to-edit поле). Табличные цифры, кнопки ± зафиксированы
 * по краям постоянной ширины (мышечная память попадания пальцем при стопке степперов).
 * [allowDecimal] = `false` — целочисленный режим (для повторов): клавиатура без точки, введённое
 * значение округляется до целого; по умолчанию `true` (дробный ввод для веса, напр. «62.5»).
 */
@Composable
fun LyteStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double = 1.0,
    min: Double = 0.0,
    max: Double = Double.POSITIVE_INFINITY,
    unit: String? = null,
    size: LyteStepperSize = LyteStepperSize.Large,
    allowDecimal: Boolean = true,
    fillMaxWidth: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val buttonSize = if (size == LyteStepperSize.Large) StepperButtonSizeLarge else StepperButtonSizeSmall
    val width = if (size == LyteStepperSize.Large) StepperWidthLarge else StepperWidthSmall
    val widthModifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(width)
    val numericStyle = if (size == LyteStepperSize.Large) LyteTheme.numericTypography.large else LyteTheme.numericTypography.medium

    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(TextFieldValue(formatStepperValue(value))) }
    val focusRequester = remember { FocusRequester() }

    fun commit() {
        val parsed = draft.text.replace(',', '.').toDoubleOrNull()
        if (parsed != null) {
            val normalized = if (allowDecimal) roundTo2(parsed) else round(parsed)
            onValueChange(normalized.coerceIn(min, max))
        }
        editing = false
    }

    Row(
        modifier = modifier.then(widthModifier),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperCircleButton(
            icon = LyteIcons.Minus,
            enabled = value > min,
            size = buttonSize,
            onClick = { onValueChange(roundTo2((value - step).coerceIn(min, max))) },
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState -> if (!focusState.isFocused) commit() },
                    textStyle = numericStyle.centeredOn(MaterialTheme.colorScheme.onSurface),
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { commit() }),
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                StepperValueDisplay(
                    value = value,
                    unit = unit,
                    style = numericStyle,
                    onClick = {
                        val text = formatStepperValue(value)
                        draft = TextFieldValue(text = text, selection = TextRange(0, text.length))
                        editing = true
                    },
                )
            }
        }
        StepperCircleButton(
            icon = LyteIcons.Plus,
            enabled = value < max,
            size = buttonSize,
            onClick = { onValueChange(roundTo2((value + step).coerceIn(min, max))) },
        )
    }
}

@Composable
private fun StepperValueDisplay(
    value: Double,
    unit: String?,
    style: TextStyle,
    onClick: () -> Unit,
) {
    val editLabel = stringResource(Res.string.stepper_edit)
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .semantics { contentDescription = editLabel }
            .clickable(onClick = onClick),
    ) {
        Text(text = formatStepperValue(value), style = style, color = MaterialTheme.colorScheme.onSurface)
        unit?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
        }
    }
}

@Composable
private fun StepperCircleButton(
    icon: ImageVector,
    enabled: Boolean,
    size: Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedAndEnabled = pressed && enabled
    val scale by animateFloatAsState(if (pressedAndEnabled) StepperPressScale else 1f, label = "stepperButtonScale")
    val containerColor = if (pressedAndEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (pressedAndEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(if (enabled) 1f else StepperDisabledAlpha),
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(StepperIconSize))
        }
    }
}

private fun TextStyle.centeredOn(color: Color): TextStyle = copy(textAlign = TextAlign.Center, color = color)

private fun roundTo2(value: Double): Double = round(value * 100) / 100

private fun formatStepperValue(value: Double): String {
    val rounded = roundTo2(value)
    val long = rounded.toLong()
    return if (rounded == long.toDouble()) long.toString() else rounded.toString()
}

@Preview
@Composable
private fun LyteStepperPreview() {
    LyteTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LyteStepper(value = 60.0, onValueChange = {}, unit = "кг")
        }
    }
}
