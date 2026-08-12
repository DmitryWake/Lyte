package com.nikolaevskii.lyte.core.design.component.mark

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseIcon
import com.nikolaevskii.lyte.core.design.theme.LyteAccent

private val ExerciseMarkDefaultSize = 44.dp

/** Доля глифа от диаметра круга: знак заполняет маркер, но не упирается в его края. */
private const val ExerciseMarkGlyphRatio = 0.58f

private val MarkSpecimenSizes = listOf(36.dp, 38.dp, 52.dp)
private val MarkSpecimenGap = 12.dp
private val MarkSpecimenPadding = 16.dp

/**
 * Круг-маркер упражнения — единственный визуальный якорь карточек и строк. Несёт сразу два сигнала:
 * цвет ([accent], заливка круга) и знак движения ([glyph], рисунок внутри). Оба — обычные свойства
 * упражнения, которые выбрал пользователь; ничего не выводится из данных и никакой таксономии за
 * ними нет.
 *
 * Читается на любом размере, поэтому строка пикера (36dp) и карточка (52dp) оформляются одинаково.
 * Фотографий и иллюстраций в системе нет: параметр `image` веб-версии сюда намеренно не перенесён.
 *
 * По умолчанию маркер декоративен ([contentDescription] = `null`): рядом всегда стоит название
 * упражнения, и озвучивать «Присед» вторым голосом незачем. Подпись нужна там, где по самому
 * маркеру нажимают — например, когда он открывает шторку «Цвет и знак».
 */
@Composable
fun LyteExerciseMark(
    accent: LyteAccent,
    glyph: LyteExerciseGlyph,
    size: Dp = ExerciseMarkDefaultSize,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LyteTheme.accents[accent]
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.container),
    ) {
        LyteExerciseIcon(
            glyph = glyph,
            tint = colors.fg,
            size = size * ExerciseMarkGlyphRatio,
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
private fun LyteExerciseMarkPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(MarkSpecimenGap),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(MarkSpecimenPadding),
        ) {
            // Размеры из макетов: 36 (строка пикера), 38 (карточка упражнения), 52 (карточка программы).
            MarkSpecimenSizes.forEach { size ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LyteAccent.entries.forEachIndexed { index, accent ->
                        LyteExerciseMark(
                            accent = accent,
                            glyph = LyteExerciseGlyph.entries[index],
                            size = size,
                        )
                    }
                }
            }
        }
    }
}
