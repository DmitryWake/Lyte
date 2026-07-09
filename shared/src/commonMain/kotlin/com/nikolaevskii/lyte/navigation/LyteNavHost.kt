package com.nikolaevskii.lyte.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.navigation
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.applyOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.core.navigation.model.navigateToTopLevel
import com.nikolaevskii.lyte.feature.history.HistoryRoute
import com.nikolaevskii.lyte.feature.history.presentation.navigation.historyGraph
import com.nikolaevskii.lyte.feature.splash.SplashRoute
import com.nikolaevskii.lyte.feature.splash.presentation.navigation.splashGraph
import com.nikolaevskii.lyte.feature.tracker.TrackerRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.navigation.trackerGraph
import com.nikolaevskii.lyte.feature.workout.WorkoutListRoute
import com.nikolaevskii.lyte.feature.workout.presentation.navigation.workoutGraph
import org.koin.compose.koinInject

@Composable
fun LyteNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    lyteNavigator: LyteNavigator = koinInject(),
) {
    // Единственный подписчик команд навигации: VM шлёт их через Navigator, а шелл применяет к
    // NavController. Так NavController не утекает за пределы App().
    LaunchedEffect(navController) {
        lyteNavigator.commands.collect { command ->
            when (command) {
                is NavCommand.Forward -> navController.navigate(command.route) {
                    command.options?.let { applyOptions(it) }
                }

                NavCommand.Back -> navController.popBackStack()
                is NavCommand.SwitchTab -> navController.navigateToTopLevel(command.destination)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = SplashRoute,
        modifier = Modifier
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues),
    ) {
        splashGraph()

        navigation<BottomNavGraph>(startDestination = TrackerTabGraph) {
            navigation<TrackerTabGraph>(startDestination = TrackerRoute) {
                trackerGraph()
            }
            navigation<WorkoutTabGraph>(startDestination = WorkoutListRoute) {
                workoutGraph()
            }
            navigation<HistoryTabGraph>(startDestination = HistoryRoute) {
                historyGraph()
            }
        }
    }
}
