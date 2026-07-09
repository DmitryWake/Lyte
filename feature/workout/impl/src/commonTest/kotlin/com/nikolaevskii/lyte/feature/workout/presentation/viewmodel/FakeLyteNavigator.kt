package com.nikolaevskii.lyte.feature.workout.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.core.navigation.model.TopLevelDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class FakeLyteNavigator : LyteNavigator {

    val navigateCalls = mutableListOf<Pair<Any, LyteNavOptions?>>()
    var backCallCount: Int = 0
        private set

    override val commands: Flow<NavCommand> = MutableSharedFlow()

    override fun navigate(route: Any, options: LyteNavOptions?) {
        navigateCalls += route to options
    }

    override fun back() {
        backCallCount++
    }

    override fun switchTab(destination: TopLevelDestination) = Unit
}
