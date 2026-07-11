package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutRepEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutPreviewUiMapperTest {

    @Test
    fun mapsNameNumberingAndCounts() {
        val model = workout(
            name = "Push Day",
            exercises = listOf(
                exercise("Жим лёжа", rep(8, 80.0), rep(8, 80.0)),
                exercise("Отжимания на брусьях", rep(12, null)),
            ),
        ).toPreviewUiModel()

        assertEquals("Push Day", model.programName)
        assertEquals(2, model.exerciseCount)
        assertEquals(3, model.setCount)
        assertEquals(listOf(1, 2), model.exercises.map { it.number })
        assertEquals(listOf("Жим лёжа", "Отжимания на брусьях"), model.exercises.map { it.name })
    }

    @Test
    fun formatsWeightWithoutTrailingZero() {
        val sets = workout(exercises = listOf(exercise("Жим", rep(8, 60.0), rep(8, 62.5))))
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
        val sets = workout(exercises = listOf(exercise("Брусья", rep(12, null), rep(10, 0.0))))
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

    private fun exercise(name: String, vararg reps: WorkoutRepEntity): WorkoutExerciseWithRepsEntity =
        WorkoutExerciseWithRepsEntity(
            exercise = WorkoutExerciseEntity(id = name, name = name),
            reps = reps.toList(),
        )

    private fun rep(count: Int, weight: Double?): WorkoutRepEntity = WorkoutRepEntity(count = count, weight = weight)
}
