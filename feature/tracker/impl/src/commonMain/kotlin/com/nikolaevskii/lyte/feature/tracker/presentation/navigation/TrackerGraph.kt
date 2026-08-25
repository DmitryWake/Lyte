package com.nikolaevskii.lyte.feature.tracker.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.WorkoutPreviewRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.screen.ActiveSessionScreen
import com.nikolaevskii.lyte.feature.tracker.presentation.screen.TrackerLandingScreen
import com.nikolaevskii.lyte.feature.tracker.presentation.screen.WorkoutPreviewScreen

fun NavGraphBuilder.trackerGraph() {
    composable<TrackerLandingRoute> {
        TrackerLandingScreen()
    }
    composable<WorkoutPreviewRoute> { entry ->
        WorkoutPreviewScreen(programId = entry.toRoute<WorkoutPreviewRoute>().programId)
    }
    composable<ActiveSessionRoute> { entry ->
        ActiveSessionScreen(sessionId = entry.toRoute<ActiveSessionRoute>().sessionId)
    }
}
