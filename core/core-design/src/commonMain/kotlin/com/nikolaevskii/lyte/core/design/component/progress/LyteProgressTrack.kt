package com.nikolaevskii.lyte.core.design.component.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Зазор между сегментами. Не приватный: узкому треку в карточке приходится считать свою ширину
 * самому, а без зазора эту арифметику не сделать — см. [lytePlanTrackWidth].
 */
internal val LyteProgressTrackSegmentGap = 4.dp

private val TonesTrackHeight = 14.dp
private val PlainSegmentHeight = 5.dp
private val SegmentRingWidth = 1.5.dp

private val TonePositiveHeight = 14.dp
private val ToneMetHeight = 9.dp
private val ToneNegativeHeight = 5.dp
private val ToneSkippedHeight = 9.dp
private val ToneTodoHeight = 4.dp

/** Будущий подход — контур поверхности, а не цвет: он не должен спорить с уже случившимся. */
private const val TodoSegmentAlpha = 0.28f

/** Доля акцента в заливке запланированного сегмента; остальное — фон, чтобы тон не бил в глаза. */
private const val PlanSegmentAccentFraction = 0.62f

private val TrackSpecimenGap = 12.dp
private val TrackSpecimenPadding = 16.dp
private val TrackSpecimenPlanWidth = 56.dp
private val TrackSpecimenTitleGap = 4.dp

/**
 * Прогресс подходов сегментами вместо чисел: «как прошло» читается взглядом, а точные значения
 * остаются в одном касании. Заменяет переносящиеся пилюли «10×60 кг» из v1.
 *
 * Три режима — см. [LyteProgressTrackMode]. Ключевое в режиме `Tones`: **исход кодируется высотой**,
 * а не только оттенком (обоснование — в [LyteProgressTone]). Смена состояния анимируется токенами
 * движения, поэтому подход не «перепрыгивает» в новую высоту.
 *
 * Компонент растягивается по ширине контейнера и делит её между сегментами поровну — ширину задаёт
 * вызывающая сторона (`Modifier.width(…)` для узкого трека в карточке упражнения, `fillMaxWidth()`
 * для сводки сессии).
 */
@Composable
fun LyteProgressTrack(
    mode: LyteProgressTrackMode,
    modifier: Modifier = Modifier,
) {
    when (mode) {
        is LyteProgressTrackMode.Tones -> ToneSegments(tones = mode.tones, modifier = modifier)

        is LyteProgressTrackMode.Plan -> PlainSegments(total = mode.total, modifier = modifier) {
            SegmentColors(fill = planSegmentColor(accent = mode.accent))
        }

        is LyteProgressTrackMode.Progress -> PlainSegments(
            total = mode.total,
            modifier = modifier,
        ) { index ->
            when {
                index in mode.missed -> SegmentColors(
                    fill = Color.Transparent,
                    ring = LyteTheme.extendedColors.diffNegative,
                )

                index < mode.done -> SegmentColors(fill = MaterialTheme.colorScheme.primary)

                else -> SegmentColors(fill = todoSegmentColor())
            }
        }
    }
}

@Composable
private fun ToneSegments(
    tones: List<LyteProgressTone>,
    modifier: Modifier = Modifier,
) {
    val motion = LyteTheme.motion
    Row(
        horizontalArrangement = Arrangement.spacedBy(LyteProgressTrackSegmentGap),
        // Сегменты стоят на общей нижней линии — только так разная высота читается как шкала.
        verticalAlignment = Alignment.Bottom,
        modifier = modifier.height(TonesTrackHeight),
    ) {
        tones.forEach { tone ->
            val height by animateDpAsState(
                targetValue = tone.height,
                animationSpec = tween(
                    durationMillis = motion.durationMedium,
                    easing = motion.easingStandard,
                ),
            )
            val colors = toneColors(tone)
            Segment(
                fill = colors.fill,
                ring = colors.ring,
                modifier = Modifier
                    .weight(1f)
                    .height(height),
            )
        }
    }
}

@Composable
private fun PlainSegments(
    total: Int,
    modifier: Modifier = Modifier,
    colorsAt: @Composable (index: Int) -> SegmentColors,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LyteProgressTrackSegmentGap),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        repeat(total) { index ->
            val colors = colorsAt(index)
            Segment(
                fill = colors.fill,
                ring = colors.ring,
                modifier = Modifier
                    .weight(1f)
                    .height(PlainSegmentHeight),
            )
        }
    }
}

