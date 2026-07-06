package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryUiState

class HistoryViewModel : BaseViewModel<HistoryUiState, HistoryIntent>() {

    override fun onIntent(intent: HistoryIntent) = Unit

    override fun getInitialState(): HistoryUiState = HistoryUiState()
}
