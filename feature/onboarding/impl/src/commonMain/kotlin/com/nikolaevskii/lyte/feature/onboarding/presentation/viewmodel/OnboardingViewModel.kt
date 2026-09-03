package com.nikolaevskii.lyte.feature.onboarding.presentation.viewmodel

import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.onboarding.OnboardingRoute
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.OnboardingStep
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingIntent
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingUiState
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingUiState.OnboardingContent
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appLaunchStateRepository: AppLaunchStateRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<OnboardingUiState, OnboardingIntent>() {

    override fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.OnStartTourClicked -> startTour()
            OnboardingIntent.OnNextClicked -> goToNextStep()
            OnboardingIntent.OnBackPressed -> goToPreviousStep()
            OnboardingIntent.OnSkipClicked -> finish()
        }
    }

    override fun getInitialState(): OnboardingUiState = OnboardingUiState()

    private fun startTour() {
        // Шагов пока нет (RD-31), и «Показать как это работает» ведёт туда же, куда «Пропустить».
        // Это не заглушка: обучение считается пройденным в обоих случаях, флаг пишется одинаково, и
        // появление шагов не потребует трогать ни один переход.
        if (OnboardingStep.ALL.isEmpty()) {
            finish()
            return
        }
        updateState { copy(content = OnboardingContent.Tour(step = 0)) }
    }

    private fun goToNextStep() {
        val tour = uiStateValue.content as? OnboardingContent.Tour ?: return
        if (tour.isLast) {
            finish()
            return
        }
        updateState { copy(content = tour.copy(step = tour.step + 1)) }
    }

    /** Системное «назад» с первого шага — такой же выход, как «Пропустить»: флаг обязан записаться. */
    private fun goToPreviousStep() {
        val tour = uiStateValue.content as? OnboardingContent.Tour
        if (tour == null || tour.isFirst) {
            finish()
            return
        }
        updateState { copy(content = tour.copy(step = tour.step - 1)) }
    }

    /**
     * Единственная точка выхода: флаг пишется на **любом** пути наружу, и только потом идёт переход.
     * Сбой записи вход не блокирует — уходим в трекер, худшее последствие в том, что обучение
     * покажется ещё раз; запертый на обучении пользователь был бы хуже.
     *
     * Guard держит [OnboardingUiState.isLeaving], а не приватный `Job`: он же гасит кнопки, иначе
     * второй тап был бы мёртвым — экран на него не отвечает, а причина не видна. Без guard'а двойной
     * тап по «Пропустить» отправил бы две команды в буферизованный канал навигации.
     */
    private fun finish() {
        if (uiStateValue.isLeaving) {
            return
        }
        updateState { copy(isLeaving = true) }
        launch {
            runCatching { appLaunchStateRepository.markOnboardingCompleted() }
                .onFailure { error -> if (error is CancellationException) throw error }
            lyteNavigator.navigate(
                route = TrackerLandingRoute,
                options = LyteNavOptions(popUpTo = OnboardingRoute, popUpToInclusive = true),
            )
        }
    }
}
