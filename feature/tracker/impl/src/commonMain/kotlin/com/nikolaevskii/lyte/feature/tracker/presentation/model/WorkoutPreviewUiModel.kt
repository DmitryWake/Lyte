package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Готовая к отрисовке модель превью программы (спека 4.2): имя, число упражнений и их список.
 * Все преобразования домена делает маппер `toPreviewUiModel` — Compose только рендерит.
 *
 * Счётчик один: в v2 шапка называет ровно один факт («5 упражнений»), а сколько в программе
 * подходов, видно по трекам плана на карточках.
 *
 * Локализованные строки (единицы, плюрализация) в модель не кладём: их подставляет UI-слой через
 * ресурсы, поэтому смена языка на лету по-прежнему перерисовывает экран.
 */
data class WorkoutPreviewUiModel(
    val programName: String,
    val exerciseCount: Int,
    val exercises: List<WorkoutPreviewExerciseUiModel>,
)
