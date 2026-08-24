package com.nikolaevskii.lyte.feature.tracker

import kotlinx.serialization.Serializable

/**
 * Граф вкладки «Трекер» — собственный back stack вкладки. Живёт в `:api`, чтобы шелл (`:shared`) и
 * другие фичи могли переключаться на вкладку через `LyteNavigator.switchTab(TrackerTabGraph)`.
 */
@Serializable
data object TrackerTabGraph

/** Корень вкладки: экран «нет активной сессии»; выбор программы открывается на нём шторкой. */
@Serializable
data object TrackerLandingRoute

/**
 * Превью выбранной программы перед стартом сессии (спека 4.2): состав и план подходов + кнопка
 * «Начать тренировку». Не стартовый экран вкладки, поэтому bottom-bar на нём скрыт. Экран сам грузит
 * программу по [programId] — доменные модели в аргументы роута не кладём.
 */
@Serializable
data class WorkoutPreviewRoute(val programId: String)

/**
 * Активная сессия тренировки (спека 4.3). Экран сам грузит сессию по [sessionId]; после смерти
 * процесса Navigation восстанавливает маршрут с аргументом, и состояние сессии поднимается из БД.
 * Не стартовый экран вкладки, поэтому bottom-bar на нём скрыт — во время сессии приложение
 * сфокусировано на ней. В стек вкладки экран ставится единственным (popUpTo лендинга включительно):
 * выйти из сессии можно только завершив её.
 */
@Serializable
data class ActiveSessionRoute(val sessionId: String)
