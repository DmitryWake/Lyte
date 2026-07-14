package com.nikolaevskii.lyte.feature.history.presentation.model

/** Упражнение в деталях сессии (5.2): заголовок-имя и строки диффа по его подходам. */
data class HistoryExerciseGroupUiModel(
    val exerciseId: String,
    val exerciseName: String,
    val rows: List<HistoryDiffRowUiModel>,
)
