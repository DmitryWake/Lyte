package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrack
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrackMode
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.design.theme.withTabularNums

private val SessionCardPaddingVertical = 14.dp
private val SessionCardPaddingHorizontal = 16.dp
private val SessionCardGap = 14.dp
private val SessionCardMarkSize = 52.dp
private val SessionCardSubtitleSpacing = 1.dp
private val SessionCardTrackSpacing = 12.dp

/** Геро-число длительности: в шкалу M3 не попадает, в бандле — 19px/700 с плотным трекингом. */
private val SessionCardDurationSize = 19.sp
private val SessionCardDurationTracking = (-0.4).sp

private val SessionCardSpecimenGap = 12.dp
private val SessionCardSpecimenPadding = 16.dp

/**
 * Карточка завершённой сессии в истории (спека 5.1).
 *
 * В v1 карточка упаковывала название, дату, длительность и «15/16 подходов» в две текстовые строки
 * равного веса. В v2 остаётся **одно** геро-число — длительность [duration] на трейлинг-крае, —
 * а соотношение подходов превращается в [track]; ведёт карточку маркер программы.
 *
 * [track] принимает режим целиком, а не список тонов: сводка сессии по-хорошему рисуется
 * `LyteProgressTrackMode.Tones` (исход каждого подхода высотой сегмента), но пока исходы не
 * посчитаны, честнее показать `Progress` из «сколько подходов позади», чем выдумывать тона.
 * `null` — трека нет вообще.
 *
 * Все тексты формирует вызывающая сторона: компонент задаёт только раскладку и стиль.
 */
@Composable
fun LyteSessionCard(
    title: String,
    subtitle: String,
    duration: String,
    accent: LyteAccent,
    glyph: LyteExerciseGlyph,
    onClick: () -> Unit,
    track: LyteProgressTrackMode? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = LyteTheme.elevation.level1),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = SessionCardPaddingHorizontal,
                vertical = SessionCardPaddingVertical,
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SessionCardGap),
            ) {
                LyteExerciseMark(accent = accent, glyph = glyph, size = SessionCardMarkSize)
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
                        modifier = Modifier.padding(top = SessionCardSubtitleSpacing),
                    )
                }
                Text(
                    text = duration,
                    style = MaterialTheme.typography.titleMedium
                        .copy(
                            fontSize = SessionCardDurationSize,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = SessionCardDurationTracking,
                        )
                        .withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            track?.let { mode ->
                LyteProgressTrack(
                    mode = mode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = SessionCardTrackSpacing),
                )
            }
        }
    }
}

@Preview
@Composable
private fun LyteSessionCardPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(SessionCardSpecimenGap),
            modifier = Modifier.padding(SessionCardSpecimenPadding),
        ) {
            LyteSessionCard(
                title = "Push Day",
                subtitle = "2 июля",
                duration = "52 мин",
                accent = LyteAccent.Indigo,
                glyph = LyteExerciseGlyph.BenchPress,
                onClick = {},
                track = LyteProgressTrackMode.Tones(
                    tones = listOf(
                        LyteProgressTone.Met,
                        LyteProgressTone.Met,
                        LyteProgressTone.Positive,
                        LyteProgressTone.Met,
                        LyteProgressTone.Negative,
                        LyteProgressTone.Met,
                        LyteProgressTone.Positive,
                        LyteProgressTone.Skipped,
                    ),
                ),
            )
            // Трек «сколько подходов позади» — вид карточки, пока исходы подходов не посчитаны.
            LyteSessionCard(
                title = "Leg Day",
                subtitle = "28 июня",
                duration = "61 мин",
                accent = LyteAccent.Lime,
                glyph = LyteExerciseGlyph.Squat,
                onClick = {},
                track = LyteProgressTrackMode.Progress(total = 14, done = 13),
            )
            LyteSessionCard(
                title = "Очень длинное название программы, которое не влезает",
                subtitle = "30 июня",
                duration = "8 мин",
                accent = LyteAccent.Coral,
                glyph = LyteExerciseGlyph.PullUp,
                onClick = {},
            )
        }
    }
}
