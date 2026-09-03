package com.nikolaevskii.lyte.core.design.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_back
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_remove
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import org.jetbrains.compose.resources.stringResource

enum class LyteTopBarSize { Small, Large }

private val TopBarLargePaddingHorizontal = 24.dp
private val TopBarLargePaddingTop = 6.dp
private val TopBarLargePaddingBottom = 10.dp
private val TopBarLargeActionRowSpacing = 2.dp
private val TopBarLargeTitleTopNoActions = 10.dp
private val TopBarLargeSubtitleSpacing = 4.dp
private val TopBarLargeActionInset = 12.dp
private val TopBarLargeTitleTracking = (-0.6).sp

/**
 * Заголовок экрана. Два размера:
 * [LyteTopBarSize.Small] — компактная строка (кнопка назад + заголовок в одну строку, M3 [TopAppBar]);
 * [LyteTopBarSize.Large] — крупный iOS-заголовок: ряд действий сверху, крупный тайтл, опциональные
 * [subtitle] и [content] (метаданные/фильтры под заголовком).
 *
 * Оба размера сами учитывают статус-бар (M3 [TopAppBar] — из коробки, [LyteTopBarSize.Large] —
 * через явный `windowInsetsPadding`, как [com.nikolaevskii.lyte.core.design.component.navigation.LyteBottomNavigationBar]
 * учитывает нав-зону) — экрану не нужно оборачивать его в дополнительный inset-модификатор снаружи.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyteTopBar(
    title: String,
    size: LyteTopBarSize = LyteTopBarSize.Small,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    if (size == LyteTopBarSize.Large) {
        LargeTopBar(title = title, onBack = onBack, subtitle = subtitle, trailing = trailing, content = content, modifier = modifier)
        return
    }

    TopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = {
            onBack?.let {
                val backLabel = stringResource(Res.string.a11y_back)
                IconButton(onClick = it) {
                    Icon(imageVector = LyteIcons.ChevronLeft, contentDescription = backLabel)
                }
            }
        },
        actions = { trailing?.invoke() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
private fun LargeTopBar(
    title: String,
    onBack: (() -> Unit)?,
    subtitle: String?,
    trailing: (@Composable () -> Unit)?,
    content: (@Composable ColumnScope.() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val hasActionRow = onBack != null || trailing != null
    Column(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            .padding(
                start = TopBarLargePaddingHorizontal,
                end = TopBarLargePaddingHorizontal,
                top = TopBarLargePaddingTop,
                bottom = TopBarLargePaddingBottom,
            ),
    ) {
        if (hasActionRow) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = TopBarLargeActionRowSpacing),
            ) {
                if (onBack != null) {
                    LyteIconButton(
                        icon = LyteIcons.ChevronLeft,
                        contentDescription = stringResource(Res.string.a11y_back),
                        onClick = onBack,
                        modifier = Modifier.offset(x = -TopBarLargeActionInset),
                    )
                } else {
                    Spacer(modifier = Modifier)
                }
                if (trailing != null) {
                    Box(modifier = Modifier.offset(x = TopBarLargeActionInset)) { trailing() }
                } else {
                    Spacer(modifier = Modifier)
                }
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(letterSpacing = TopBarLargeTitleTracking),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = if (hasActionRow) 0.dp else TopBarLargeTitleTopNoActions),
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = TopBarLargeSubtitleSpacing),
            )
        }
        content?.invoke(this)
    }
}

@Preview
@Composable
private fun LyteTopBarSmallPreview() {
    LyteTheme {
        LyteTopBar(title = "Активная сессия", onBack = {})
    }
}

@Preview
@Composable
private fun LyteTopBarLargePreview() {
    LyteTheme {
        // Крупный заголовок рисуется прямо на фоне экрана и своей заливки не имеет — превью
        // подкладывает поверхность само, иначе в тёмной теме светлый тайтл лежит на светлом холсте.
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            LyteTopBar(
                title = "Программы",
                size = LyteTopBarSize.Large,
                onBack = {},
                subtitle = "3 активные программы",
            )
        }
    }
}

/** Заголовок с действием справа — так выглядят детали сессии (5.2) с удалением тренировки. */
@Preview
@Composable
private fun LyteTopBarLargeWithTrailingPreview() {
    LyteTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            LyteTopBar(
                title = "Push Day",
                size = LyteTopBarSize.Large,
                onBack = {},
                subtitle = "6 августа 2026 · начало 19:02",
                trailing = {
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = stringResource(Res.string.a11y_remove),
                        onClick = {},
                    )
                },
            )
        }
    }
}
