package com.nikolaevskii.lyte.feature.history

import kotlinx.serialization.Serializable

/**
 * Граф вкладки «История» — собственный back stack вкладки. Живёт в `:api`, чтобы шелл (`:shared`) и
 * другие фичи могли переключаться на вкладку через `LyteNavigator.switchTab(HistoryTabGraph)`.
 */
@Serializable
data object HistoryTabGraph

@Serializable
data object HistoryRoute

/**
 * Экран деталей завершённой сессии (спека 5.2). Живёт в `:api`, чтобы на него могли навигироваться и
 * список Истории, и трекер сразу после завершения сессии. [sessionId] — id завершённой сессии.
 */
@Serializable
data class HistorySessionDetailsRoute(val sessionId: String)
