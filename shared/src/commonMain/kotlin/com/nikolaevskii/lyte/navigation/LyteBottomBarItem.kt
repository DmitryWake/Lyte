package com.nikolaevskii.lyte.navigation

import com.nikolaevskii.lyte.core.navigation.model.TopLevelDestination
import lyte.shared.generated.resources.Res
import lyte.shared.generated.resources.ic_tab_history
import lyte.shared.generated.resources.ic_tab_tracker
import lyte.shared.generated.resources.ic_tab_workout
import lyte.shared.generated.resources.tab_history
import lyte.shared.generated.resources.tab_tracker
import lyte.shared.generated.resources.tab_workouts
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Верхнеуровневые разделы bottom-bar. Реализуют [TopLevelDestination] (маршрут графа вкладки) и
 * несут метаданные для отрисовки нижней навигации.
 */
enum class LyteBottomBarItem(
    override val graphRoute: Any,
    val icon: DrawableResource,
    val label: StringResource,
) : TopLevelDestination {
    TRACKER(graphRoute = TrackerTabGraph, icon = Res.drawable.ic_tab_tracker, label = Res.string.tab_tracker),
    WORKOUTS(graphRoute = WorkoutTabGraph, icon = Res.drawable.ic_tab_workout, label = Res.string.tab_workouts),
    HISTORY(graphRoute = HistoryTabGraph, icon = Res.drawable.ic_tab_history, label = Res.string.tab_history),
}
