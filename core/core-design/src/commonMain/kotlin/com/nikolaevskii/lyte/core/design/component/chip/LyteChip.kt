package com.nikolaevskii.lyte.core.design.component.chip

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.lytePressScale

private val ChipHeight = 38.dp
private val ChipIconSize = 18.dp

/** Фильтр-чип для быстрых тегов подхода («тяжело», «легко») и фильтров. */
@Composable
fun LyteChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier
            .lytePressScale(interactionSource)
            .height(ChipHeight),
        leadingIcon = icon?.let {
            { Icon(imageVector = it, contentDescription = null, modifier = Modifier.height(ChipIconSize)) }
        },
        shape = LyteTheme.extendedShapes.full,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        border = null,
        interactionSource = interactionSource,
    )
}

@Preview
@Composable
private fun LyteChipPreview() {
    LyteTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            LyteChip(text = "Тяжело", selected = true, onClick = {}, icon = LyteIcons.Sparkles)
            LyteChip(text = "Легко", selected = false, onClick = {})
        }
    }
}
