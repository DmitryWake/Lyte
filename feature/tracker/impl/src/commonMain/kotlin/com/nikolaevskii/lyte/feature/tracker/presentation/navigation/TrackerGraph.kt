package com.nikolaevskii.lyte.feature.tracker.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.WorkoutPickerRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.screen.TrackerLandingScreen
import com.nikolaevskii.lyte.feature.tracker.presentation.screen.WorkoutPickerScreen

fun NavGraphBuilder.trackerGraph() {
    composable<TrackerLandingRoute> {
        TrackerLandingScreen()
    }
    composable<WorkoutPickerRoute> {
        WorkoutPickerScreen()
    }
}
