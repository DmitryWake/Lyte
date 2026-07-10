package com.nikolaevskii.lyte.feature.tracker.domain.model

/**
 * Значение подхода «повторения × вес» — общее и для плана (цели), и для факта.
 * [weight] `null` — упражнение со своим весом (bodyweight), как в `WorkoutRepEntity`.
 */
data class SessionSetValueEntity(
    val count: Int,
    val weight: Double?,
)
