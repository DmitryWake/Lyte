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

/**
 * Превью выбранной программы перед стартом сессии (спека 4.2): состав и план подходов + кнопка
 * «Начать тренировку». Не стартовый экран вкладки, поэтому bottom-bar на нём скрыт. Экран сам грузит
 * программу по [programId] — доменные модели в аргументы роута не кладём.
 */
@Serializable
data class WorkoutPreviewRoute(val programId: String)
