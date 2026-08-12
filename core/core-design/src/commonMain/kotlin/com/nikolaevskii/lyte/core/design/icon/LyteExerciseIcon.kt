package com.nikolaevskii.lyte.core.design.icon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import org.jetbrains.compose.resources.painterResource

private val ExerciseIconDefaultSize = 24.dp

private const val GlyphSpecimenColumns = 3
private val GlyphSpecimenTileSize = 46.dp
private val GlyphSpecimenIconSize = 26.dp
private val GlyphSpecimenColumnWidth = 112.dp
private val GlyphSpecimenGap = 8.dp
private val GlyphSpecimenLabelGap = 6.dp
private val GlyphSpecimenPadding = 16.dp

/**
 * Пиктограмма движения, перекрашенная в [tint].
 *
 * Исходники — растровая линейная графика (чёрный штрих на прозрачном), поэтому рисуются не как
 * картинка, а как силуэт: `Icon` накладывает `ColorFilter.tint` по альфе. Без этого чёрный штрих
 * пропадал бы на тёмной теме и внутри насыщенного круга-маркера. Явный [size] обязателен —
 * у растрового `Painter` есть собственный размер, и дефолтные 24dp `Icon` не подставляет.
 *
 * По умолчанию иконка подписана названием движения: она самостоятельна (например, тайл пикера).
 * Внутри круга-маркера, где рядом стоит название упражнения, вызывающая сторона передаёт
 * `contentDescription = null` — знак там декоративен.
 */
@Composable
fun LyteExerciseIcon(
    glyph: LyteExerciseGlyph,
    tint: Color = LocalContentColor.current,
    size: Dp = ExerciseIconDefaultSize,
    contentDescription: String? = lyteExerciseGlyphLabel(glyph),
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(glyph.drawable),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}

@Composable
private fun GlyphSpecimen(
    glyph: LyteExerciseGlyph,
    modifier: Modifier = Modifier,
) {
    // Акценты идут по кругу: специмен заодно показывает, что тонирование работает со всеми шестью.
    val colors = LyteTheme.accents[LyteAccent.entries[glyph.ordinal % LyteAccent.entries.size]]
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GlyphSpecimenLabelGap),
        modifier = modifier.width(GlyphSpecimenColumnWidth),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(GlyphSpecimenTileSize)
                .clip(CircleShape)
                .background(colors.container),
        ) {
            LyteExerciseIcon(
                glyph = glyph,
                tint = colors.fg,
                size = GlyphSpecimenIconSize,
                contentDescription = null,
            )
        }
        Text(
            text = lyteExerciseGlyphLabel(glyph),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun LyteExerciseIconPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(GlyphSpecimenGap),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(GlyphSpecimenPadding),
        ) {
            LyteExerciseGlyph.entries.chunked(GlyphSpecimenColumns).forEach { glyphs ->
                Row(horizontalArrangement = Arrangement.spacedBy(GlyphSpecimenGap)) {
                    glyphs.forEach { glyph ->
                        GlyphSpecimen(glyph = glyph)
                    }
                }
            }
        }
    }
}
