package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.card.LyteListRow

private val BottomSheetCornerRadius = 32.dp
private val BottomSheetContentPadding = 20.dp
private val BottomSheetTitleTopPadding = 4.dp
private val BottomSheetTitleBottomPadding = 12.dp
private val BottomSheetTitleWithSubtitleBottomPadding = 4.dp
private val BottomSheetSubtitleBottomPadding = 14.dp
private val BottomSheetContentBottomPadding = 20.dp
private val BottomSheetHandleWidth = 40.dp
private val BottomSheetHandleHeight = 5.dp
private val BottomSheetHandleTopPadding = 12.dp
private val BottomSheetHandleBottomPadding = 6.dp
private const val BottomSheetSubtitleMaxLines = 2

/**
 * Шит-«тянучка» для выбора/редактирования упражнения и переключателя упражнений в сессии.
 * Открывается сразу в развёрнутом на весь экран состоянии (`skipPartiallyExpanded` у
 * [rememberModalBottomSheetState] + `Modifier.fillMaxSize()` на внутреннем [Scaffold]) —
 * промежуточного полу-открытого состояния нет. Закрытие — тап по скриму или свайп вниз; отдельной
 * кнопки закрытия нет. Хэндл — чисто декоративный (см. [BottomSheetDragHandle]): без него M3-дефолт
 * кликабелен и даёт рипл при тапе, что здесь не нужно.
 *
 * [title]/[subtitle] закреплены сверху и не скроллятся вместе с [content] — [content] единственная
 * скроллящаяся часть. [bottomBar] (как `Scaffold.bottomBar`), если передан, закреплён снизу — туда
 * кладётся основное действие шторки («Готово» и т.п.), которое должно быть на виду независимо от
 * прокрутки/длины [content].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyteBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    subtitle: String? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = BottomSheetCornerRadius, topEnd = BottomSheetCornerRadius),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDragHandle() },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = { bottomBar?.invoke() },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(
                        start = BottomSheetContentPadding,
                        end = BottomSheetContentPadding,
                        top = BottomSheetTitleTopPadding,
                        bottom = if (subtitle != null) BottomSheetTitleWithSubtitleBottomPadding else BottomSheetTitleBottomPadding,
                    ),
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = BottomSheetSubtitleMaxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            start = BottomSheetContentPadding,
                            end = BottomSheetContentPadding,
                            bottom = BottomSheetSubtitleBottomPadding,
                        ),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(
                            start = BottomSheetContentPadding,
                            end = BottomSheetContentPadding,
                            bottom = BottomSheetContentBottomPadding,
                        ),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun BottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(top = BottomSheetHandleTopPadding, bottom = BottomSheetHandleBottomPadding)
            .size(width = BottomSheetHandleWidth, height = BottomSheetHandleHeight)
            .background(color = MaterialTheme.colorScheme.outline, shape = LyteTheme.extendedShapes.full),
    )
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

@Preview
@Composable
private fun LyteBottomSheetWithSubtitleAndBottomBarPreview() {
    LyteTheme {
        LyteBottomSheet(
            title = "Жим лёжа",
            subtitle = "Штанга на верхней части спины, присед до параллели бёдер с полом.",
            onDismissRequest = {},
            bottomBar = {
                LyteButton(
                    text = "Готово",
                    onClick = {},
                    fullWidth = true,
                    modifier = Modifier.padding(BottomSheetContentPadding),
                )
            },
        ) {
            LyteListRow(title = "Жим лёжа", onClick = {})
            LyteListRow(title = "Приседания", onClick = {})
        }
    }
}
