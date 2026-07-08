package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.card.LyteListRow

private val BottomSheetCornerRadius = 32.dp
private val BottomSheetContentPadding = 20.dp
private val BottomSheetTitleVerticalPadding = 4.dp

/**
 * Шит-«тянучка» для выбора/редактирования упражнения и переключателя упражнений в сессии.
 * Закрытие — тап по скриму или свайп за хэндл (стандартное поведение M3 [ModalBottomSheet]);
 * отдельной кнопки закрытия нет.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyteBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        shape = RoundedCornerShape(topStart = BottomSheetCornerRadius, topEnd = BottomSheetCornerRadius),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = BottomSheetContentPadding, vertical = BottomSheetTitleVerticalPadding),
        )
        content()
    }
}

@Preview
@Composable
private fun LyteBottomSheetPreview() {
    LyteTheme {
        LyteBottomSheet(title = "Выбор упражнения", onDismissRequest = {}) {
            LyteListRow(title = "Жим лёжа", onClick = {})
            LyteListRow(title = "Приседания", onClick = {})
        }
    }
}
