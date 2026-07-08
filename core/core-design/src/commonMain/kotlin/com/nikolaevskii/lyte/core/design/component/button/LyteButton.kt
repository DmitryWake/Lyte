package com.nikolaevskii.lyte.core.design.component.button

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
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

/**
 * Кнопка действия в стиле M3 (filled/tonal/outlined/text), пилюлеобразная форма.
 * [accent] определяет цвет фона у filled/tonal и цвет текста у text; outlined всегда нейтрален.
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
    val sizedModifier = modifier
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
            elevation = ButtonDefaults.buttonElevation(defaultElevation = LyteTheme.elevation.level2),
            contentPadding = contentPadding,
            content = label,
        )

        LyteButtonVariant.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = tonalColors(accent),
            contentPadding = contentPadding,
            content = label,
        )

        LyteButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
            border = BorderStroke(ButtonOutlineWidth, MaterialTheme.colorScheme.outlineVariant),
            contentPadding = contentPadding,
            content = label,
        )

        LyteButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(contentColor = accentColor(accent)),
            contentPadding = contentPadding,
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

@Composable
private fun filledColors(accent: LyteButtonAccent): ButtonColors = when (accent) {
    LyteButtonAccent.Primary -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    )

    LyteButtonAccent.Secondary -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary,
    )

    LyteButtonAccent.Tertiary -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.tertiary,
        contentColor = MaterialTheme.colorScheme.onTertiary,
    )

    LyteButtonAccent.Error -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
    )
}

@Composable
private fun tonalColors(accent: LyteButtonAccent): ButtonColors = when (accent) {
    LyteButtonAccent.Primary -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    LyteButtonAccent.Secondary -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    LyteButtonAccent.Tertiary -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )

    LyteButtonAccent.Error -> ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
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
        }
    }
}
