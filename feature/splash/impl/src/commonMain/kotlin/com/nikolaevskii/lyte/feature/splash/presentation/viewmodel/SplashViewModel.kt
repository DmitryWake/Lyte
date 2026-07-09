package com.nikolaevskii.lyte.feature.splash.presentation.viewmodel

import com.nikolaevskii.lyte.core.mvi.BaseViewModel
import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.splash.SplashRoute
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_MIN_LOADING_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_REVEAL_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializationManager
import com.nikolaevskii.lyte.feature.splash.presentation.model.SplashPhaseUiModel
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashIntent
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashUiState
import com.nikolaevskii.lyte.feature.tracker.TrackerRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModel(
    private val appInitializationManager: AppInitializationManager,
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

    override fun getInitialState(): SplashUiState = SplashUiState()

    // Отменяем предыдущую попытку перед стартом новой — иначе повторный Retry (например, двойной тап)
    // до завершения текущего запуска породил бы второй параллельный launch{} и мог бы вызвать
    // lyteNavigator.navigate(...) дважды.
    private fun runInitialization() {
        initializationJob?.cancel()
        initializationJob = launch {
            updateState { copy(phase = SplashPhaseUiModel.Blinking, isError = false) }

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
                    updateState { copy(phase = SplashPhaseUiModel.Revealing) }
                    delay(SPLASH_REVEAL_DURATION_MS.milliseconds)
                    lyteNavigator.navigate(
                        route = TrackerRoute,
                        options = LyteNavOptions(popUpTo = SplashRoute, popUpToInclusive = true),
                    )
                }
                .onFailure {
                    updateState { copy(phase = SplashPhaseUiModel.Blinking, isError = true) }
                }
        }
    }
}
