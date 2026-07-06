
package com.nikolaevskii.lyte.core.navigation

import androidx.navigation.NavOptionsBuilder
import com.nikolaevskii.lyte.core.navigation.model.LyteNavOptions

inline fun <reified R : Any> NavOptionsBuilder.popUpToRoute(
    inclusive: Boolean = false,
    saveState: Boolean = false,
) {
    popUpTo<R> {
        this.inclusive = inclusive
        this.saveState = saveState
    }
}

fun NavOptionsBuilder.singleTop() {
    launchSingleTop = true
}

fun NavOptionsBuilder.restorable() {
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
