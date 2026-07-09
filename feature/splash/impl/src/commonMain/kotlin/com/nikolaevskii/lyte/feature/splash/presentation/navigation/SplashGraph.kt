package com.nikolaevskii.lyte.feature.splash.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.nikolaevskii.lyte.feature.splash.SplashRoute
import com.nikolaevskii.lyte.feature.splash.presentation.screen.SplashScreen

fun NavGraphBuilder.splashGraph() {
    composable<SplashRoute> {
        SplashScreen()
    }
}
