package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.mvi.toLyteError
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListIntent
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutListUiState
import com.nikolaevskii.lyte.feature.workout.presentation.model.toProgramUiModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class WorkoutListViewModel(
    private val workoutRepository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutListUiState, WorkoutListIntent>() {

    init {
        // Реактивный SSOT: список сам подхватывает создание/переименование/удаление из редактора —
        // отдельный OnScreenShown-рефреш больше не нужен.
        launch { observePrograms() }
    }

    override fun onIntent(intent: WorkoutListIntent) {
        when (intent) {
            is WorkoutListIntent.OnProgramClicked -> lyteNavigator.navigate(WorkoutDetailsRoute(id = intent.id))
            WorkoutListIntent.OnCreateProgramClicked -> lyteNavigator.navigate(WorkoutDetailsRoute(id = null))

            is WorkoutListIntent.OnDeleteProgramClicked -> updateState {
                val content = this as? WorkoutListUiState.Content ?: return@updateState this
                val program = content.programs.firstOrNull { it.id == intent.id } ?: return@updateState this
                content.copy(pendingDelete = program, actionError = null)
            }

            WorkoutListIntent.OnDeleteDismissed -> updateState {
                (this as? WorkoutListUiState.Content)?.copy(pendingDelete = null) ?: this
            }

            WorkoutListIntent.OnDeleteConfirmed -> confirmDelete()
        }
    }

    override fun getInitialState(): WorkoutListUiState = WorkoutListUiState.Loading

    // Провал чтения списка уходит в воронку handleError → Error.
    override fun handleError(error: Throwable) {
        updateState { WorkoutListUiState.Error(error.toLyteError()) }
    }

    private suspend fun observePrograms() {
        workoutRepository.observeWorkouts().collect { items ->
            val programs = items.map { it.toProgramUiModel() }
            updateState {
                when {
                    programs.isEmpty() -> WorkoutListUiState.Empty
                    // Фоновое обновление не гасит открытый диалог/баннер — сохраняем поля Content.
                    this is WorkoutListUiState.Content -> copy(programs = programs)
                    else -> WorkoutListUiState.Content(programs = programs)
                }
            }
        }
    }

    /**
     * Удаление обёрнуто в runCatching (без него сбой DAO уронил бы процесс необработанным исключением
     * в viewModelScope). Успех список переспроецирует сам через observeWorkouts; провал — баннер над
     * списком, а не подмена экрана.
     */
    private fun confirmDelete() {
        val pending = (uiStateValue as? WorkoutListUiState.Content)?.pendingDelete ?: return
        launch {
            runCatching { workoutRepository.deleteWorkout(pending.id) }
                .onSuccess {
                    updateState { (this as? WorkoutListUiState.Content)?.copy(pendingDelete = null) ?: this }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    updateState {
                        (this as? WorkoutListUiState.Content)
                            ?.copy(pendingDelete = null, actionError = error.toLyteError()) ?: this
                    }
                }
        }
    }
}
