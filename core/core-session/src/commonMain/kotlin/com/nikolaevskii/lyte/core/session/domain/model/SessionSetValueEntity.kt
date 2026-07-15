package com.nikolaevskii.lyte.core.session.domain.model

/**
 * Значение подхода «повторения × вес» — общее и для плана (цели), и для факта.
 * [weight] `null` — упражнение со своим весом (bodyweight), как в `WorkoutRepEntity`.
 */
data class SessionSetValueEntity(
    val count: Int,
    val weight: Double?,
)
