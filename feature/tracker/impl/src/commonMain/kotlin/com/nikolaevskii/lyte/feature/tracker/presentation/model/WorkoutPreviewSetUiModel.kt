package com.nikolaevskii.lyte.feature.tracker.presentation.model

/** Плановый подход в превью: с весом (число веса уже отформатировано — «60», «62.5») либо со своим весом. */
sealed interface WorkoutPreviewSetUiModel {

    val reps: Int

    data class Weighted(override val reps: Int, val weight: String) : WorkoutPreviewSetUiModel

    data class Bodyweight(override val reps: Int) : WorkoutPreviewSetUiModel
}
