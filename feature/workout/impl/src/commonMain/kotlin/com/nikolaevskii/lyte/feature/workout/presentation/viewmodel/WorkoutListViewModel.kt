package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val workoutRepository: WorkoutRepository,
) : BaseViewModel<WorkoutListUiState, WorkoutListIntent>() {

    init {
        launch { loadWorkouts() }
    }

    override fun onIntent(intent: WorkoutListIntent) {
        when (intent) {
            // Редактор программы (3.2) — отдельная задача, пока переход никуда не ведёт.
            is WorkoutListIntent.OpenDetails -> Unit
            WorkoutListIntent.CreateProgram -> Unit

            is WorkoutListIntent.RequestDelete -> updateState { copy(pendingDeleteId = intent.id) }
            WorkoutListIntent.CancelDelete -> updateState { copy(pendingDeleteId = null) }
            WorkoutListIntent.ConfirmDelete -> confirmDelete()
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
