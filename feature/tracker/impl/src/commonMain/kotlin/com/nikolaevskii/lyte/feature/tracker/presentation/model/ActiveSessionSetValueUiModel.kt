package com.nikolaevskii.lyte.feature.tracker.presentation.model

/**
 * Значение подхода «повторения × вес» для отрисовки. Вес уже отформатирован маппером («60», «62.5») —
 * локализованные единицы («кг», «повт») подставляет UI-слой через ресурсы, как в превью программы.
 */
sealed interface ActiveSessionSetValueUiModel {

    data class Weighted(val reps: Int, val weight: String) : ActiveSessionSetValueUiModel

    data class Bodyweight(val reps: Int) : ActiveSessionSetValueUiModel
}
