package com.nikolaevskii.lyte.feature.history.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState

data class HistoryUiState(
    val isLoading: Boolean = false,
) : UiState

sealed interface HistoryIntent : UiIntent
