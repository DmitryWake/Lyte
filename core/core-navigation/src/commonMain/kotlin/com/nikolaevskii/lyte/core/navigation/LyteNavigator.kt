package com.nikolaevskii.lyte.core.navigation

import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions
import com.nikolaevskii.lyte.core.navigation.model.NavCommand
import com.nikolaevskii.lyte.core.navigation.model.TopLevelDestination
import kotlinx.coroutines.flow.Flow

/**
 * Абстракция навигации для ViewModel: VM шлёт команды, единственный подписчик — `App()` — применяет их
 * к `NavController`. Так логика переходов (в т.ч. условная) живёт в VM, а `NavController` не утекает за
 * пределы шелла.
 *
 * Реализация — [LyteNavigatorImpl] (синглтон в DI). Подписчик [commands] должен быть **один** — шелл.
 */
interface LyteNavigator {

    /** Поток команд навигации. Собирается только в `App()`. */
    val commands: Flow<NavCommand>

    /** Перейти на [route] (опционально с шейпингом стека [options]). */
    fun navigate(route: Any, options: LyteNavOptions? = null)

    /** Вернуться назад. */
    fun back()

    /** Переключиться на верхнеуровневую вкладку [destination]. */
    fun switchTab(destination: TopLevelDestination)
}
