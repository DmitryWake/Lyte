package com.nikolaevskii.lyte.feature.splash.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.splash.presentation.model.SplashPhaseUiModel

data class SplashUiState(
    val phase: SplashPhaseUiModel = SplashPhaseUiModel.Blinking,
    val isError: Boolean = false,
) : UiState

sealed interface SplashIntent : UiIntent {
    data object Retry : SplashIntent
}
