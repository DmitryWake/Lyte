package com.nikolaevskii.lyte.core.design.component.picker

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.accent_picker_label
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.design.theme.lyteAccentLabel
import org.jetbrains.compose.resources.stringResource

private val AccentSwatchSize = 44.dp
private val AccentSwatchRingGap = 2.5.dp
private val AccentSwatchRingWidth = 2.5.dp

/**
 * Слот кружка вместе с кольцом выбора. Кольцо входит в габариты компонента, а не висит поверх
 * соседей box-shadow'ом, как в вебе: иначе его срезал бы первый же скроллер или шторка.
 */
private val AccentSwatchSlotSize = AccentSwatchSize + (AccentSwatchRingGap + AccentSwatchRingWidth) * 2

private val AccentPickerSpecimenPadding = 16.dp

/**
 * Выбор цвета упражнения или программы: шесть кружков, залитых тем самым `container`, которым будет
 * залит круг-маркер.
 *
 * Выбранный отмечен **кольцом снаружи, а не галочкой**: галочка внутри кружка закрывает собой цвет,
 * который в этот момент и выбирают. Между кружком и кольцом остаётся зазор — он ничем не залит и
 * показывает фон формы, поэтому пикер одинаково выглядит и на карточке, и в шторке.
 */
@Composable
fun LyteAccentPicker(
    value: LyteAccent,
    onChange: (LyteAccent) -> Unit,
    label: String? = stringResource(Res.string.accent_picker_label),
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (label != null) {
            LytePickerLabel(text = label)
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
        ) {
            LyteAccent.entries.forEach { accent ->
                AccentSwatch(
                    accent = accent,
                    selected = accent == value,
                    onClick = { onChange(accent) },
                )
            }
        }
    }
}

@Composable
private fun AccentSwatch(
    accent: LyteAccent,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LyteTheme.accents[accent]
    val motion = LyteTheme.motion
    val label = lyteAccentLabel(accent)
    val ringColor by animateColorAsState(
        targetValue = if (selected) colors.fg else Color.Transparent,
        animationSpec = tween(
            durationMillis = motion.durationShort,
            easing = motion.easingStandard,
        ),
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(AccentSwatchSlotSize)
            .clip(CircleShape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = label }
            .border(
                width = AccentSwatchRingWidth,
                color = ringColor,
                shape = CircleShape,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(AccentSwatchSize)
                .clip(CircleShape)
                .background(colors.container),
        )
    }
}

@Preview
@Composable
private fun LyteAccentPickerPreview() {
    LyteTheme {
        LyteAccentPicker(
            value = LyteAccent.Lime,
            onChange = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(AccentPickerSpecimenPadding),
        )
    }
}
