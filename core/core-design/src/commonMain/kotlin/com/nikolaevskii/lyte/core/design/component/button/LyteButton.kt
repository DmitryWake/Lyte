package com.nikolaevskii.lyte.core.design.component.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.interaction.lytePressScale
import com.nikolaevskii.lyte.core.design.icon.LyteIcons

enum class LyteButtonVariant { Filled, Tonal, Outlined, Text }

enum class LyteButtonAccent { Primary, Secondary, Tertiary, Error }

enum class LyteButtonSize { Large, Medium, Small }

private val ButtonHeightLarge = 64.dp
private val ButtonHeightMedium = 56.dp
private val ButtonHeightSmall = 44.dp
private val ButtonPaddingLarge = 40.dp
private val ButtonPaddingMedium = 28.dp
private val ButtonPaddingSmall = 20.dp
private val ButtonTextSizeLarge = 17.sp
private val ButtonIconSize = 18.dp
private val ButtonIconSpacing = 8.dp
private val ButtonOutlineWidth = 1.5.dp
private const val ButtonDisabledAlpha = 0.38f

/**
 * Кнопка действия в стиле M3 (filled/tonal/outlined/text), пилюлеобразная форма.
 * [accent] определяет цвет фона у filled/tonal и цвет текста у text; outlined всегда нейтрален.
 *
 * Нажатие — уменьшение до 0.97 поверх M3-овского state layer; выключенная кнопка гасится целиком
 * до alpha 0.38 (цветовые роли при этом не подменяются, поэтому M3-дефолты выключенного вида
 * приравнены к обычным — иначе кнопка гасилась бы дважды).
 */
@Composable
fun LyteButton(
    text: String,
    onClick: () -> Unit,
    variant: LyteButtonVariant = LyteButtonVariant.Filled,
    accent: LyteButtonAccent = LyteButtonAccent.Primary,
    size: LyteButtonSize = LyteButtonSize.Medium,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = LyteTheme.extendedShapes.full
    val heightDp = when (size) {
        LyteButtonSize.Large -> ButtonHeightLarge
        LyteButtonSize.Medium -> ButtonHeightMedium
        LyteButtonSize.Small -> ButtonHeightSmall
    }
    val horizontalPadding = when (size) {
        LyteButtonSize.Large -> ButtonPaddingLarge
        LyteButtonSize.Medium -> ButtonPaddingMedium
        LyteButtonSize.Small -> ButtonPaddingSmall
    }
    val textStyle = when (size) {
        LyteButtonSize.Large -> MaterialTheme.typography.titleMedium.copy(fontSize = ButtonTextSizeLarge)
        LyteButtonSize.Medium -> MaterialTheme.typography.titleMedium
        LyteButtonSize.Small -> MaterialTheme.typography.labelLarge
    }
    val contentPadding = PaddingValues(horizontal = horizontalPadding)
    val interactionSource = remember { MutableInteractionSource() }
    val sizedModifier = modifier
        .lytePressScale(interactionSource = interactionSource, enabled = enabled)
        .alpha(if (enabled) 1f else ButtonDisabledAlpha)
        .height(heightDp)
        .let { if (fullWidth) it.fillMaxWidth() else it }

    val label: @Composable RowScope.() -> Unit = {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(ButtonIconSize))
            Spacer(modifier = Modifier.width(ButtonIconSpacing))
        }
        Text(text = text, style = textStyle)
    }

    when (variant) {
        LyteButtonVariant.Filled -> Button(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = filledColors(accent),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = LyteTheme.elevation.level2,
                disabledElevation = LyteTheme.elevation.level2,
            ),
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = label,
        )

        LyteButtonVariant.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = tonalColors(accent),
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = label,
        )

        LyteButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border = BorderStroke(ButtonOutlineWidth, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = label,
        )

        LyteButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(
                contentColor = accentColor(accent),
                disabledContentColor = accentColor(accent),
            ),
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = label,
        )
    }
}

@Composable
private fun accentColor(accent: LyteButtonAccent): Color = when (accent) {
    LyteButtonAccent.Primary -> MaterialTheme.colorScheme.primary
    LyteButtonAccent.Secondary -> MaterialTheme.colorScheme.secondary
    LyteButtonAccent.Tertiary -> MaterialTheme.colorScheme.tertiary
    LyteButtonAccent.Error -> MaterialTheme.colorScheme.error
}

/**
 * Выключенный вид повторяет обычный: гашение делает alpha всей кнопки, а M3-дефолты выключенных
 * ролей подменили бы ещё и цвета — кнопка потускнела бы дважды.
 */
@Composable
private fun filledColors(accent: LyteButtonAccent): ButtonColors {
    val (container, content) = filledRoles(accent)
    return ButtonDefaults.buttonColors(
        containerColor = container,
        contentColor = content,
        disabledContainerColor = container,
        disabledContentColor = content,
    )
}

@Composable
private fun tonalColors(accent: LyteButtonAccent): ButtonColors {
    val (container, content) = tonalRoles(accent)
    return ButtonDefaults.filledTonalButtonColors(
        containerColor = container,
        contentColor = content,
        disabledContainerColor = container,
        disabledContentColor = content,
    )
}

@Composable
private fun filledRoles(accent: LyteButtonAccent): Pair<Color, Color> = when (accent) {
    LyteButtonAccent.Primary -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
    LyteButtonAccent.Secondary -> MaterialTheme.colorScheme.secondary to MaterialTheme.colorScheme.onSecondary
    LyteButtonAccent.Tertiary -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.onTertiary
    LyteButtonAccent.Error -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
}

@Composable
private fun tonalRoles(accent: LyteButtonAccent): Pair<Color, Color> = when (accent) {
    LyteButtonAccent.Primary -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    LyteButtonAccent.Secondary -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    LyteButtonAccent.Tertiary -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    LyteButtonAccent.Error -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
}

@Preview
@Composable
private fun LyteButtonPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteButton(text = "Начать тренировку", onClick = {}, size = LyteButtonSize.Large, fullWidth = true)
            LyteButton(text = "Средняя", onClick = {}, icon = LyteIcons.Dumbbell)
            LyteButton(text = "Тонал", onClick = {}, variant = LyteButtonVariant.Tonal)
            LyteButton(text = "Контур", onClick = {}, variant = LyteButtonVariant.Outlined)
            LyteButton(text = "Текст", onClick = {}, variant = LyteButtonVariant.Text)
            LyteButton(text = "Удалить", onClick = {}, variant = LyteButtonVariant.Text, accent = LyteButtonAccent.Error)
            LyteButton(text = "Маленькая", onClick = {}, size = LyteButtonSize.Small)
            LyteButton(text = "Сохранить", onClick = {}, enabled = false)
            LyteButton(text = "Сохранить", onClick = {}, variant = LyteButtonVariant.Tonal, enabled = false)
        }
    }
}
