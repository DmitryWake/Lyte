package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.feature.tracker.WorkoutPickerRoute
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerLandingViewModelTest {

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
    fun doesNotNavigateOnItsOwn() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()

        TrackerLandingViewModel(lyteNavigator = navigator)

        assertTrue(navigator.commandLog.isEmpty())
    }

    @Test
    fun openWorkoutPickerNavigatesForwardWithinTheSameTab() = runTest(testDispatcher) {
        val navigator = FakeLyteNavigator()
        val viewModel = TrackerLandingViewModel(lyteNavigator = navigator)

        viewModel.onIntent(TrackerLandingIntent.OpenWorkoutPicker)

        assertEquals(
            listOf<NavCommand>(NavCommand.Forward(route = WorkoutPickerRoute, options = null)),
            navigator.commandLog,
        )
    }
}
