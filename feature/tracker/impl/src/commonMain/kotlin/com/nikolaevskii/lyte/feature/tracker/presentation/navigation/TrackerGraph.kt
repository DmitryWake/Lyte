package com.nikolaevskii.lyte.feature.tracker.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nikolaevskii.lyte.feature.tracker.navigation.TrackerRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.screen.TrackerScreen

fun NavGraphBuilder.trackerGraph() {
    composable<TrackerRoute> {
        TrackerScreen()
    }
}
