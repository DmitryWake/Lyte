package com.nikolaevskii.lyte.core.navigation.model

/**
 * Команда навигации: её отправляет ViewModel, а применяет шелл (`App()`), где живёт `NavController`.
 * VM не знает про `NavController` — только про эти команды и `@Serializable`-роуты из `:feature:<name>:api`.
 */
sealed interface NavCommand {

    /** Переход вперёд на [route] с опциональным шейпингом стека [options]. */
    data class Forward(
        val route: Any,
        val options: LyteNavOptions? = null,
    ) : NavCommand

    /** Возврат на предыдущий экран (`popBackStack`). */
    data object Back : NavCommand

    /** Переключение верхнеуровневой вкладки bottom-bar с сохранением/восстановлением её back stack. */
    data class SwitchTab(val destination: TopLevelDestination) : NavCommand
}
