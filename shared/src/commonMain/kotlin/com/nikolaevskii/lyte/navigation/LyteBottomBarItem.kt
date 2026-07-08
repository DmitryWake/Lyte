package com.nikolaevskii.lyte.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.navigation.model.TopLevelDestination
import lyte.shared.generated.resources.Res
import lyte.shared.generated.resources.tab_history
import lyte.shared.generated.resources.tab_tracker
import lyte.shared.generated.resources.tab_workouts
import org.jetbrains.compose.resources.StringResource

/**
 * Верхнеуровневые разделы bottom-bar. Реализуют [TopLevelDestination] (маршрут графа вкладки) и
 * несут метаданные для отрисовки нижней навигации.
 */
enum class LyteBottomBarItem(
    override val graphRoute: Any,
    val icon: ImageVector,
    val label: StringResource,
) : TopLevelDestination {
    TRACKER(graphRoute = TrackerTabGraph, icon = LyteIcons.ListChecks, label = Res.string.tab_tracker),
    WORKOUTS(graphRoute = WorkoutTabGraph, icon = LyteIcons.Dumbbell, label = Res.string.tab_workouts),
    HISTORY(graphRoute = HistoryTabGraph, icon = LyteIcons.History, label = Res.string.tab_history),
}
