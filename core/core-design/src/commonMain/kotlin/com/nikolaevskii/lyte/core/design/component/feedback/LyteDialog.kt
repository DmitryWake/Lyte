package com.nikolaevskii.lyte.core.design.component.feedback

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonAccent
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.dialog_cancel_default
import com.nikolaevskii.lyte.core.design.generated.resources.dialog_confirm_default
import org.jetbrains.compose.resources.stringResource

/**
 * Диалог подтверждения деструктивного действия (удалить программу, завершить сессию досрочно).
 * Требует явного подтверждения — вызывающая сторона решает, когда его показывать (composition
 * presence), отдельного флага видимости у компонента нет.
 */
@Composable
fun LyteDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    description: String? = null,
    confirmLabel: String = stringResource(Res.string.dialog_confirm_default),
    cancelLabel: String = stringResource(Res.string.dialog_cancel_default),
    destructive: Boolean = true,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        title = {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
        },
        text = description?.let {
            { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        confirmButton = {
            LyteButton(
                text = confirmLabel,
                onClick = onConfirm,
                variant = LyteButtonVariant.Text,
                accent = if (destructive) LyteButtonAccent.Error else LyteButtonAccent.Primary,
                size = LyteButtonSize.Small,
            )
        },
        dismissButton = {
            LyteButton(
                text = cancelLabel,
                onClick = onDismissRequest,
                variant = LyteButtonVariant.Text,
                size = LyteButtonSize.Small,
            )
        },
    )
}

@Preview
@Composable
private fun LyteDialogPreview() {
    LyteTheme {
        LyteDialog(
            title = "Удалить программу «Push Day»?",
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}
