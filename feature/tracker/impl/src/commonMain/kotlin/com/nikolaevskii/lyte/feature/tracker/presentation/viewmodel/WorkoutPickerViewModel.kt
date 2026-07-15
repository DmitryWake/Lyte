package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.tracker.WorkoutPreviewRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerUiState
import com.nikolaevskii.lyte.feature.workout.WorkoutTabGraph
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.launch

class WorkoutPickerViewModel(
    private val workoutRepository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutPickerUiState, WorkoutPickerIntent>() {

    init {
        launch { loadPrograms() }
    }

    override fun onIntent(intent: WorkoutPickerIntent) {
        when (intent) {
            is WorkoutPickerIntent.OnProgramClicked ->
                lyteNavigator.navigate(WorkoutPreviewRoute(programId = intent.id))

            WorkoutPickerIntent.OnCreateProgramClicked -> openWorkoutsTab()
            WorkoutPickerIntent.OnBack -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutPickerUiState = WorkoutPickerUiState()

    /**
     * Сначала снимаем экран выбора со стека вкладки, и только потом переключаемся: `switchTab`
     * сохраняет стек уходящей вкладки (`saveState`), поэтому иначе при возврате на «Трекер»
     * восстановился бы этот экран, а не главный.
     */
    private fun openWorkoutsTab() {
        lyteNavigator.back()
        lyteNavigator.switchTab(WorkoutTabGraph)
    }

    private suspend fun loadPrograms() {
        updateState { copy(isLoading = true, errorMessage = null) }
        runCatching { workoutRepository.getWorkouts() }
            .onSuccess { programs -> updateState { copy(isLoading = false, programs = programs) } }
            .onFailure { error -> updateState { copy(isLoading = false, errorMessage = error.message) } }
    }
}
