package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.mvi.toLyteError
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.feature.tracker.WorkoutPreviewRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPickerUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toProgramUiModel
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

    override fun getInitialState(): WorkoutPickerUiState = WorkoutPickerUiState.Loading

    override fun handleError(error: Throwable) {
        updateState { WorkoutPickerUiState.Error(error.toLyteError()) }
    }

    /**
     * Сначала снимаем экран выбора со стека вкладки, и только потом переключаемся: `switchTab`
     * сохраняет стек уходящей вкладки (`saveState`), поэтому иначе при возврате на «Трекер»
     * восстановился бы этот экран, а не главный.
     */
    private fun openWorkoutsTab() {
        lyteNavigator.back()
        lyteNavigator.switchTab(WorkoutTabGraph)
    }

    // Ошибка загрузки уходит в handleError (воронка BaseViewModel) → WorkoutPickerUiState.Error.
    private suspend fun loadPrograms() {
        updateState { WorkoutPickerUiState.Loading }
        val programs = workoutRepository.getWorkouts().map { it.toProgramUiModel() }
        updateState {
            if (programs.isEmpty()) WorkoutPickerUiState.Empty else WorkoutPickerUiState.Content(programs)
        }
    }
}
