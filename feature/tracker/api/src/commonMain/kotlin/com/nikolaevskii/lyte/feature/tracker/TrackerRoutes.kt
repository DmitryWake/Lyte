package com.nikolaevskii.lyte.feature.tracker

import kotlinx.serialization.Serializable

/**
 * Граф вкладки «Трекер» — собственный back stack вкладки. Живёт в `:api`, чтобы шелл (`:shared`) и
 * другие фичи могли переключаться на вкладку через `LyteNavigator.switchTab(TrackerTabGraph)`.
 */
@Serializable
data object TrackerTabGraph

/** Корень вкладки: экран «нет активной сессии» с переходом к выбору программы. */
@Serializable
data object TrackerLandingRoute

/** Выбор программы для тренировки. Не стартовый экран вкладки, поэтому bottom-bar на нём скрыт. */
@Serializable
data object WorkoutPickerRoute
