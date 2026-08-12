package com.nikolaevskii.lyte.core.workout.domain.model

/**
 * Упражнение библиотеки. [accent]/[glyph] — маркер: цвет и знак движения, выбранные пользователем
 * при создании упражнения. Дефолты означают «маркер не выбирали», а не «маркера нет»: упражнение
 * без выбора всё равно рисуется осознанным сланцевым приседом.
 */
data class WorkoutExerciseEntity(
    val id: String,
    val name: String,
    val description: String? = null,
    val accent: ExerciseAccent = ExerciseAccent.Default,
    val glyph: ExerciseGlyph = ExerciseGlyph.Default,
)
