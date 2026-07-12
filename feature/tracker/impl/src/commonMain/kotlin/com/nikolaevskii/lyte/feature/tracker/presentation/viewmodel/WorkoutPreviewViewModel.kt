package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toPreviewUiModel
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class WorkoutPreviewViewModel(
    private val programId: String,
    private val workoutRepository: WorkoutRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<WorkoutPreviewUiState, WorkoutPreviewIntent>() {

    init {
        launch { loadProgram() }
    }

    override fun onIntent(intent: WorkoutPreviewIntent) {
        when (intent) {
            WorkoutPreviewIntent.OnStartClicked -> startSession()

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

    /**
     * Старт: снапшот программы в БД → замена стека вкладки экраном сессии (назад в превью/пикер после
     * старта не возвращаемся). Если активная сессия уже есть (инвариант БД «не больше одной» кинул
     * [IllegalStateException] или гонка гейта) — открываем её вместо ошибки.
     */
    private fun startSession() {
        if (uiStateValue.isStarting) {
            return
        }
        updateState { copy(isStarting = true) }
        launch {
            runCatching {
                val workout = checkNotNull(workoutRepository.getWorkout(programId)) { "Workout $programId not found" }
                workoutSessionRepository.startSession(workout)
            }
                .onSuccess { sessionId -> navigateToSession(sessionId) }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val activeSession = runCatching { workoutSessionRepository.getActiveSession() }.getOrNull()
                    if (activeSession != null) {
                        navigateToSession(activeSession.id)
                    } else {
                        updateState { copy(isStarting = false, errorMessage = error.message) }
                    }
                }
        }
    }

    private fun navigateToSession(sessionId: String) {
        lyteNavigator.navigate(
            route = ActiveSessionRoute(sessionId = sessionId),
            options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
        )
    }
}
