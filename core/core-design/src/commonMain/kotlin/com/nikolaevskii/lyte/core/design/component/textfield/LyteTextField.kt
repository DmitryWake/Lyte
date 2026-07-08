package com.nikolaevskii.lyte.core.design.component.textfield

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme

private const val MULTILINE_MIN_LINES = 3

/**
 * Текстовое поле M3 (filled): контейнер surfaceContainer, индикатор прозрачный →
 * primary в фокусе. Используется для названий программ/упражнений и заметок к подходу.
 */
@Composable
fun LyteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String? = null,
    multiline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { text -> { Text(text = text) } },
        placeholder = placeholder?.let { text -> { Text(text = text) } },
        singleLine = !multiline,
        minLines = if (multiline) MULTILINE_MIN_LINES else 1,
        shape = MaterialTheme.shapes.large,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Preview
@Composable
private fun LyteTextFieldPreview() {
    LyteTheme {
        LyteTextField(
            value = "",
            onValueChange = {},
            label = "Название программы",
            placeholder = "Push Day",
            modifier = Modifier.padding(16.dp),
        )
    }
}
