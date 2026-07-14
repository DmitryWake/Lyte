package com.nikolaevskii.lyte.feature.history.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** Пишет команды навигации в один список — порядок и содержимое проверяются в тестах ViewModel. */
internal class FakeLyteNavigator : LyteNavigator {

    val commandLog = mutableListOf<NavCommand>()

    override val commands: Flow<NavCommand> = MutableSharedFlow()

    override fun navigate(route: Any, options: LyteNavOptions?) {
        commandLog += NavCommand.Forward(route = route, options = options)
    }

    override fun back() {
        commandLog += NavCommand.Back
    }

    override fun switchTab(graphRoute: Any) {
        commandLog += NavCommand.SwitchTab(graphRoute = graphRoute)
    }
}
