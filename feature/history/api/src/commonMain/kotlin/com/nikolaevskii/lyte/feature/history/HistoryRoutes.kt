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
