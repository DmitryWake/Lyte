package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutPreviewUiMapperTest {

    @Test
    fun mapsNameNumberingAndCounts() {
        val model = workout(
            name = "Push Day",
            exercises = listOf(
                exercise(name = "Жим лёжа", reps = listOf(rep(8, 80.0), rep(8, 80.0))),
                exercise(name = "Отжимания на брусьях", reps = listOf(rep(12, null))),
            ),
        ).toPreviewUiModel()

        assertEquals("Push Day", model.programName)
        assertEquals(2, model.exerciseCount)
        assertEquals(3, model.setCount)
        assertEquals(listOf(1, 2), model.exercises.map { it.number })
        assertEquals(listOf("Жим лёжа", "Отжимания на брусьях"), model.exercises.map { it.name })
    }

    @Test
    fun mapsExerciseMarkToDesignSystemValues() {
        val exercises = workout(
            exercises = listOf(
                exercise(
                    name = "Жим лёжа",
                    accent = ExerciseAccent.Indigo,
                    glyph = ExerciseGlyph.BenchPress,
                    reps = listOf(rep(8, 80.0)),
                ),
                // Упражнение без выбранного маркера: дефолт домена доезжает как дефолт дизайн-системы.
                exercise(name = "Растяжка", reps = listOf(rep(1, null))),
            ),
        ).toPreviewUiModel().exercises

        assertEquals(listOf(LyteAccent.Indigo, LyteAccent.Slate), exercises.map { it.accent })
        assertEquals(listOf(LyteExerciseGlyph.BenchPress, LyteExerciseGlyph.Squat), exercises.map { it.glyph })
    }

    @Test
    fun formatsWeightWithoutTrailingZero() {
        val sets = workout(exercises = listOf(exercise(name = "Жим", reps = listOf(rep(8, 60.0), rep(8, 62.5)))))
            .toPreviewUiModel()
            .exercises
            .single()
            .sets

        assertEquals(
            listOf(
                WorkoutPreviewSetUiModel.Weighted(reps = 8, weight = "60"),
                WorkoutPreviewSetUiModel.Weighted(reps = 8, weight = "62.5"),
            ),
            sets,
        )
    }

    @Test
    fun treatsNullAndZeroWeightAsBodyweight() {
        val sets = workout(exercises = listOf(exercise(name = "Брусья", reps = listOf(rep(12, null), rep(10, 0.0)))))
            .toPreviewUiModel()
            .exercises
            .single()
            .sets

        assertEquals(
            listOf(
                WorkoutPreviewSetUiModel.Bodyweight(reps = 12),
                WorkoutPreviewSetUiModel.Bodyweight(reps = 10),
            ),
            sets,
        )
    }

    private fun workout(name: String = "Program", exercises: List<WorkoutExerciseWithRepsEntity>): WorkoutEntity =
        WorkoutEntity(id = "w1", name = name, description = null, exercises = exercises)

    private fun exercise(
        name: String,
        reps: List<WorkoutRepEntity>,
        accent: ExerciseAccent = ExerciseAccent.Default,
        glyph: ExerciseGlyph = ExerciseGlyph.Default,
    ): WorkoutExerciseWithRepsEntity =
        WorkoutExerciseWithRepsEntity(
            exercise = WorkoutExerciseEntity(id = name, name = name, accent = accent, glyph = glyph),
            reps = reps,
        )

    private fun rep(count: Int, weight: Double?): WorkoutRepEntity = WorkoutRepEntity(count = count, weight = weight)
}
