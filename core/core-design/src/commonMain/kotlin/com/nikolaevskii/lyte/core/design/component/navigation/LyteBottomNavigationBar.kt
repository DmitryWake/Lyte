package com.nikolaevskii.lyte.core.design.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.icon.LyteIcons

private val BottomNavMarginHorizontal = 16.dp
private val BottomNavMarginBottom = 12.dp
private val BottomNavContainerPadding = 6.dp
private val BottomNavItemPaddingVertical = 8.dp
private val BottomNavIconLabelGap = 3.dp
private val BottomNavIconSize = 22.dp
private const val BottomNavContainerAlpha = 0.82f

/**
 * Место, которое [LyteBottomNavigationBar] в среднем занимает от нижнего края экрана поверх
 * системных инсетов (margin + внутренние отступы + иконка + подпись, с небольшим запасом). Док —
 * плавающий overlay, а не `Scaffold.bottomBar`-слот (тот дёргал бы contentPadding экрана при каждой
 * анимации показа/скрытия дока), поэтому экраны, показывающиеся вместе с доком (корни вкладок),
 * резервируют этим значением низ своих скролл-контейнеров сами — иначе последний элемент списка
 * прячется под доком.
 */
val LyteBottomNavigationBarHeight: Dp = 88.dp

/**
 * Плавающий пилюлеобразный нав-док (3 вкладки). Референс использует backdrop-blur —
 * в Compose Multiplatform это не переносится единообразно между Android/iOS, поэтому
 * фон приближён полупрозрачной заливкой без блюра.
 *
 * Сам учитывает системную нав-зону (жестовую/кнопочную панель), как это делает M3
 * `NavigationBar` — доку не нужно оборачивать в дополнительный `windowInsetsPadding` снаружи.
 */
@Composable
fun LyteBottomNavigationBar(
    items: List<LyteBottomNavigationBarItem>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .padding(horizontal = BottomNavMarginHorizontal)
            .padding(bottom = BottomNavMarginBottom),
        shape = LyteTheme.extendedShapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = BottomNavContainerAlpha),
        shadowElevation = LyteTheme.elevation.level3,
    ) {
        Row(modifier = Modifier.padding(BottomNavContainerPadding)) {
            items.forEach { item ->
                BottomNavDestination(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun BottomNavDestination(item: LyteBottomNavigationBarItem, modifier: Modifier = Modifier) {
    val containerColor = if (item.selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (item.selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BottomNavIconLabelGap),
        modifier = modifier
            .clip(LyteTheme.extendedShapes.full)
            .background(containerColor)
            .clickable(onClick = item.onClick)
            .padding(vertical = BottomNavItemPaddingVertical),
    ) {
        Icon(imageVector = item.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(BottomNavIconSize))
        Text(text = item.label, style = MaterialTheme.typography.labelMedium, color = contentColor)
    }
}

@Preview
@Composable
private fun LyteBottomNavigationBarPreview() {
    LyteTheme {
        LyteBottomNavigationBar(
            items = listOf(
                LyteBottomNavigationBarItem(icon = LyteIcons.Dumbbell, label = "Тренировка", selected = true, onClick = {}),
                LyteBottomNavigationBarItem(icon = LyteIcons.ClipboardList, label = "Программы", selected = false, onClick = {}),
                LyteBottomNavigationBarItem(icon = LyteIcons.History, label = "История", selected = false, onClick = {}),
            ),
        )
    }
}
