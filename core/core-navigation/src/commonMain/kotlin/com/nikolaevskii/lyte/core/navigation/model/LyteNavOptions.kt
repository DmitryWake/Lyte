package com.nikolaevskii.lyte.core.navigation.model

/**
 * Декларативные опции стек-шейпинга. Шелл транслирует их в `NavOptionsBuilder` (см. `applyOptions`),
 * поэтому VM остаётся свободной от androidx.navigation-типов и тестируется без `NavController`.
 *
 * [popUpTo] — `@Serializable`-роут, до которого сворачивается стек (как объект, не тип).
 */
data class LyteNavOptions(
    val popUpTo: Any? = null,
    val popUpToInclusive: Boolean = false,
    val saveState: Boolean = false,
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
)
