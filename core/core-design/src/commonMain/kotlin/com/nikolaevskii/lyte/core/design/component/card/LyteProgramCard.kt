package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_menu
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import org.jetbrains.compose.resources.stringResource

private val ProgramCardPadding = 20.dp
private val ProgramCardPaddingStart = 24.dp
private val ProgramCardPaddingEnd = 20.dp
private val ProgramCardGap = 12.dp
private val ProgramCardSubtitleSpacing = 4.dp
private val CardTitleTracking = (-0.3).sp

/**
 * Строка списка программ: название + сформированная вызывающей стороной подпись.
 * [trailing] — кастомное действие справа (напр. инлайн-удаление); если не задано,
 * но задан [onMenuClick], показывается kebab-меню.
 */
@Composable
fun LyteProgramCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    onMenuClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = LyteTheme.elevation.level1),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ProgramCardGap),
            modifier = Modifier.padding(start = ProgramCardPaddingStart, top = ProgramCardPadding, bottom = ProgramCardPadding, end = ProgramCardPaddingEnd),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = CardTitleTracking),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = ProgramCardSubtitleSpacing),
                )
            }
            when {
                trailing != null -> trailing()
                onMenuClick != null -> {
                    val menuLabel = stringResource(Res.string.a11y_menu)
                    IconButton(onClick = onMenuClick) {
                        Icon(imageVector = LyteIcons.OverflowMenu, contentDescription = menuLabel)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun LyteProgramCardPreview() {
    LyteTheme {
        LyteProgramCard(
            title = "Push Day",
            subtitle = "5 упражнений · 3 дня назад",
            onClick = {},
            onMenuClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
