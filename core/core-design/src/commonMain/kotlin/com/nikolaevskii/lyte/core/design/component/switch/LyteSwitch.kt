package com.nikolaevskii.lyte.core.design.component.switch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme

private val SwitchLabelSpacing = 12.dp

/** M3-переключатель с опциональной подписью справа. */
@Composable
fun LyteSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String? = null,
    modifier: Modifier = Modifier,
) {
    if (label == null) {
        Switch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SwitchLabelSpacing),
        modifier = modifier,
    ) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
private fun LyteSwitchPreview() {
    LyteTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            LyteSwitch(checked = true, onCheckedChange = {}, label = "Уведомления")
        }
    }
}
