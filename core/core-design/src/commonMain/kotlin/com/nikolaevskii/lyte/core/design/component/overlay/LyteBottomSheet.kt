package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField

private val BottomSheetCornerRadius = 32.dp
private val BottomSheetContentPadding = 20.dp
private val BottomSheetTitleTopPadding = 4.dp
private val BottomSheetTitleBottomPadding = 12.dp
private val BottomSheetTitleWithSubtitleBottomPadding = 4.dp
private val BottomSheetSubtitleBottomPadding = 14.dp
private val BottomSheetHandleWidth = 40.dp
private val BottomSheetHandleHeight = 5.dp
private val BottomSheetHandleTopPadding = 12.dp
private val BottomSheetHandleBottomPadding = 6.dp
private const val BottomSheetSubtitleMaxLines = 2

/** Высота шторки: во весь экран либо по высоте собственного контента. */
enum class LyteBottomSheetHeight {

    /** Разворачивается на весь экран. Для длинных и заранее неизвестных по высоте списков. */
    Full,

    /** Занимает ровно столько, сколько нужно контенту. Для коротких форм на пару полей. */
    WrapContent,
}

/**
 * Шит-«тянучка» для выбора/редактирования упражнения и переключателя упражнений в сессии.
 * Промежуточного полу-открытого состояния нет (`skipPartiallyExpanded` у
 * [rememberModalBottomSheetState]) — см. [height] про итоговую высоту. Закрытие — тап по скриму или
 * свайп вниз; отдельной кнопки закрытия нет. Хэндл — чисто декоративный (см. [BottomSheetDragHandle]):
 * без него M3-дефолт кликабелен и даёт рипл при тапе, что здесь не нужно.
 *
 * Слоты сверху вниз: [title], [subtitle], [topContent], [content], [bottomBar]. Всё, кроме
 * [content], закреплено и не скроллится: в [topContent] кладут строку поиска или фильтры, в
 * [bottomBar] — основное действие шторки («Готово», «Создать…»), которое должно быть на виду
 * независимо от длины [content].
 *
 * **Скролл [content] реализует потребитель**, а не штора: короткий контент оборачивают в
 * `Column(Modifier.verticalScroll(...))`, длинные списки — в `LazyColumn`, чтобы строки
 * отрисовывались лениво. При [LyteBottomSheetHeight.Full] [content] получает всю оставшуюся высоту
 * (`weight(1f)`), при [LyteBottomSheetHeight.WrapContent] — свою собственную.
 *
 * **Паддинги штора задаёт только [title] и [subtitle]**; [topContent], [content] и [bottomBar]
 * паддингует потребитель — иначе список скроллился бы не под самый край, а прибитая снизу кнопка не
 * смогла бы растянуть свою подложку с тенью на всю ширину. Горизонтальный отступ, к которому нужно
 * выравниваться, — `LyteTheme.spacing.s5`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyteBottomSheet(
    title: String,
    onDismissRequest: () -> Unit,
    subtitle: String? = null,
    height: LyteBottomSheetHeight = LyteBottomSheetHeight.Full,
    topContent: (@Composable () -> Unit)? = null,
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
        when (height) {
            LyteBottomSheetHeight.Full -> FullHeightSheetLayout(
                title = title,
                subtitle = subtitle,
                topContent = topContent,
                bottomBar = bottomBar,
                content = content,
            )

            LyteBottomSheetHeight.WrapContent -> WrapContentSheetLayout(
                title = title,
                subtitle = subtitle,
                topContent = topContent,
                bottomBar = bottomBar,
                content = content,
            )
        }
    }
}

/**
 * [Scaffold] всегда занимает всю выданную ему высоту, поэтому годится только для полноэкранного
 * режима: он же прижимает [bottomBar] к низу экрана и разводит системные инсеты.
 */
@Composable
private fun FullHeightSheetLayout(
    title: String,
    subtitle: String?,
    topContent: (@Composable () -> Unit)?,
    bottomBar: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    // imePadding: без него клавиатура, поднятая полем из topContent, перекрывает bottomBar.
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = Color.Transparent,
        bottomBar = { bottomBar?.invoke() },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            BottomSheetHeader(title = title, subtitle = subtitle)
            topContent?.invoke()
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                content = content,
            )
        }
    }
}

/**
 * Высота — по контенту, поэтому [Scaffold] здесь неприменим, а [bottomBar] прижат не к низу экрана,
 * а к низу самой шторки (что для неё одно и то же — шторка и так снизу).
 *
 * `navigationBarsPadding().imePadding()` именно в этом порядке: внешний модификатор «съедает»
 * инсет навбара, поэтому `imePadding` добавляет только разницу, а не полную высоту клавиатуры
 * поверх уже отданного отступа.
 */
@Composable
private fun WrapContentSheetLayout(
    title: String,
    subtitle: String?,
    topContent: (@Composable () -> Unit)?,
    bottomBar: (@Composable () -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        BottomSheetHeader(title = title, subtitle = subtitle)
        topContent?.invoke()
        content()
        bottomBar?.invoke()
    }
}

@Composable
private fun BottomSheetHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
            LyteListRow(title = "Жим лёжа", onClick = {}, modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5))
            LyteListRow(title = "Приседания", onClick = {}, modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5))
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
            LyteListRow(title = "Жим лёжа", onClick = {}, modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5))
            LyteListRow(title = "Приседания", onClick = {}, modifier = Modifier.padding(horizontal = LyteTheme.spacing.s5))
        }
    }
}

@Preview
@Composable
private fun LyteBottomSheetWithTopContentPreview() {
    val exercises = listOf("Приседания со штангой", "Становая тяга", "Жим лёжа", "Подтягивания")
    LyteTheme {
        LyteBottomSheet(
            title = "Добавить упражнение",
            onDismissRequest = {},
            topContent = {
                LyteTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "Поиск по названию",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LyteTheme.spacing.s5, vertical = LyteTheme.spacing.s2),
                )
            },
            bottomBar = {
                LyteButton(
                    text = "Создать новое упражнение",
                    onClick = {},
                    fullWidth = true,
                    modifier = Modifier.padding(BottomSheetContentPadding),
                )
            },
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = LyteTheme.spacing.s5),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(items = exercises, key = { name -> name }) { name ->
                    LyteListRow(title = name, onClick = {})
                }
            }
        }
    }
}

@Preview
@Composable
private fun LyteBottomSheetWrapContentPreview() {
    LyteTheme {
        LyteBottomSheet(
            title = "Новое упражнение",
            onDismissRequest = {},
            height = LyteBottomSheetHeight.WrapContent,
            bottomBar = {
                LyteButton(
                    text = "Создать",
                    onClick = {},
                    fullWidth = true,
                    modifier = Modifier.padding(BottomSheetContentPadding),
                )
            },
        ) {
            LyteTextField(
                value = "Жим гантелей на наклонной",
                onValueChange = {},
                label = "Название",
                modifier = Modifier.fillMaxWidth().padding(horizontal = LyteTheme.spacing.s5),
            )
        }
    }
}
