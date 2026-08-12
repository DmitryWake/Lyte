package com.nikolaevskii.lyte.core.workout.domain.model

/** Лёгкая проекция программы для списка: без графа упражнений, но с маркером и их количеством. */
data class WorkoutItemEntity(
    val id: String,
    val name: String,
    val description: String?,
    val accent: ExerciseAccent = ExerciseAccent.Default,
    val glyph: ExerciseGlyph = ExerciseGlyph.Default,
    val exerciseCount: Int,
)
