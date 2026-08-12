package com.nikolaevskii.lyte.core.workout.domain.model

/**
 * Программа тренировки. [accent]/[glyph] — собственный маркер программы, а не выведенный из первого
 * упражнения: программу узнают в списке по цвету и знаку, и они не должны меняться при правке состава.
 */
data class WorkoutEntity(
    val id: String,
    val name: String,
    val description: String?,
    val accent: ExerciseAccent = ExerciseAccent.Default,
    val glyph: ExerciseGlyph = ExerciseGlyph.Default,
    val exercises: List<WorkoutExerciseWithRepsEntity>
)
