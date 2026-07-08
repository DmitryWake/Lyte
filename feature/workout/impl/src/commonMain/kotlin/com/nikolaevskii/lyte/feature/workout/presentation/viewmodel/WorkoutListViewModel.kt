package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState

class WorkoutListViewModel(
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutListUiState, WorkoutListIntent>() {

    override fun onIntent(intent: WorkoutListIntent) {
        when (intent) {
            is WorkoutListIntent.OpenDetails ->
                lyteNavigator.navigate(WorkoutDetailsRoute(id = intent.id))
        }
    }

    override fun getInitialState(): WorkoutListUiState = WorkoutListUiState()
}
