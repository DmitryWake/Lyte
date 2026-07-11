package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Готовая к отрисовке модель превью программы (спека 4.2): имя, посчитанные счётчики и список
 * упражнений. Все преобразования домена делает маппер `toPreviewUiModel` — Compose только рендерит.
 *
 * Локализованные строки (единицы, плюрализация) в модель не кладём: их подставляет UI-слой через
 * ресурсы, поэтому смена языка на лету по-прежнему перерисовывает экран.
 */
data class WorkoutPreviewUiModel(
    val programName: String,
    val exerciseCount: Int,
    val setCount: Int,
    val exercises: List<WorkoutPreviewExerciseUiModel>,
)
