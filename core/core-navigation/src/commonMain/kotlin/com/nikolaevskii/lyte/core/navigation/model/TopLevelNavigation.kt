package com.nikolaevskii.lyte.core.navigation.model

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Контракт верхнеуровневого раздела bottom-bar (вкладки).
 *
 * [graphRoute] — `@Serializable`-маршрут **вложенного графа** вкладки (а не отдельного экрана):
 * каждая вкладка живёт в собственном `navigation<TabGraphRoute>(startDestination = …)`, чтобы у неё
 * был свой back stack. Сам маршрут лежит в `:feature:<name>:api`.
 */
interface TopLevelDestination {
    val graphRoute: Any
}

/**
 * Канонический переход на вкладку bottom-bar с сохранением/восстановлением её back stack.
 *
 * - `popUpTo(<старт графа-контейнера вкладок>){ saveState = true }` — сворачивает текущую вкладку,
 *   сохраняя её стек;
 * - `launchSingleTop` — не плодит дубликат корня вкладки при повторном тапе;
 * - `restoreState` — восстанавливает ранее сохранённый стек целевой вкладки.
 *
 * [graphRoute] — маршрут **графа** вкладки. Передать сюда экран внутри вкладки нельзя: `navigate` тогда
 * положит целевую вкладку поверх текущей (Navigation достроит недостающий entry её графа), а следующий
 * `popUpTo(saveState) + restoreState` привяжет сохранённый стек к графу вкладки-источника и будет
 * восстанавливать его вместо переключения — вкладка-источник станет недостижимой.
 *
 * Вызывать только из агрегатора (`App()` / шелл), где живёт [NavController].
 */
fun NavController.navigateToTopLevel(graphRoute: Any) {
    navigate(graphRoute) {
        popUpTo(tabsHostStartDestinationId()) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/** Перегрузка [navigateToTopLevel] для вкладки, описанной [TopLevelDestination] (bottom-bar). */
fun NavController.navigateToTopLevel(destination: TopLevelDestination) {
    navigateToTopLevel(destination.graphRoute)
}

/**
 * Id стартового назначения графа-контейнера вкладок — постоянной базы стека в табах. На неё
 * сворачиваемся с `saveState`, сохраняя стек уходящей вкладки.
 *
 * Контейнер вкладок — родитель графа текущей вкладки (ближайшего графа-предка экрана). Если такого
 * предка нет (сама вкладка — корень навигации), используем корневой граф как базу.
 */
private fun NavController.tabsHostStartDestinationId(): Int {
    val currentTabGraph = currentBackStackEntry?.destination?.hierarchy
        ?.firstOrNull { it is NavGraph } as? NavGraph
    val tabsHost = currentTabGraph?.parent ?: graph
    return tabsHost.findStartDestination().id
}

/**
 * Выбрана ли вкладка [destination] — `true` только на её **стартовом** экране (списке), а не на
 * любом экране внутри графа вкладки: иначе вложенные detail-экраны (напр. редактор программы)
 * наследовали бы bottom-bar от корня вкладки, хотя сами его показывать не должны.
 */
fun NavDestination?.isTopLevelSelected(destination: TopLevelDestination): Boolean {
    val current = this ?: return false
    val tabGraph = current.hierarchy.firstOrNull { it.hasRoute(destination.graphRoute::class) } as? NavGraph ?: return false
    return current.id == tabGraph.findStartDestination().id
}
