
package com.nikolaevskii.lyte.core.navigation

import androidx.navigation.NavOptionsBuilder
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions

internal fun NavOptionsBuilder.singleTop() {
    launchSingleTop = true
}

internal fun NavOptionsBuilder.restorable() {
    restoreState = true
}

/**
 * Транслирует декларативные [LyteNavOptions] в `NavOptionsBuilder`. Вызывается шеллом (`App()`) при
 * применении [com.nikolaevskii.lyte.core.navigation.model.NavCommand.Forward], чтобы VM не зависела
 * от androidx.navigation-типов.
 */
fun NavOptionsBuilder.applyOptions(options: LyteNavOptions) {
    options.popUpTo?.let { route ->
        popUpTo(route) {
            inclusive = options.popUpToInclusive
            saveState = options.saveState
        }
    }
    if (options.launchSingleTop) {
        singleTop()
    }
    if (options.restoreState) {
        restorable()
    }
}
