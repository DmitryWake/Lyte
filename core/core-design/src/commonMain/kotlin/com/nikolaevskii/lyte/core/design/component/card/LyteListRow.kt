package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

private val ListRowPaddingHorizontal = 14.dp
private val ListRowPaddingVertical = 10.dp
private val ListRowGap = 12.dp
private val ListRowMarkSize = 36.dp
private val ListRowLeadingIconSize = 20.dp
private val ListRowChevronSize = 17.dp
private val ListRowSubtitleSpacing = 1.dp
private val ListRowDividerWidth = 1.dp
private const val ListRowTitleMaxLines = 2
private const val ListRowSubtitleMaxLines = 2

/**
 * Строка списка: ведущий элемент ([leading] — маркер упражнения или иконка), заголовок с подписью,
 * трейлинг-аксессуар либо шеврон. Используется в шторках выбора (упражнения, программы) и в списках
 * ссылок/настроек.
 *
 * Шеврон и [trailing] взаимоисключающи: своё действие справа отменяет намёк «строка куда-то ведёт».
 */
@Composable
fun LyteListRow(
    title: String,
    subtitle: String? = null,
    leading: LyteListRowLeading? = null,
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
        when (leading) {
            is LyteListRowLeading.Mark -> LyteExerciseMark(
                accent = leading.accent,
                glyph = leading.glyph,
                size = ListRowMarkSize,
            )

            is LyteListRowLeading.Icon -> Icon(
                imageVector = leading.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ListRowLeadingIconSize),
            )

            null -> Unit
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = rowTitleMediumStyle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = ListRowTitleMaxLines,
                overflow = TextOverflow.Ellipsis,
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
        // Своего фона у строки нет — она живёт в шторке или на экране, поэтому превью подкладывает
        // поверхность само: иначе в тёмной теме светлый текст оказывается на светлом холсте.
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            LyteListRow(
                title = "Жим лёжа",
                subtitle = "Жим штанги от середины груди лёжа на горизонтальной скамье.",
                leading = LyteListRowLeading.Mark(
                    accent = LyteAccent.Indigo,
                    glyph = LyteExerciseGlyph.BenchPress,
                ),
                onClick = {},
            )
            LyteListRow(
                title = "Приседания со штангой",
                leading = LyteListRowLeading.Mark(accent = LyteAccent.Lime, glyph = LyteExerciseGlyph.Squat),
                onClick = {},
            )
            LyteListRow(
                title = "Лицензии и благодарности",
                leading = LyteListRowLeading.Icon(icon = LyteIcons.List),
                onClick = {},
            )
            LyteListRow(title = "Без ведущего элемента и без шеврона", showChevron = false)
        }
    }
}
