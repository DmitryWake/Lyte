package com.nikolaevskii.lyte.core.design.component.picker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_icon_picker_label
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseIcon
import com.nikolaevskii.lyte.core.design.icon.lyteExerciseGlyphLabel
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import org.jetbrains.compose.resources.stringResource

private const val IconPickerColumns = 5
private val IconPickerTileSize = 46.dp
private val IconPickerTileIconSize = 26.dp
private val IconPickerGap = 8.dp

private val IconPickerSpecimenPadding = 16.dp

/**
 * Выбор знака движения: десять круглых тайлов в две строки.
 *
 * Тайл — это и есть будущий маркер в том же размере, в каком он появится в списке, поэтому сетке не
 * нужны ни подписи, ни отдельное превью результата. Названия движений остаются в `contentDescription`
 * — для скринридера.
 *
 * Сетка целиком перекрашивается вслед за [accent], и два пикера читаются как одно решение: выбрал
 * цвет — перекрасилось всё, выбрал знак — маркер готов. Выбранный тайл залит `container` акцента, то
 * есть выглядит ровно так, как будет выглядеть маркер; остальные сидят на `surfaceContainer`.
 */
@Composable
fun LyteExerciseIconPicker(
    value: LyteExerciseGlyph,
    accent: LyteAccent,
    onChange: (LyteExerciseGlyph) -> Unit,
    label: String? = stringResource(Res.string.exercise_icon_picker_label),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (label != null) {
            LytePickerLabel(text = label)
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(IconPickerGap),
            modifier = Modifier.selectableGroup(),
        ) {
            LyteExerciseGlyph.entries.chunked(IconPickerColumns).forEach { rowGlyphs ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(IconPickerGap),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rowGlyphs.forEach { glyph ->
                        // Колонка тянется, тайл в ней остаётся круглым и стоит по центру.
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(1f),
                        ) {
                            GlyphTile(
                                glyph = glyph,
                                accent = accent,
                                selected = glyph == value,
                                onClick = { onChange(glyph) },
                            )
                        }
                    }
                    // Неполная последняя строка не должна растягивать свои тайлы на всю ширину.
                    repeat(IconPickerColumns - rowGlyphs.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun GlyphTile(
    glyph: LyteExerciseGlyph,
    accent: LyteAccent,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LyteTheme.accents[accent]
    val motion = LyteTheme.motion
    val label = lyteExerciseGlyphLabel(glyph)
    val background by animateColorAsState(
        targetValue = if (selected) colors.container else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(
            durationMillis = motion.durationShort,
            easing = motion.easingStandard,
        ),
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(IconPickerTileSize)
            .clip(CircleShape)
            .background(background)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
    ) {
        LyteExerciseIcon(
            glyph = glyph,
            tint = if (selected) colors.fg else MaterialTheme.colorScheme.onSurfaceVariant,
            size = IconPickerTileIconSize,
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun LyteExerciseIconPickerPreview() {
    LyteTheme {
        LyteExerciseIconPicker(
            value = LyteExerciseGlyph.PullUp,
            accent = LyteAccent.Lime,
            onChange = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(IconPickerSpecimenPadding),
        )
    }
}
