package com.nikolaevskii.lyte.core.design.component.iconbutton

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteIcons

private val LyteIconButtonDefaultSize = 40.dp

/** Круглая кнопка-иконка. [active] переключает фон на secondaryContainer (напр. открытое меню). */
@Composable
fun LyteIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    size: Dp = LyteIconButtonDefaultSize,
    active: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val iconSize = size / 2
    if (active) {
        FilledIconButton(
            onClick = onClick,
            modifier = modifier.size(size),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(size),
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
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
            LyteIconButton(icon = LyteIcons.OverflowMenu, contentDescription = "Меню", onClick = {}, active = true)
        }
    }
}
