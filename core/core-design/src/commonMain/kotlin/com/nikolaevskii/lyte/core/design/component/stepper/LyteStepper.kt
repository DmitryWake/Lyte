package com.nikolaevskii.lyte.core.design.component.stepper

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.nikolaevskii.lyte.core.design.format.WeightFractionDigits
import com.nikolaevskii.lyte.core.design.format.formatWeight
import com.nikolaevskii.lyte.core.design.format.roundToWeightPrecision
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.stepper_edit
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LytePressScaleStrong
import com.nikolaevskii.lyte.core.design.theme.lytePressScale
import kotlin.math.round
import org.jetbrains.compose.resources.stringResource

/**
 * Размеры по словарю ДС: [Large] — герой формы (`LyteTheme.hitTarget.stepper`), [Medium] — контрол
 * в минимальную зону касания (`LyteTheme.hitTarget.min`).
 */
enum class LyteStepperSize { Large, Medium }

private val StepperWidthLarge = 248.dp
private val StepperWidthMedium = 196.dp
private val StepperIconSize = 24.dp
private const val StepperDisabledAlpha = 0.38f

/** Единица измерения — половина кегля числа (`0.5em` в макете), а не отдельный шаг типографики. */
private const val StepperUnitFontScale = 0.5f
private const val StepperMaxIntegerDigits = 5

/** Дробная часть ввода ограничена ровно тем, что формат потом покажет (см. [formatWeight]). */
private const val StepperMaxDecimalDigits = WeightFractionDigits
private val StepperDecimalInputRegex = Regex("\\d{0,$StepperMaxIntegerDigits}([.,]\\d{0,$StepperMaxDecimalDigits})?")
private val StepperIntegerInputRegex = Regex("\\d{0,$StepperMaxIntegerDigits}")

/**
 * Крупный контрол ± для числового ввода (повторы, вес): значение меняется и кнопками ±, и
 * **ручным вводом** (тап по числу → tap-to-edit поле). Табличные цифры, кнопки ± зафиксированы
 * по краям постоянной ширины (мышечная память попадания пальцем при стопке степперов).
 * [allowDecimal] = `false` — целочисленный режим (для повторов): клавиатура без точки, введённое
 * значение округляется до целого; по умолчанию `true` (дробный ввод для веса, напр. «62,5»).
 *
 * Показывает значение тем же [formatWeight], что и подписи вокруг: у степпера нет собственного
 * формата, иначе «Цель 10 повт × 62,5 кг» стояло бы над полем с «62.5». Ввод при этом принимает
 * оба разделителя — клавиатура даёт свой по системной локали.
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
    val buttonSize = if (size == LyteStepperSize.Large) LyteTheme.hitTarget.stepper else LyteTheme.hitTarget.min
    val width = if (size == LyteStepperSize.Large) StepperWidthLarge else StepperWidthMedium
    val widthModifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(width)
    val numericStyle = if (size == LyteStepperSize.Large) LyteTheme.numericTypography.large else LyteTheme.numericTypography.medium

    var editing by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(TextFieldValue(formatWeight(value))) }
    var hasFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    fun commit() {
        val parsed = draft.text.replace(',', '.').toDoubleOrNull()
        if (parsed != null) {
            val normalized = if (allowDecimal) roundToWeightPrecision(parsed) else round(parsed)
            onValueChange(normalized.coerceIn(min, max))
        }
        hasFocus = false
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
            onClick = { onValueChange(roundToWeightPrecision((value - step).coerceIn(min, max))) },
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            if (editing) {
                BasicTextField(
                    value = draft,
                    onValueChange = { candidate ->
                        if (candidate.text == draft.text || isStepperInputAccepted(candidate.text, allowDecimal)) {
                            draft = candidate
                        }
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasFocus = true
                            } else if (hasFocus) {
                                commit()
                            }
                        },
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
                        val text = formatWeight(value)
                        draft = TextFieldValue(text = text, selection = TextRange(0, text.length))
                        hasFocus = false
                        editing = true
                    },
                )
            }
        }
        StepperCircleButton(
            icon = LyteIcons.Plus,
            enabled = value < max,
            size = buttonSize,
            onClick = { onValueChange(roundToWeightPrecision((value + step).coerceIn(min, max))) },
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
        modifier = Modifier
            .semantics { contentDescription = editLabel }
            .clickable(onClick = onClick),
    ) {
        Text(
            text = formatWeight(value),
            style = style,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.alignByBaseline(),
        )
        unit?.let {
            // Единица наследует начертание числа и садится на его базовую линию — в макете это
            // инлайновый span в 0.5em, а не самостоятельный шаг типографики.
            Text(
                text = it,
                style = style.copy(fontSize = style.fontSize * StepperUnitFontScale),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alignByBaseline().padding(start = 4.dp),
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
    val motion = LyteTheme.motion
    val colorSpec = tween<Color>(durationMillis = motion.durationShort, easing = motion.easingStandard)
    // Заливка перекидывается в primary/onPrimary: одноручный тап вслепую должен дать подтверждение.
    val containerColor by animateColorAsState(
        targetValue = if (pressedAndEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = colorSpec,
        label = "stepperButtonContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (pressedAndEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = colorSpec,
        label = "stepperButtonContent",
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .lytePressScale(interactionSource = interactionSource, pressedScale = LytePressScaleStrong, enabled = enabled)
            .size(size)
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

/**
 * Пропускает промежуточный ручной ввод степпера: пустую строку (очистка поля) и число с
 * не более чем [StepperMaxIntegerDigits] цифрами целой части и [StepperMaxDecimalDigits]
 * знаками после запятой (`,`/`.`). В целочисленном режиме дробная часть недоступна.
 */
internal fun isStepperInputAccepted(text: String, allowDecimal: Boolean): Boolean {
    if (text.isEmpty()) {
        return true
    }
    val regex = if (allowDecimal) StepperDecimalInputRegex else StepperIntegerInputRegex
    return regex.matches(text)
}

@Preview
@Composable
private fun LyteStepperPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteStepper(value = 62.5, onValueChange = {}, unit = "кг")
            LyteStepper(value = 10.0, onValueChange = {}, min = 10.0, allowDecimal = false)
            LyteStepper(value = 62.5, onValueChange = {}, unit = "кг", size = LyteStepperSize.Medium)
        }
    }
}
