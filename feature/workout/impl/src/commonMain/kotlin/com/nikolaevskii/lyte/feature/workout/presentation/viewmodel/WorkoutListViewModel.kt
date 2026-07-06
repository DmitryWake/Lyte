package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.domain.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.navigation.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val repository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutListUiState, WorkoutListIntent>() {

    init {
        launch {
            runCatching { repository.seedIfEmpty() }
                .onFailure { cause ->
                    updateState { copy(isLoading = false, errorMessage = cause.message) }
                }
        }

        launch {
            repository.observeAll().collect { items ->
                updateState {
                    copy(
                        isLoading = false,
                        items = items,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    override fun onIntent(intent: WorkoutListIntent) {
        when (intent) {
            is WorkoutListIntent.OpenDetails ->
                lyteNavigator.navigate(WorkoutDetailsRoute(id = intent.id))
        }
    }

    override fun getInitialState(): WorkoutListUiState = WorkoutListUiState()
}
