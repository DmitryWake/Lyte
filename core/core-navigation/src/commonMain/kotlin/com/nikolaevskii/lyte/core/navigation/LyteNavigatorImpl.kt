package com.nikolaevskii.lyte.core.navigation

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Реализация [LyteNavigator] поверх [Channel] (FIFO, один потребитель). [Channel.BUFFERED] гарантирует, что
 * команда, отправленная в момент пересоздания подписчика (config change на Android), не теряется, а
 * доезжает после переподписки — в отличие от `SharedFlow(replay = 0)`.
 */
class LyteNavigatorImpl : LyteNavigator {

    private val commandChannel = Channel<NavCommand>(Channel.BUFFERED)

    override val commands: Flow<NavCommand> = commandChannel.receiveAsFlow()

    override fun navigate(route: Any, options: LyteNavOptions?) {
        commandChannel.trySend(NavCommand.Forward(route = route, options = options))
    }

    override fun back() {
        commandChannel.trySend(NavCommand.Back)
    }

    override fun switchTab(graphRoute: Any) {
        commandChannel.trySend(NavCommand.SwitchTab(graphRoute = graphRoute))
    }
}
