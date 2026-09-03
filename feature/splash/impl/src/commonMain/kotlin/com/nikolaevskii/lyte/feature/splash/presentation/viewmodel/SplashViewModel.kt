package com.nikolaevskii.lyte.feature.splash.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.feature.onboarding.OnboardingRoute
import com.nikolaevskii.lyte.feature.splash.SplashRoute
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_MIN_LOADING_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_EXIT_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializationManager
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashIntent
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashUiState
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModel(
    private val appInitializationManager: AppInitializationManager,
    private val appLaunchStateRepository: AppLaunchStateRepository,
    private val lyteNavigator: LyteNavigator,
) : BaseViewModel<SplashUiState, SplashIntent>() {

    private var initializationJob: Job? = null

    init {
        runInitialization()
    }

    override fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.Retry -> runInitialization()
        }
    }

    override fun getInitialState(): SplashUiState = SplashUiState.Loading

    // Отменяем предыдущую попытку перед стартом новой — иначе повторный Retry (например, двойной тап)
    // до завершения текущего запуска породил бы второй параллельный launch{} и мог бы вызвать
    // lyteNavigator.navigate(...) дважды.
    private fun runInitialization() {
        initializationJob?.cancel()
        initializationJob = launch {
            updateState { SplashUiState.Loading }

            // runCatching не умеет отличать CancellationException от обычной ошибки (ловит любой
            // Throwable) — поэтому ниже явно перебрасываем её, иначе отмена ViewModel-скоупа была бы
            // ошибочно показана пользователю как isError.
            val outcome = coroutineScope {
                val minDurationJob = async { delay(SPLASH_MIN_LOADING_DURATION_MS.milliseconds) }
                val workJob = async { runCatching { appInitializationManager.initialize() } }
                val result = workJob.await()
                minDurationJob.await()
                result
            }
            outcome.exceptionOrNull()?.let { throwable ->
                if (throwable is CancellationException) throw throwable
            }

            outcome
                .onSuccess {
                    updateState { SplashUiState.Exiting }
                    delay(SPLASH_EXIT_DURATION_MS.milliseconds)
                    lyteNavigator.navigate(
                        route = nextRoute(),
                        options = LyteNavOptions(popUpTo = SplashRoute, popUpToInclusive = true),
                    )
                }
                .onFailure {
                    updateState { SplashUiState.Error }
                }
        }
    }

    /**
     * Решение «с чего стартует приложение» живёт здесь, а не в самом обучении: тур не делается корнем
     * `NavHost` с самороспуском, иначе развилка размазалась бы по двум местам.
     *
     * Сбой чтения флага не должен запирать вход — считаем обучение пройденным и уходим в трекер:
     * лишний показ тура дешевле, чем неработающее приложение.
     */
    private suspend fun nextRoute(): Any =
        if (runCatching { appLaunchStateRepository.hasCompletedOnboarding() }.getOrDefault(true)) {
            TrackerLandingRoute
        } else {
            OnboardingRoute
        }
}
