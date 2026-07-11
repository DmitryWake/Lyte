package com.nikolaevskii.lyte.feature.tracker.presentation.model

/** Упражнение в превью программы: порядковый номер, название и плановые подходы. */
data class WorkoutPreviewExerciseUiModel(
    val number: Int,
    val name: String,
    val sets: List<WorkoutPreviewSetUiModel>,
)
