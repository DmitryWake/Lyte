package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.mvi.LyteNotFoundException
import com.nikolaevskii.lyte.core.mvi.toLyteError
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.WorkoutPreviewUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toPreviewUiModel
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
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

            is WorkoutPreviewIntent.OnExerciseClicked -> openExerciseInfo(intent.number)

            WorkoutPreviewIntent.OnExerciseInfoDismissed -> closeExerciseInfo()

            WorkoutPreviewIntent.OnBack -> lyteNavigator.back()
        }
    }

    override fun getInitialState(): WorkoutPreviewUiState = WorkoutPreviewUiState.Loading

    // Провал загрузки уходит в воронку handleError → Error (показывать нечего).
    override fun handleError(error: Throwable) {
        updateState { WorkoutPreviewUiState.Error(error.toLyteError()) }
    }

    private suspend fun loadProgram() {
        updateState { WorkoutPreviewUiState.Loading }
        val workout = workoutRepository.getWorkout(programId)
            ?: throw LyteNotFoundException("Workout $programId not found")
        updateState { WorkoutPreviewUiState.Content(program = workout.toPreviewUiModel()) }
    }

    /**
     * Старт: снапшот программы в БД → замена стека вкладки экраном сессии (назад в превью/пикер после
     * старта не возвращаемся). Если активная сессия уже есть (инвариант БД «не больше одной» кинул
     * исключение или гонка гейта) — открываем её вместо ошибки. Неудачный старт не подменяет экран:
     * состав остаётся, показывается баннер [WorkoutPreviewUiState.Content.startError].
     */
    private fun startSession() {
        val content = uiStateValue as? WorkoutPreviewUiState.Content ?: return
        if (content.isStarting) {
            return
        }
        updateState { (this as? WorkoutPreviewUiState.Content)?.copy(isStarting = true) ?: this }
        launch {
            runCatching {
                val workout = workoutRepository.getWorkout(programId)
                    ?: throw LyteNotFoundException("Workout $programId not found")
                workoutSessionRepository.startSession(workout)
            }
                .onSuccess { sessionId -> navigateToSession(sessionId) }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    val activeSession = runCatching { workoutSessionRepository.getActiveSession() }.getOrNull()
                    if (activeSession != null) {
                        navigateToSession(activeSession.id)
                    } else {
                        updateState {
                            (this as? WorkoutPreviewUiState.Content)
                                ?.copy(isStarting = false, startError = error.toLyteError()) ?: this
                        }
                    }
                }
        }
    }

    /**
     * Состав уже загружен, поэтому шторка открывается синхронно. Номер, которому не нашлось
     * упражнения, состояние не трогает: клик по исчезнувшей карточке не должен открывать пустоту.
     */
    private fun openExerciseInfo(number: Int) {
        updateState {
            val content = this as? WorkoutPreviewUiState.Content ?: return@updateState this
            val exercise = content.program.exercises.firstOrNull { exercise -> exercise.number == number }
                ?: return@updateState this
            content.copy(exerciseInfo = exercise)
        }
    }

    private fun closeExerciseInfo() {
        updateState {
            (this as? WorkoutPreviewUiState.Content)?.copy(exerciseInfo = null) ?: this
        }
    }

    private fun navigateToSession(sessionId: String) {
        lyteNavigator.navigate(
            route = ActiveSessionRoute(sessionId = sessionId),
            options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
        )
    }
}
