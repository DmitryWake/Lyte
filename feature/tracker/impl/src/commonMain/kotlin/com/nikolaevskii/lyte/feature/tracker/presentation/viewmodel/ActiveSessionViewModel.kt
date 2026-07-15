package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.history.HistorySessionDetailsRoute
import com.nikolaevskii.lyte.feature.history.HistoryTabGraph
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionOverlayUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState.ActiveSessionContent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toActiveSessionUiModel
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Экран активной сессии (спека 4.3). SSOT — БД: каждая мутация уходит в [workoutSessionRepository],
 * после чего сессия перечитывается и её проекция ([ActiveSessionContent]) пересобирается целиком
 * ([mutate]/[applySession]) — состояние экрана нельзя рассинхронизировать с БД, и оно полностью
 * восстанавливается после смерти процесса.
 *
 * Секундомер пересчитывает elapsed от `startedAt` по wall-clock ([clock]) каждый тик, а не копит
 * счётчик: после фона/переводов часов значение самокорректируется, после перезапуска — поднимается из БД.
 */
class ActiveSessionViewModel(
    private val sessionId: String,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val lyteNavigator: LyteNavigator,
    private val clock: Clock,
) : BaseViewModel<ActiveSessionUiState, ActiveSessionIntent>() {

    init {
        launch { loadSession() }
        launch { tickElapsed() }
    }

    override fun onIntent(intent: ActiveSessionIntent) {
        when (intent) {
            ActiveSessionIntent.OnRetryClicked -> launch { loadSession() }
            ActiveSessionIntent.OnBackToLandingClicked -> navigateToLanding()
            is ActiveSessionIntent.OnDraftRepsChanged -> updateTracking { copy(draftReps = intent.reps) }
            is ActiveSessionIntent.OnDraftWeightChanged -> updateTracking { copy(draftWeight = intent.weight) }
            ActiveSessionIntent.OnCompleteSetClicked -> completeCurrentSet()
            ActiveSessionIntent.OnSkipSetClicked -> skipCurrentSet()
            ActiveSessionIntent.OnOpenExerciseSheetClicked -> {
                updateTracking { copy(overlay = ActiveSessionOverlayUiModel.ExerciseSheet) }
            }

            is ActiveSessionIntent.OnExerciseSelected -> selectExercise(intent.exerciseId)
            ActiveSessionIntent.OnOpenNoteSheetClicked -> {
                updateTracking { copy(overlay = ActiveSessionOverlayUiModel.NoteSheet(draft = current.note)) }
            }

            is ActiveSessionIntent.OnNoteDraftChanged -> changeNoteDraft(intent.text)
            ActiveSessionIntent.OnSaveNoteClicked -> saveNote()
            ActiveSessionIntent.OnEndEarlyClicked -> {
                updateTracking { copy(overlay = ActiveSessionOverlayUiModel.EndEarlyDialog) }
            }

            ActiveSessionIntent.OnEndEarlyConfirmed -> finishSession()
            ActiveSessionIntent.OnFinishClicked -> finishSession()
            ActiveSessionIntent.OnDismissOverlay -> updateTracking { copy(overlay = ActiveSessionOverlayUiModel.None) }
        }
    }

    override fun getInitialState(): ActiveSessionUiState = ActiveSessionUiState()

    private suspend fun loadSession() {
        updateState { copy(content = ActiveSessionContent.Loading, startedAt = null, elapsedSeconds = 0) }
        runCatching {
            checkNotNull(workoutSessionRepository.getSession(sessionId)) { "Session $sessionId not found" }
        }
            .onSuccess { session ->
                if (session.finishedAt != null) {
                    // Маршрут протух: сессия уже завершена (напр., восстановлен стек старого процесса).
                    navigateToLanding()
                } else {
                    applySession(session)
                }
            }
            .onFailure { error ->
                if (error is CancellationException) throw error
                updateState { copy(content = ActiveSessionContent.Error) }
            }
    }

    /**
     * Единственный путь записи прогресса: guard от дабл-тапа → мутация → перечитать сессию → пересобрать
     * контент. Перечитывание после каждой мутации гарантирует, что экран показывает ровно то, что в БД.
     */
    private fun mutate(block: suspend () -> Unit) {
        if (uiStateValue.isMutating) {
            return
        }
        updateState { copy(isMutating = true) }
        launch {
            runCatching {
                block()
                applySession(checkNotNull(workoutSessionRepository.getSession(sessionId)) { "Session $sessionId not found" })
            }.onFailure { error ->
                // Отмена скоупа — не ошибка мутации: пробрасываем, чтобы не проглотить cancellation.
                if (error is CancellationException) throw error
                updateTracking { copy(hasMutationError = true) }
            }
            updateState { copy(isMutating = false) }
        }
    }

    /**
     * Пересобирает контент из перечитанной сессии. Драфты степперов сохраняются, только пока не сменился
     * текущий подход (иначе перезаполняются его целью); успешная пересборка гасит баннер ошибки.
     */
    private fun applySession(session: WorkoutSessionEntity) {
        val model = session.toActiveSessionUiModel()
        updateState {
            val current = model.current
            val content = if (current != null) {
                val previous = content as? ActiveSessionContent.Tracking
                val sameSet = previous != null && previous.current.currentSetId == current.currentSetId
                ActiveSessionContent.Tracking(
                    current = current,
                    switcherRows = model.switcherRows,
                    draftReps = if (sameSet) previous.draftReps else current.targetReps,
                    draftWeight = if (sameSet) previous.draftWeight else (current.targetWeight ?: DEFAULT_DRAFT_WEIGHT),
                    overlay = ActiveSessionOverlayUiModel.None,
                    hasMutationError = false,
                )
            } else {
                ActiveSessionContent.AllDone(
                    programName = model.programName,
                    completedCount = model.completedCount,
                    totalCount = model.totalCount,
                )
            }
            copy(content = content, startedAt = model.startedAt, elapsedSeconds = elapsedSecondsFrom(model.startedAt))
        }
    }

    private fun completeCurrentSet() {
        val tracking = uiStateValue.content as? ActiveSessionContent.Tracking ?: return
        val current = tracking.current
        // Для bodyweight-подхода вес в факт не пишем — черновик веса для него не редактируется.
        val weight = tracking.draftWeight.takeIf { current.targetWeight != null }
        mutate {
            workoutSessionRepository.completeSet(setId = current.currentSetId, count = tracking.draftReps, weight = weight)
        }
    }

    private fun skipCurrentSet() {
        val tracking = uiStateValue.content as? ActiveSessionContent.Tracking ?: return
        mutate { workoutSessionRepository.skipSet(setId = tracking.current.currentSetId) }
    }

    private fun selectExercise(exerciseId: String) {
        val tracking = uiStateValue.content as? ActiveSessionContent.Tracking ?: return
        val row = tracking.switcherRows.firstOrNull { candidate -> candidate.exerciseId == exerciseId } ?: return
        if (!row.isSelectable) {
            return
        }
        updateTracking { copy(overlay = ActiveSessionOverlayUiModel.None) }
        if (exerciseId == tracking.current.exerciseId) {
            // Тап по текущему упражнению — просто закрыть шторку, писать в БД нечего.
            return
        }
        mutate { workoutSessionRepository.setCurrentExercise(sessionId = sessionId, sessionExerciseId = exerciseId) }
    }

    private fun changeNoteDraft(text: String) {
        updateTracking {
            if (overlay is ActiveSessionOverlayUiModel.NoteSheet) {
                copy(overlay = ActiveSessionOverlayUiModel.NoteSheet(draft = text))
            } else {
                this
            }
        }
    }

    private fun saveNote() {
        val tracking = uiStateValue.content as? ActiveSessionContent.Tracking ?: return
        val overlay = tracking.overlay as? ActiveSessionOverlayUiModel.NoteSheet ?: return
        updateTracking { copy(overlay = ActiveSessionOverlayUiModel.None) }
        mutate { workoutSessionRepository.saveSetNote(setId = tracking.current.currentSetId, note = overlay.draft) }
    }

    /**
     * Финализация (и досрочная, и с экрана «все подходы выполнены»): репозиторий одной транзакцией
     * помечает незакрытые подходы пропущенными и ставит `finishedAt`. [ActiveSessionUiState.isMutating]
     * на успехе не сбрасывается — экран уходит в навигацию, повторные тапы уже не должны писать.
     */
    private fun finishSession() {
        if (uiStateValue.isMutating) {
            return
        }
        updateState { copy(isMutating = true) }
        updateTracking { copy(overlay = ActiveSessionOverlayUiModel.None) }
        launch {
            runCatching { workoutSessionRepository.finishSession(sessionId) }
                .onSuccess { navigateToSessionDetails() }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    updateState { copy(isMutating = false) }
                    updateTracking { copy(hasMutationError = true) }
                }
        }
    }

    private suspend fun tickElapsed() {
        while (true) {
            val startedAt = uiStateValue.startedAt
            if (startedAt != null) {
                updateState { copy(elapsedSeconds = elapsedSecondsFrom(startedAt)) }
                delay(millisUntilNextSecond(startedAt).milliseconds)
            } else {
                delay(MILLIS_PER_SECOND.milliseconds)
            }
        }
    }

    /** Правит контент только когда экран в трекинге; в остальных состояниях — no-op. */
    private fun updateTracking(block: ActiveSessionContent.Tracking.() -> ActiveSessionContent.Tracking) {
        updateState {
            val tracking = content as? ActiveSessionContent.Tracking ?: return@updateState this
            copy(content = tracking.block())
        }
    }

    private fun elapsedSecondsFrom(startedAt: Instant): Int =
        (clock.now() - startedAt).inWholeSeconds.coerceAtLeast(0).toInt()

    /**
     * Задержка до следующей границы секунды от старта сессии: тики не дрейфуют от времени работы
     * внутри цикла, отображаемые секунды не перескакивают.
     */
    private fun millisUntilNextSecond(startedAt: Instant): Long {
        val sinceStartMillis = (clock.now() - startedAt).inWholeMilliseconds
        return MILLIS_PER_SECOND - sinceStartMillis.mod(MILLIS_PER_SECOND)
    }

    private fun navigateToLanding() {
        lyteNavigator.navigate(
            route = TrackerLandingRoute,
            options = LyteNavOptions(
                popUpTo = ActiveSessionRoute(sessionId = sessionId),
                popUpToInclusive = true,
            ),
        )
    }

    /**
     * После финиша — на детали завершённой сессии (5.2). Экран живёт во вкладке «История», поэтому
     * переключаем вкладку и кладём детали поверх её списка: «назад» с деталей уходит в список Истории.
     *
     * Порядок важен: сперва выкидываем завершённую сессию из стека трекера ([navigateToLanding]) —
     * иначе `switchTab` сохранил бы стек вкладки на мёртвом экране активной сессии, и возврат на
     * трекер показывал бы её вместо лендинга.
     */
    private fun navigateToSessionDetails() {
        navigateToLanding()
        lyteNavigator.switchTab(HistoryTabGraph)
        lyteNavigator.navigate(route = HistorySessionDetailsRoute(sessionId = sessionId))
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1_000L

        /** Черновик веса для bodyweight-подходов: степпер веса скрыт, значение в факт не уходит. */
        const val DEFAULT_DRAFT_WEIGHT = 0.0
    }
}
