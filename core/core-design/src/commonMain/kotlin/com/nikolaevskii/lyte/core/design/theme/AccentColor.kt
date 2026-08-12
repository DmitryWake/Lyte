package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.accent_amber
import com.nikolaevskii.lyte.core.design.generated.resources.accent_coral
import com.nikolaevskii.lyte.core.design.generated.resources.accent_indigo
import com.nikolaevskii.lyte.core.design.generated.resources.accent_lime
import com.nikolaevskii.lyte.core.design.generated.resources.accent_slate
import com.nikolaevskii.lyte.core.design.generated.resources.accent_teal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Коралл, индиго, лайм и сланец взяты из референсной палитры (secondary / tertiary / primary /
 * neutral). Янтарь и бирюза интерполированы между ними в oklch на той же светлоте и цветности —
 * это единственные два цвета системы, которых не было в исходном хендоффе.
 */
internal val LyteLightAccents = LyteAccents(
    coral = LyteAccentColors(fg = Color(0xFFC74323), container = Color(0xFFFFDAD2)),
    indigo = LyteAccentColors(fg = Color(0xFF4B4FAB), container = Color(0xFFE0DEFF)),
    lime = LyteAccentColors(fg = Color(0xFF357600), container = Color(0xFFCDF593)),
    amber = LyteAccentColors(fg = Color(0xFF96600B), container = Color(0xFFFFE0B3)),
    teal = LyteAccentColors(fg = Color(0xFF00706B), container = Color(0xFFB8ECE7)),
    slate = LyteAccentColors(fg = Color(0xFF5C5F58), container = Color(0xFFE1E4D9)),
)

internal val LyteDarkAccents = LyteAccents(
    coral = LyteAccentColors(fg = Color(0xFFFFB19D), container = Color(0xFF5C150A)),
    indigo = LyteAccentColors(fg = Color(0xFFBCBDFF), container = Color(0xFF23266F)),
    lime = LyteAccentColors(fg = Color(0xFFA6D768), container = Color(0xFF0E3300)),
    amber = LyteAccentColors(fg = Color(0xFFF2C078), container = Color(0xFF4A2F00)),
    teal = LyteAccentColors(fg = Color(0xFF7FD6CE), container = Color(0xFF00382F)),
    slate = LyteAccentColors(fg = Color(0xFFC5C8BE), container = Color(0xFF2E312C)),
)

internal val LocalLyteAccents = staticCompositionLocalOf { LyteLightAccents }

private const val AccentSpecimenColumns = 3
private val AccentSpecimenSize = 44.dp
private val AccentSpecimenGlyphSize = 20.dp
private val AccentSpecimenGap = 12.dp
private val AccentSpecimenLabelGap = 6.dp
private val AccentSpecimenPadding = 16.dp

/**
 * Фиксированная ширина колонки специмена. Без неё подпись («Коралловый») распирает колонку, шесть
 * штук перестают влезать в ширину кадра, и `Row` дожимает последнюю до овала.
 */
private val AccentSpecimenColumnWidth = 112.dp

/**
 * Шесть цветов, которые может нести упражнение (и программа). Цветом залит круг-маркер на каждой
 * карточке и строке, поэтому список читается цветом раньше, чем словом.
 *
 * Это **не группы мышц**: таких данных в приложении нет и не планируется — цвет просто свойство
 * упражнения. Сидовые упражнения приезжают уже раскрашенными, созданные пользователем красит
 * пользователь. Шести хватает, чтобы различать список, и мало настолько, чтобы выбор оставался
 * осознанным, а не палитрой.
 *
 * [Slate] — дефолт: упражнение без выбранного цвета всё равно выглядит осознанным.
 */
enum class LyteAccent {
    Coral,
    Indigo,
    Lime,
    Amber,
    Teal,
    Slate,
    ;

    companion object {
        val Default: LyteAccent = Slate
    }
}

/** Пара тонов акцента: [fg] — глиф и обводка, [container] — заливка круга-маркера. */
data class LyteAccentColors(
    val fg: Color,
    val container: Color,
)

/**
 * Палитра акцентов текущей темы. Доступ по значению — `LyteTheme.accents[accent]`:
 * `when` исчерпан на этапе компиляции, поэтому промаха и `null` быть не может.
 */
data class LyteAccents(
    val coral: LyteAccentColors,
    val indigo: LyteAccentColors,
    val lime: LyteAccentColors,
    val amber: LyteAccentColors,
    val teal: LyteAccentColors,
    val slate: LyteAccentColors,
) {
    operator fun get(accent: LyteAccent): LyteAccentColors = when (accent) {
        LyteAccent.Coral -> coral
        LyteAccent.Indigo -> indigo
        LyteAccent.Lime -> lime
        LyteAccent.Amber -> amber
        LyteAccent.Teal -> teal
        LyteAccent.Slate -> slate
    }
}

private val LyteAccent.label: StringResource
    get() = when (this) {
        LyteAccent.Coral -> Res.string.accent_coral
        LyteAccent.Indigo -> Res.string.accent_indigo
        LyteAccent.Lime -> Res.string.accent_lime
        LyteAccent.Amber -> Res.string.accent_amber
        LyteAccent.Teal -> Res.string.accent_teal
        LyteAccent.Slate -> Res.string.accent_slate
    }

/**
 * Подпись цвета («Коралловый», «Индиго», …). Цвет виден глазами, поэтому в интерфейсе слово не
 * рисуется — оно нужно скринридеру и долгому нажатию на кружок пикера.
 */
@Composable
fun lyteAccentLabel(accent: LyteAccent): String = stringResource(accent.label)

@Composable
private fun AccentSpecimen(
    accent: LyteAccent,
    modifier: Modifier = Modifier,
) {
    val colors = LyteTheme.accents[accent]
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AccentSpecimenLabelGap),
        modifier = modifier.width(AccentSpecimenColumnWidth),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(AccentSpecimenSize)
                .clip(CircleShape)
                .background(colors.container),
        ) {
            Box(
                modifier = Modifier
                    .size(AccentSpecimenGlyphSize)
                    .clip(CircleShape)
                    .background(colors.fg),
            )
        }
        Text(
            text = lyteAccentLabel(accent),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun LyteAccentsPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(AccentSpecimenGap),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(AccentSpecimenPadding),
        ) {
            LyteAccent.entries.chunked(AccentSpecimenColumns).forEach { accents ->
                Row(horizontalArrangement = Arrangement.spacedBy(AccentSpecimenGap)) {
                    accents.forEach { accent ->
                        AccentSpecimen(accent = accent)
                    }
                }
            }
        }
    }
}
