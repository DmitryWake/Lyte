package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.domain.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsUiState
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val initialId: Long,
    private val repository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutDetailsUiState, WorkoutDetailsIntent>() {

    init {
        launch {
            repository.observeById(initialId).collect { workout ->
                updateState {
                    copy(
                        isLoading = false,
                        workout = workout,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    override fun onIntent(intent: WorkoutDetailsIntent) {
        when (intent) {
            WorkoutDetailsIntent.Back -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutDetailsUiState = WorkoutDetailsUiState(id = initialId)
}
