package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerUiState
import com.nikolaevskii.lyte.feature.workout.WorkoutListRoute

class TrackerViewModel(
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<TrackerUiState, TrackerIntent>() {

    override fun onIntent(intent: TrackerIntent) {
        when (intent) {
            TrackerIntent.OpenWorkouts -> lyteNavigator.navigate(WorkoutListRoute)
        }
    }

    override fun getInitialState(): TrackerUiState = TrackerUiState()
}
