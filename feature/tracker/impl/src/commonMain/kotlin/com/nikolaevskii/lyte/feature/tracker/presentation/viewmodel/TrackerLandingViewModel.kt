package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.mvi.toLyteError
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import com.nikolaevskii.lyte.feature.tracker.ActiveSessionRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import com.nikolaevskii.lyte.feature.tracker.WorkoutPreviewRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ProgramPickerUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toProgramUiModel
import com.nikolaevskii.lyte.feature.workout.WorkoutDetailsRoute
import com.nikolaevskii.lyte.feature.workout.WorkoutTabGraph
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Корень вкладки «Трекер» и гейт активной сессии: если в БД есть незавершённая сессия (в т.ч. после
 * смерти процесса), лендинг не показывается — стек вкладки заменяется её экраном. Иначе — лендинг,
 * на котором программу выбирают шторкой (спека 4.1), а не отдельным экраном.
 */
class TrackerLandingViewModel(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val workoutRepository: WorkoutRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<TrackerLandingUiState, TrackerLandingIntent>() {

    init {
        launch { checkActiveSession() }
    }

    override fun onIntent(intent: TrackerLandingIntent) {
        when (intent) {
            TrackerLandingIntent.OnStartClicked -> openPicker()
            TrackerLandingIntent.OnPickerDismissed -> closePicker()

            is TrackerLandingIntent.OnProgramClicked -> {
                closePicker()
                lyteNavigator.navigate(WorkoutPreviewRoute(programId = intent.id))
            }

            // Прототип из шторки уводит сразу в редактор новой программы, а не в список: пустая
            // вкладка «Программы» сама по себе не отвечает на «создайте первую».
            TrackerLandingIntent.OnCreateProgramClicked -> {
                closePicker()
                lyteNavigator.switchTab(WorkoutTabGraph)
                lyteNavigator.navigate(WorkoutDetailsRoute(id = null))
            }
        }
    }

    override fun getInitialState(): TrackerLandingUiState = TrackerLandingUiState.CheckingSession

    /** Сюда приходит только сбой загрузки программ: гейт активной сессии ловит своё исключение сам. */
    override fun handleError(error: Throwable) {
        updateOpenPicker(ProgramPickerUiModel.Error(error.toLyteError()))
    }

    private suspend fun checkActiveSession() {
        val activeSession = try {
            workoutSessionRepository.getActiveSession()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Не смогли проверить — не блокируем вкладку, показываем обычный лендинг.
            null
        }
        if (activeSession != null) {
            lyteNavigator.navigate(
                route = ActiveSessionRoute(sessionId = activeSession.id),
                options = LyteNavOptions(popUpTo = TrackerLandingRoute, popUpToInclusive = true),
            )
        } else {
            updateState { TrackerLandingUiState.NoActiveSession() }
        }
    }

    /**
     * Программы читаются на каждое открытие шторки, а не один раз в `init`: гейт может увести с
     * лендинга на активную сессию (и запрос оказался бы лишним), а пока лендинг лежит в стеке
     * вкладки, список программ могли изменить на вкладке «Программы».
     */
    private fun openPicker() {
        updateState {
            if (this is TrackerLandingUiState.NoActiveSession) copy(picker = ProgramPickerUiModel.Loading) else this
        }
        launch {
            val programs = workoutRepository.getWorkouts().map { workout -> workout.toProgramUiModel() }
            updateOpenPicker(
                if (programs.isEmpty()) {
                    ProgramPickerUiModel.Empty
                } else {
                    ProgramPickerUiModel.Programs(programs)
                },
            )
        }
    }

    private fun closePicker() {
        updateState {
            if (this is TrackerLandingUiState.NoActiveSession) copy(picker = null) else this
        }
    }

    /**
     * Обновляет содержимое шторки, только если она ещё открыта: иначе поздний ответ загрузки или
     * ошибка открыли бы её обратно уже после того, как пользователь её закрыл.
     */
    private fun updateOpenPicker(content: ProgramPickerUiModel) {
        updateState {
            if (this is TrackerLandingUiState.NoActiveSession && picker != null) copy(picker = content) else this
        }
    }
}
