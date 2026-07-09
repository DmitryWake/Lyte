package com.nikolaevskii.lyte

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.navigation.LyteBottomNavigationBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteBottomNavigationBarItem
import com.nikolaevskii.lyte.core.navigation.model.isTopLevelSelected
import com.nikolaevskii.lyte.core.navigation.model.navigateToTopLevel
import com.nikolaevskii.lyte.navigation.LyteBottomBarItem
import com.nikolaevskii.lyte.navigation.LyteNavHost
import org.jetbrains.compose.resources.stringResource

// M3 emphasized-motion tokens (duration + easing) для показа/скрытия дока. Не выставлены наружу как
// public API в этой alpha-версии material3 (MotionTokens/MotionScheme internal), поэтому заданы явно —
// тем же паттерном, что уже принят в SplashScreen.kt для его переходов.
private const val BottomBarEnterDurationMs = 300
private const val BottomBarExitDurationMs = 150
private val BottomBarEnterEasing = CubicBezierEasing(0.05f, 0.70f, 0.10f, 1.0f)
private val BottomBarExitEasing = CubicBezierEasing(0.30f, 0.0f, 0.80f, 0.15f)
private val BottomBarSlideDistance = 24.dp

@Composable
fun App() {
    LyteTheme {
        val navController = rememberNavController()
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        val currentTab = LyteBottomBarItem.entries.firstOrNull { tab -> currentDestination.isTopLevelSelected(tab) }
        val showBottomBar = currentTab != null

        // Подсветка вкладки — от «последней вкладки, на корне которой были», а не от текущего route
        // напрямую: иначе в момент пуша detail-экрана (напр. редактора программы) currentTab на один
        // кадр становится null, и подсвеченная пилюля дока мигала бы в «ничего не выбрано» ровно в
        // момент начала анимации скрытия — до неё, пока не пропал, наиболее заметный дефект.
        var activeTab by remember { mutableStateOf<LyteBottomBarItem?>(null) }
        if (currentTab != null) {
            activeTab = currentTab
        }

        val density = LocalDensity.current
        val slideDistancePx = remember(density) { with(density) { BottomBarSlideDistance.roundToPx() } }

        Scaffold(
            // Верхний системный inset отдаём TopBar экранов — его фон закрывает зону статус-бара.
            // Нижний — системный нав-бар — резервируем здесь всегда: плавающий док теперь overlay вне
            // Scaffold.bottomBar (см. комментарий у Box ниже), а не layout-слот, который раньше сам
            // поглощал эту зону только пока был показан.
            contentWindowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0)
                .union(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                LyteNavHost(
                    navController = navController,
                    paddingValues = paddingValues,
                )

                // Плавающий док НЕ в Scaffold.bottomBar: тот лэйаут-слот меряет фактическую высоту
                // своего содержимого на каждый layout pass, а slide+fade-only exit не уменьшает
                // измеренный размер вместе с визуальной анимацией (это делает только shrinkVertically/
                // changeSize) — контент под ним держал бы полный отступ весь exit и потом падал на
                // один кадр, вместо плавного «контент остаётся на месте, док просто уезжает поверх».
                // Экраны, показывающиеся вместе с доком (корни вкладок), сами резервируют место под
                // него — см. LyteBottomNavigationBarHeight в core-design.
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = fadeIn(tween(durationMillis = BottomBarEnterDurationMs, easing = BottomBarEnterEasing)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = BottomBarEnterDurationMs, easing = BottomBarEnterEasing),
                            initialOffsetY = { slideDistancePx },
                        ),
                    exit = fadeOut(tween(durationMillis = BottomBarExitDurationMs, easing = BottomBarExitEasing)) +
                        slideOutVertically(
                            animationSpec = tween(durationMillis = BottomBarExitDurationMs, easing = BottomBarExitEasing),
                            targetOffsetY = { slideDistancePx },
                        ),
                    modifier = Modifier.align(Alignment.BottomCenter),
                ) {
                    LyteBottomNavigationBar(
                        items = LyteBottomBarItem.entries.map { tab ->
                            LyteBottomNavigationBarItem(
                                icon = tab.icon,
                                label = stringResource(tab.label),
                                selected = tab == activeTab,
                                onClick = { navController.navigateToTopLevel(tab) },
                            )
                        },
                    )
                }
            }
        }
    }
}
