package com.nikolaevskii.lyte.core.db.workout

/** Лёгкая проекция тренировки для списка — без графа упражнений/подходов, только их количество. */
data class WorkoutItemWithExerciseCount(
    val id: String,
    val name: String,
    val description: String?,
    val accent: String,
    val glyph: String,
    val exerciseCount: Int,
)
