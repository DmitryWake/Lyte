package com.nikolaevskii.lyte.navigation

import kotlinx.serialization.Serializable

// Граф-контейнер вкладок: общий родитель всех табов. Его старт (TrackerTabGraph) — постоянная база
// стека, на которую опирается saveState/restoreState при переключении вкладок.
@Serializable
data object BottomNavGraph

// Маршруты вложенных графов вкладок. Каждая вкладка — собственный back stack
// (`navigation<TabGraph>(startDestination = …)` в LyteNavHost).
@Serializable
data object TrackerTabGraph

@Serializable
data object WorkoutTabGraph

@Serializable
data object HistoryTabGraph
