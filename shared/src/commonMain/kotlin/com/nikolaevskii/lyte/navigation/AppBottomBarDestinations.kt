package com.nikolaevskii.lyte.navigation

import kotlinx.serialization.Serializable

// Граф-контейнер вкладок: общий родитель всех табов. Его старт (TrackerTabGraph) — постоянная база
// стека, на которую опирается saveState/restoreState при переключении вкладок.
//
// Маршруты самих вкладок живут в `:feature:<name>:api` (TrackerTabGraph, WorkoutTabGraph,
// HistoryTabGraph): так фича может переключиться на чужую вкладку через LyteNavigator.switchTab(),
// не завися от шелла.
@Serializable
data object BottomNavGraph
