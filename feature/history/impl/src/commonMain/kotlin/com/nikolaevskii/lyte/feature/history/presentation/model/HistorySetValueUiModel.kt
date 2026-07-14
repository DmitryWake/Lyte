package com.nikolaevskii.lyte.feature.history.presentation.model

/**
 * Значение подхода «повторения (× вес)» для строки диффа 5.2. [Weighted.weight] — уже отформатированное
 * число (без «.0»); единицы («повт»/«кг») добавляет UI-слой. [Bodyweight] — упражнение со своим весом.
 */
sealed interface HistorySetValueUiModel {

    data class Weighted(val reps: Int, val weight: String) : HistorySetValueUiModel

    data class Bodyweight(val reps: Int) : HistorySetValueUiModel
}
