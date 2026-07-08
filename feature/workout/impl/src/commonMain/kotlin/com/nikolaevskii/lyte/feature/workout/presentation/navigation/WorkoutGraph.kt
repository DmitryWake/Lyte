package com.nikolaevskii.lyte.feature.workout.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.WorkoutListRoute
import com.nikolaevskii.lyte.feature.workout.presentation.screen.WorkoutDetailsScreen
import com.nikolaevskii.lyte.feature.workout.presentation.screen.WorkoutListScreen

fun NavGraphBuilder.workoutGraph() {
    composable<WorkoutListRoute> {
        WorkoutListScreen()
    }
    composable<WorkoutDetailsRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<WorkoutDetailsRoute>()
        WorkoutDetailsScreen(id = args.id)
    }
}
