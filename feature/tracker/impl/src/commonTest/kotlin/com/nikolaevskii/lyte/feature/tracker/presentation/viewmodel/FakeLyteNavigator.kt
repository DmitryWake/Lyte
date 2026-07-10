package com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel

import com.nikolaevskii.lyte.core.navigation.LyteNavigator
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Пишет команды в один список: порядок важен — уход на другую вкладку это `back()`, а затем
 * `switchTab()`, и обратный порядок оставил бы экран выбора в сохранённом стеке вкладки.
 */
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
