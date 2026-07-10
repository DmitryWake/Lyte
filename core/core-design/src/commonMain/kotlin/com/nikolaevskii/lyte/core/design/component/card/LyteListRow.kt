package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteIcons

private val ListRowPaddingHorizontal = 16.dp
private val ListRowPaddingVertical = 12.dp
private val ListRowGap = 12.dp
private val ListRowLeadingIconSize = 22.dp
private val ListRowChevronSize = 18.dp
private val ListRowSubtitleSpacing = 2.dp
private val ListRowDividerWidth = 1.dp
private const val ListRowSubtitleMaxLines = 2

/**
 * Универсальная однострочная строка списка: опциональная ведущая иконка, заголовок+подпись,
 * трейлинг-аксессуар либо шеврон. Используется для пикера упражнений, настроек и т.п.
 */
@Composable
fun LyteListRow(
    title: String,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ListRowGap),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .bottomDivider(MaterialTheme.colorScheme.outlineVariant, ListRowDividerWidth)
            .padding(horizontal = ListRowPaddingHorizontal, vertical = ListRowPaddingVertical),
    ) {
        leadingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ListRowLeadingIconSize),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = ListRowSubtitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = ListRowSubtitleSpacing),
                )
            }
        }
        when {
            trailing != null -> trailing()
            showChevron -> Icon(
                imageVector = LyteIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(ListRowChevronSize),
            )
        }
    }
}

private fun Modifier.bottomDivider(color: Color, strokeWidth: Dp): Modifier = drawBehind {
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = strokeWidth.toPx(),
    )
}

@Preview
@Composable
private fun LyteListRowPreview() {
    LyteTheme {
        Column {
            LyteListRow(
                title = "Жим лёжа",
                subtitle = "Грудь",
                leadingIcon = LyteIcons.Dumbbell,
                onClick = {})
            LyteListRow(title = "Приседания", onClick = {})
        }
    }
}