@Composable
private fun Segment(
    fill: Color,
    ring: Color?,
    modifier: Modifier = Modifier,
) {
    val motion = LyteTheme.motion
    val animatedFill by animateColorAsState(
        targetValue = fill,
        animationSpec = tween(
            durationMillis = motion.durationMedium,
            easing = motion.easingStandard,
        ),
    )
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(animatedFill)
            .border(
                width = SegmentRingWidth,
                // Обводка рисуется внутрь границ — точный эквивалент `inset` box-shadow из макета.
                color = ring ?: Color.Transparent,
                shape = CircleShape,
            ),
    )
}

/** Подписанный образец для превью: заголовок над треком, чтобы режимы не пришлось угадывать. */
@Composable
private fun TrackSpecimen(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(TrackSpecimenTitleGap),
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

private val LyteProgressTone.height: Dp
    get() = when (this) {
        LyteProgressTone.Positive -> TonePositiveHeight
        LyteProgressTone.Met -> ToneMetHeight
        LyteProgressTone.Negative -> ToneNegativeHeight
        LyteProgressTone.Skipped -> ToneSkippedHeight
        LyteProgressTone.Todo -> ToneTodoHeight
    }

@Composable
private fun toneColors(tone: LyteProgressTone): SegmentColors = when (tone) {
    LyteProgressTone.Positive -> SegmentColors(fill = LyteTheme.extendedColors.diffPositive)
    LyteProgressTone.Met -> SegmentColors(fill = MaterialTheme.colorScheme.primary)
    LyteProgressTone.Negative -> SegmentColors(fill = LyteTheme.extendedColors.diffNegative)
    LyteProgressTone.Skipped -> SegmentColors(
        fill = Color.Transparent,
        ring = LyteTheme.extendedColors.diffSkipped,
    )

    LyteProgressTone.Todo -> SegmentColors(fill = todoSegmentColor())
}

@Composable
private fun todoSegmentColor(): Color =
    MaterialTheme.colorScheme.outline.copy(alpha = TodoSegmentAlpha)

@Composable
private fun planSegmentColor(accent: LyteAccent): Color = mixOverSrgb(
    color = LyteTheme.accents[accent].fg,
    background = MaterialTheme.colorScheme.surfaceContainerHigh,
    fraction = PlanSegmentAccentFraction,
)

/**
 * Смешение в sRGB — эквивалент CSS `color-mix(in srgb, color X%, background)` из дизайн-бандла.
 * Compose-овский `lerp(Color, Color, Float)` интерполирует в Oklab и даёт другой оттенок, поэтому
 * считаем покомпонентно: у `Color` в sRGB компоненты — те же гамма-кодированные значения, что в hex.
 */
private fun mixOverSrgb(color: Color, background: Color, fraction: Float): Color {
    val rest = 1f - fraction
    return Color(
        red = color.red * fraction + background.red * rest,
        green = color.green * fraction + background.green * rest,
        blue = color.blue * fraction + background.blue * rest,
    )
}

@Preview
@Composable
private fun LyteProgressTrackPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(TrackSpecimenGap),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(TrackSpecimenPadding),
        ) {
            TrackSpecimen(title = "Tones · пять исходов подряд") {
                LyteProgressTrack(
                    mode = LyteProgressTrackMode.Tones(
                        tones = listOf(
                            LyteProgressTone.Positive,
                            LyteProgressTone.Met,
                            LyteProgressTone.Negative,
                            LyteProgressTone.Skipped,
                            LyteProgressTone.Todo,
                        ),
                    ),
                )
            }
            TrackSpecimen(title = "Tones · сессия целиком") {
                LyteProgressTrack(
                    mode = LyteProgressTrackMode.Tones(
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
            }
            // Все шесть акцентов: заливка плана обязана быть видна и у самых светлых.
            TrackSpecimen(title = "Plan · 4 подхода, шесть акцентов") {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LyteAccent.entries.forEach { accent ->
                        LyteProgressTrack(
                            mode = LyteProgressTrackMode.Plan(total = 4, accent = accent),
                            modifier = Modifier.width(TrackSpecimenPlanWidth),
                        )
                    }
                }
            }
            TrackSpecimen(title = "Progress · 3 из 6, второй подход мимо") {
                LyteProgressTrack(
                    mode = LyteProgressTrackMode.Progress(
                        total = 6,
                        done = 3,
                        missed = setOf(1),
                    ),
                )
            }
        }
    }
}
