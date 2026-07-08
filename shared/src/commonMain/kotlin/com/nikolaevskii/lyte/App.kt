package com.nikolaevskii.lyte

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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

@Composable
fun App() {
    LyteTheme {
        val navController = rememberNavController()
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination

        Scaffold(
            bottomBar = {
                LyteBottomNavigationBar(
                    items = LyteBottomBarItem.entries.map { tab ->
                        LyteBottomNavigationBarItem(
                            icon = tab.icon,
                            label = stringResource(tab.label),
                            selected = currentDestination.isTopLevelSelected(tab),
                            onClick = { navController.navigateToTopLevel(tab) },
                        )
                    },
                )
            },
            // Верхний системный inset отдаём TopAppBar экранов — его фон закрывает зону статус-бара.
            // Шелл резервирует только высоту нижнего нав-бара.
            contentWindowInsets = WindowInsets(left = 0, top = 0, right = 0, bottom = 0),
        ) { paddingValues ->
            LyteNavHost(
                navController = navController,
                paddingValues = paddingValues,
            )
        }
    }
}
