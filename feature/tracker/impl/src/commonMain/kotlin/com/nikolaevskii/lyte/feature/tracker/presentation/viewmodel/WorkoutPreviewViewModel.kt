package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toPreviewUiModel
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.launch

class WorkoutPreviewViewModel(
    private val programId: String,
    private val workoutRepository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutPreviewUiState, WorkoutPreviewIntent>() {

    init {
        launch { loadProgram() }
    }

    override fun onIntent(intent: WorkoutPreviewIntent) {
        when (intent) {
            // TODO: старт сессии и переход на экран активной сессии (спека 4.3 — экрана ещё нет).
            WorkoutPreviewIntent.OnStartClicked -> Unit

            WorkoutPreviewIntent.OnBack -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutPreviewUiState = WorkoutPreviewUiState()

    private suspend fun loadProgram() {
        updateState { copy(isLoading = true, errorMessage = null) }
        runCatching {
            val workout = checkNotNull(workoutRepository.getWorkout(programId)) { "Workout $programId not found" }
            workout.toPreviewUiModel()
        }
            .onSuccess { program -> updateState { copy(isLoading = false, program = program) } }
            .onFailure { error -> updateState { copy(isLoading = false, errorMessage = error.message) } }
    }
}
