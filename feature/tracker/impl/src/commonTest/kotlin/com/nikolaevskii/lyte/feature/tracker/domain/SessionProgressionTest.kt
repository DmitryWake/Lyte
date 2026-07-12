package com.nikolaevskii.lyte.feature.tracker.domain

import com.nikolaevskii.lyte.feature.tracker.completed
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SessionProgressionTest {

    @Test
    fun currentSetIsFirstWithoutResult() {
        val exercise = sessionExercise(
            id = "e1",
            name = "Жим",
            sets = listOf(
                sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0)),
                sessionSet(id = "s2", targetCount = 10, targetWeight = 60.0),
                sessionSet(id = "s3", targetCount = 8, targetWeight = 62.5),
            ),
        )

        assertEquals("s2", exercise.currentSet()?.id)
    }

    @Test
    fun currentSetNullWhenAllResolved() {
        val exercise = sessionExercise(
            id = "e1",
            name = "Жим",
            sets = listOf(
                sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = SessionSetResultEntity.Skipped),
                sessionSet(id = "s2", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0)),
            ),
        )

        assertNull(exercise.currentSet())
    }

    @Test
    fun effectiveCurrentPrefersManualSelectionWhilePending() {
        val session = workoutSession(
            currentExerciseId = "e2",
            exercises = listOf(
                sessionExercise(id = "e1", name = "Жим", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 10, targetWeight = 50.0))),
            ),
        )

        assertEquals("e2", session.effectiveCurrentExercise()?.id)
    }

    @Test
    fun effectiveCurrentFallsBackWhenSelectedExerciseDone() {
        // Выбранное упражнение закрыто — берём первое по порядку с незакрытыми подходами.
        val session = workoutSession(
            currentExerciseId = "e1",
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0))),
                ),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 10, targetWeight = 50.0))),
            ),
        )

        assertEquals("e2", session.effectiveCurrentExercise()?.id)
    }

    @Test
    fun effectiveCurrentUsesListOrderWhenNothingSelected() {
        val session = workoutSession(
            currentExerciseId = null,
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = SessionSetResultEntity.Skipped)),
                ),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 10, targetWeight = 50.0))),
                sessionExercise(id = "e3", name = "Присед", sets = listOf(sessionSet(id = "s3", targetCount = 8, targetWeight = 80.0))),
            ),
        )

        assertEquals("e2", session.effectiveCurrentExercise()?.id)
    }

    @Test
    fun effectiveCurrentNullWhenAllResolved() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0))),
                ),
            ),
        )

        assertNull(session.effectiveCurrentExercise())
    }

    @Test
    fun exerciseWithoutSetsIsNotSelectedAsCurrent() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(id = "e1", name = "Пустое", sets = emptyList()),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s2", targetCount = 10, targetWeight = 50.0))),
            ),
        )

        assertEquals("e2", session.effectiveCurrentExercise()?.id)
    }
}
