package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState

class WorkoutDetailsViewModel(
    private val initialId: Long,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutDetailsUiState, WorkoutDetailsIntent>() {

    override fun onIntent(intent: WorkoutDetailsIntent) {
        when (intent) {
            WorkoutDetailsIntent.Back -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutDetailsUiState = WorkoutDetailsUiState(id = initialId)
}
