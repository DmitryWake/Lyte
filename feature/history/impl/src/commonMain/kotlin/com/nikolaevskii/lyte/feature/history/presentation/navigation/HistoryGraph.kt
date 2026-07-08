package com.nikolaevskii.lyte.feature.history.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nikolaevskii.lyte.feature.history.HistoryRoute
import com.nikolaevskii.lyte.feature.history.presentation.screen.HistoryScreen

fun NavGraphBuilder.historyGraph() {
    composable<HistoryRoute> {
        HistoryScreen()
    }
}
