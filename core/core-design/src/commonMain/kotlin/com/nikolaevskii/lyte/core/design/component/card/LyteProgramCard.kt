package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

private val ProgramCardPaddingVertical = 14.dp
private val ProgramCardPaddingHorizontal = 16.dp
private val ProgramCardGap = 14.dp
private val ProgramCardMarkSize = 52.dp
private val ProgramCardSubtitleSpacing = 1.dp

private val ProgramCardSpecimenGap = 12.dp
private val ProgramCardSpecimenPadding = 16.dp
private val ProgramCardSpecimenActionSize = 38.dp

/**
 * Карточка программы в списке (спека 3.1) и в любом другом месте, где программу выбирают.
 *
 * Анатомия фиксирована: маркер → название → **один** тихий факт → слот [trailing]. Второй строки
 * метаданных у карточки нет намеренно: в v1 их было две равного веса («5 упражнений · посл. сессия
 * 2 июл»), и три таких карточки подряд не давали глазу за что зацепиться. Дату последней сессии
 * забрал экран программы, а [subtitle] — тот самый единственный факт, который формирует
 * вызывающая сторона.
 *
 * Кебаб-меню в v2 нет: единственное действие карточки (удаление) стоит в [trailing] прямо на ней.
 */
@Composable
fun LyteProgramCard(
    title: String,
    subtitle: String,
    accent: LyteAccent,
    glyph: LyteExerciseGlyph,
    onClick: () -> Unit,
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
            modifier = Modifier.padding(
                horizontal = ProgramCardPaddingHorizontal,
                vertical = ProgramCardPaddingVertical,
            ),
        ) {
            LyteExerciseMark(accent = accent, glyph = glyph, size = ProgramCardMarkSize)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = cardTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = ProgramCardSubtitleSpacing),
                )
            }
            trailing?.invoke()
        }
    }
}

@Preview
@Composable
private fun LyteProgramCardPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(ProgramCardSpecimenGap),
            modifier = Modifier.padding(ProgramCardSpecimenPadding),
        ) {
            LyteProgramCard(
                title = "Push Day",
                subtitle = "5 упражнений",
                accent = LyteAccent.Indigo,
                glyph = LyteExerciseGlyph.BenchPress,
                onClick = {},
                trailing = {
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = null,
                        onClick = {},
                        size = ProgramCardSpecimenActionSize,
                    )
                },
            )
            LyteProgramCard(
                title = "Pull Day",
                subtitle = "4 упражнения",
                accent = LyteAccent.Coral,
                glyph = LyteExerciseGlyph.PullUp,
                onClick = {},
                trailing = {
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = null,
                        onClick = {},
                        size = ProgramCardSpecimenActionSize,
                    )
                },
            )
            // Без trailing: так карточка выглядит там, где программу только выбирают.
            LyteProgramCard(
                title = "Очень длинное название программы, которое не влезает",
                subtitle = "3 упражнения",
                accent = LyteAccent.Lime,
                glyph = LyteExerciseGlyph.Squat,
                onClick = {},
            )
        }
    }
}
