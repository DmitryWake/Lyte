package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val workoutRepository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutListUiState, WorkoutListIntent>() {

    init {
        launch { loadWorkouts() }
    }

    override fun onIntent(intent: WorkoutListIntent) {
        when (intent) {
            WorkoutListIntent.OnScreenShown -> launch { loadWorkouts() }

            is WorkoutListIntent.OnProgramClicked -> lyteNavigator.navigate(WorkoutDetailsRoute(id = intent.id))
            WorkoutListIntent.OnCreateProgramClicked -> lyteNavigator.navigate(WorkoutDetailsRoute(id = null))

            is WorkoutListIntent.OnDeleteProgramClicked -> updateState { copy(pendingDeleteId = intent.id) }
            WorkoutListIntent.OnDeleteDismissed -> updateState { copy(pendingDeleteId = null) }
            WorkoutListIntent.OnDeleteConfirmed -> confirmDelete()
        }
    }

    override fun getInitialState(): WorkoutListUiState = WorkoutListUiState()

    private fun confirmDelete() {
        val id = uiStateValue.pendingDeleteId ?: return
        launch {
            workoutRepository.deleteWorkout(id)
            updateState { copy(pendingDeleteId = null) }
            loadWorkouts()
        }
    }

    private suspend fun loadWorkouts() {
        updateState { copy(isLoading = true, errorMessage = null) }
        runCatching { workoutRepository.getWorkouts() }
            .onSuccess { programs -> updateState { copy(isLoading = false, programs = programs) } }
            .onFailure { error -> updateState { copy(isLoading = false, errorMessage = error.message) } }
    }
}
