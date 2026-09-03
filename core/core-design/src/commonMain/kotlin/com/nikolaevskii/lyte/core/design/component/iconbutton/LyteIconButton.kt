package com.nikolaevskii.lyte.core.design.component.iconbutton

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.lytePressScale

private val LyteIconButtonDefaultSize = 40.dp

/**
 * Круглая кнопка-иконка. [active] переключает фон на secondaryContainer (напр. открытая шторка),
 * [enabled] гасит её на время операции, во время которой действие всё равно не примут.
 * Нажатие — M3-овский state layer плюс уменьшение до 0.97, как у остальных контролов системы.
 */
@Composable
fun LyteIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    size: Dp = LyteIconButtonDefaultSize,
    active: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val iconSize = size / 2
    val interactionSource = remember { MutableInteractionSource() }
    val sizedModifier = modifier
        .lytePressScale(interactionSource)
        .size(size)
    if (active) {
        FilledIconButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            interactionSource = interactionSource,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = sizedModifier,
            enabled = enabled,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
            interactionSource = interactionSource,
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    }
}

@Preview
@Composable
private fun LyteIconButtonPreview() {
    LyteTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LyteIconButton(icon = LyteIcons.Close, contentDescription = "Закрыть", onClick = {})
            LyteIconButton(icon = LyteIcons.List, contentDescription = "Упражнения", onClick = {}, active = true)
            LyteIconButton(icon = LyteIcons.Delete, contentDescription = "Удалить", onClick = {}, enabled = false)
        }
    }
}
