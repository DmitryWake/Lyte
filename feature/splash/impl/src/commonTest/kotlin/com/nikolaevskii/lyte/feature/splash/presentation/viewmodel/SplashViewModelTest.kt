package com.nikolaevskii.lyte.feature.splash.presentation.viewmodel

import com.nikolaevskii.lyte.core.app.testing.FakeAppLaunchStateRepository
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.feature.splash.SplashRoute
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_MIN_LOADING_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_EXIT_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializationManager
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializer
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashUiState
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashIntent
import com.nikolaevskii.lyte.feature.onboarding.OnboardingRoute
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun successfulInitializationWaitsMinimumDurationThenExitsThenNavigates() =
        runTest(testDispatcher) {
            val navigator = FakeLyteNavigator()
            val manager = AppInitializationManager(initializers = listOf(succeedingInitializer()))
            val viewModel =
                SplashViewModel(
                    appInitializationManager = manager,
                    appLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedOnboarding = true),
                    lyteNavigator = navigator,
                )

            runCurrent()
            assertEquals(SplashUiState.Loading, viewModel.uiState.value)
            assertTrue(
                navigator.navigateCalls.isEmpty(),
                "must not navigate before minimum splash duration elapses"
            )

            advanceTimeBy((SPLASH_MIN_LOADING_DURATION_MS - 1).milliseconds)
            runCurrent()
            assertEquals(SplashUiState.Loading, viewModel.uiState.value)
            assertTrue(navigator.navigateCalls.isEmpty())

            advanceTimeBy(1.milliseconds)
            runCurrent()
            assertEquals(SplashUiState.Exiting, viewModel.uiState.value)
            assertTrue(
                navigator.navigateCalls.isEmpty(),
                "must not navigate before the exit animation finishes"
            )

            advanceTimeBy(SPLASH_EXIT_DURATION_MS.milliseconds)
            runCurrent()
            val (route, options) = navigator.navigateCalls.single()
            assertEquals(TrackerLandingRoute, route)
            assertEquals(LyteNavOptions(popUpTo = SplashRoute, popUpToInclusive = true), options)
            assertFalse(viewModel.uiState.value is SplashUiState.Error)
        }

    @Test
    fun failedInitializationShowsErrorAfterMinimumDurationAndDoesNotNavigate() =
        runTest(testDispatcher) {
            val navigator = FakeLyteNavigator()
            val manager = AppInitializationManager(initializers = listOf(failingInitializer()))
            val viewModel =
                SplashViewModel(
                    appInitializationManager = manager,
                    appLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedOnboarding = true),
                    lyteNavigator = navigator,
                )

            advanceTimeBy(SPLASH_MIN_LOADING_DURATION_MS.milliseconds)
            runCurrent()

            assertEquals(SplashUiState.Error, viewModel.uiState.value)
            assertTrue(navigator.navigateCalls.isEmpty())
        }

    @Test
    fun retryAfterFailureCanSucceed() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        var shouldFail = true
        val initializer = object : AppInitializer {
            override suspend fun initialize() {
                if (shouldFail) error("boom")
            }
        }
        val viewModel = SplashViewModel(
            appInitializationManager = AppInitializationManager(initializers = listOf(initializer)),
            appLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedOnboarding = true),
            lyteNavigator = navigator,
        )
        advanceTimeBy(SPLASH_MIN_LOADING_DURATION_MS.milliseconds)
        runCurrent()
        assertEquals(SplashUiState.Error, viewModel.uiState.value)

        shouldFail = false
        viewModel.onIntent(SplashIntent.Retry)
        advanceTimeBy((SPLASH_MIN_LOADING_DURATION_MS + SPLASH_EXIT_DURATION_MS).milliseconds)
        runCurrent()

        assertEquals(1, navigator.navigateCalls.size)
        assertFalse(viewModel.uiState.value is SplashUiState.Error)
    }

    @Test
    fun retryWhileInitializationInFlightCancelsPreviousAttemptAndNavigatesOnce() =
        runTest(testDispatcher) {
            val navigator = FakeLyteNavigator()
            val manager = AppInitializationManager(initializers = listOf(succeedingInitializer()))
            val viewModel =
                SplashViewModel(
                    appInitializationManager = manager,
                    appLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedOnboarding = true),
                    lyteNavigator = navigator,
                )

            // Первая попытка ещё не дошла до минимальной длительности — второй Retry (например, двойной
            // тап) не должен породить второй параллельный запуск, который потом навигирует повторно.
            runCurrent()
            viewModel.onIntent(SplashIntent.Retry)

            advanceTimeBy((SPLASH_MIN_LOADING_DURATION_MS + SPLASH_EXIT_DURATION_MS).milliseconds)
            runCurrent()

            assertEquals(1, navigator.navigateCalls.size)
        }

    @Test
    fun cancellationDuringInitializationIsNotTreatedAsError() = runTest(testDispatcher) {
        // runCatching не различает CancellationException и обычную ошибку — отмена ViewModel-скоупа
        // не должна попадать в UI как isError.
        val navigator = FakeLyteNavigator()
        val cancellingInitializer = object : AppInitializer {
            override suspend fun initialize() {
                throw CancellationException("cancelled")
            }
        }
        val manager = AppInitializationManager(initializers = listOf(cancellingInitializer))
        val viewModel =
            SplashViewModel(
                    appInitializationManager = manager,
                    appLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedOnboarding = true),
                    lyteNavigator = navigator,
                )

        advanceTimeBy(SPLASH_MIN_LOADING_DURATION_MS.milliseconds)
        runCurrent()

        assertFalse(viewModel.uiState.value is SplashUiState.Error)
        assertTrue(navigator.navigateCalls.isEmpty())
    }

    private fun succeedingInitializer(): AppInitializer = object : AppInitializer {
        override suspend fun initialize() = Unit
    }

    private fun failingInitializer(): AppInitializer = object : AppInitializer {
        override suspend fun initialize(): Unit = error("boom")
    }

    /**
     * Развилка «с чего стартует приложение» живёт в сплэше, а не в обучении, поэтому проверяется
     * здесь. Флаг не выставлен — первым идёт обучение; иначе тур не показался бы никогда.
     */
    @Test
    fun navigatesToOnboardingWhenItWasNotCompleted() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        SplashViewModel(
            appInitializationManager = AppInitializationManager(initializers = emptyList()),
            appLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedOnboarding = false),
            lyteNavigator = navigator,
        )

        advanceTimeBy(SPLASH_MIN_LOADING_DURATION_MS.milliseconds)
        advanceTimeBy(SPLASH_EXIT_DURATION_MS.milliseconds)
        runCurrent()

        assertEquals(expected = OnboardingRoute, actual = navigator.navigateCalls.single().first)
    }

    /** Сбой чтения флага не запирает вход: лишний показ тура дешевле неработающего приложения. */
    @Test
    fun navigatesToTrackerWhenFlagCannotBeRead() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        SplashViewModel(
            appInitializationManager = AppInitializationManager(initializers = emptyList()),
            appLaunchStateRepository = UnreadableAppLaunchStateRepository(),
            lyteNavigator = navigator,
        )

        advanceTimeBy(SPLASH_MIN_LOADING_DURATION_MS.milliseconds)
        advanceTimeBy(SPLASH_EXIT_DURATION_MS.milliseconds)
        runCurrent()

        assertEquals(expected = TrackerLandingRoute, actual = navigator.navigateCalls.single().first)
    }
}
