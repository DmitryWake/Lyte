package com.nikolaevskii.lyte.feature.history.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nikolaevskii.lyte.feature.history.HistoryRoute
import com.nikolaevskii.lyte.feature.history.HistorySessionDetailsRoute
import com.nikolaevskii.lyte.feature.history.presentation.screen.HistorySessionDetailsScreen
import com.nikolaevskii.lyte.feature.history.presentation.screen.HistoryScreen

fun NavGraphBuilder.historyGraph() {
    composable<HistoryRoute> {
        HistoryScreen()
    }
    // Детали сессии (5.2) — экран вкладки «История»: и тап по сессии в списке, и трекер после финиша
    // (тот переключается на вкладку и кладёт детали поверх списка) возвращают «назад» в список.
    // Не стартовый экран графа вкладки, поэтому нижний док на нём скрыт (см. isTopLevelSelected).
    composable<HistorySessionDetailsRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<HistorySessionDetailsRoute>()
        HistorySessionDetailsScreen(sessionId = args.sessionId)
    }
}
