package com.nikolaevskii.lyte.feature.onboarding.presentation.viewmodel

import com.nikolaevskii.lyte.core.app.testing.FakeAppLaunchStateRepository
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingIntent
import com.nikolaevskii.lyte.feature.tracker.TrackerLandingRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun skipWritesFlagBeforeNavigating() = runTest(dispatcher) {
        val repository = FakeAppLaunchStateRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = OnboardingViewModel(appLaunchStateRepository = repository, lyteNavigator = navigator)

        viewModel.onIntent(OnboardingIntent.OnSkipClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(repository.hasCompletedOnboarding(), "Флаг не записан — обучение вернётся на следующем запуске")
        assertEquals(expected = 1, actual = navigator.navigateCalls.size)
        assertEquals(expected = TrackerLandingRoute, actual = navigator.navigateCalls.single().first)
    }

    /**
     * Двойной тап по «Пропустить» — не гипотетический: канал навигации буферизованный, и две команды
     * дошли бы обе.
     */
    @Test
    fun doubleSkipSendsSingleNavigationCommand() = runTest(dispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = OnboardingViewModel(
            appLaunchStateRepository = FakeAppLaunchStateRepository(),
            lyteNavigator = navigator,
        )

        viewModel.onIntent(OnboardingIntent.OnSkipClicked)
        viewModel.onIntent(OnboardingIntent.OnSkipClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(expected = 1, actual = navigator.navigateCalls.size)
    }

    /** Guard обязан быть виден экрану, иначе кнопки не погаснут и второй тап станет мёртвым. */
    @Test
    fun leavingIsVisibleWhileFlagIsWritten() = runTest(dispatcher) {
        val viewModel = OnboardingViewModel(
            appLaunchStateRepository = FakeAppLaunchStateRepository(),
            lyteNavigator = FakeLyteNavigator(),
        )

        viewModel.onIntent(OnboardingIntent.OnSkipClicked)

        assertTrue(viewModel.uiState.value.isLeaving)
    }

    /**
     * Пока шагов нет (RD-31), «Показать как это работает» — такой же выход, как «Пропустить»: пустой
     * тур не должен оставлять пользователя на экране, которому нечего показать.
     */
    @Test
    fun startingEmptyTourLeavesInsteadOfShowingNothing() = runTest(dispatcher) {
        val repository = FakeAppLaunchStateRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = OnboardingViewModel(appLaunchStateRepository = repository, lyteNavigator = navigator)

        viewModel.onIntent(OnboardingIntent.OnStartTourClicked)
        testScheduler.advanceUntilIdle()

        assertTrue(repository.hasCompletedOnboarding())
        assertEquals(expected = 1, actual = navigator.navigateCalls.size)
    }

    /** Системное «назад» — тоже выход, и флаг обязан записаться: иначе тур вернётся. */
    @Test
    fun backOnWelcomeWritesFlagAndLeaves() = runTest(dispatcher) {
        val repository = FakeAppLaunchStateRepository()
        val navigator = FakeLyteNavigator()
        val viewModel = OnboardingViewModel(appLaunchStateRepository = repository, lyteNavigator = navigator)

        viewModel.onIntent(OnboardingIntent.OnBackPressed)
        testScheduler.advanceUntilIdle()

        assertTrue(repository.hasCompletedOnboarding())
        assertEquals(expected = 1, actual = navigator.navigateCalls.size)
    }

    /** Сбой записи не запирает вход: лишний показ тура дешевле неработающего приложения. */
    @Test
    fun failedFlagWriteStillLetsUserIn() = runTest(dispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = OnboardingViewModel(
            appLaunchStateRepository = FailingAppLaunchStateRepository(),
            lyteNavigator = navigator,
        )

        viewModel.onIntent(OnboardingIntent.OnSkipClicked)
        testScheduler.advanceUntilIdle()

        assertEquals(expected = 1, actual = navigator.navigateCalls.size)
    }
}
