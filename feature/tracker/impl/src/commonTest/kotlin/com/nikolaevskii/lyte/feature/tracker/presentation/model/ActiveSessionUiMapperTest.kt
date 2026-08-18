package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.feature.tracker.completed
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActiveSessionUiMapperTest {

    @Test
    fun mapsCurrentExerciseAndSetPositions() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим лёжа",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 8, targetWeight = 70.0, result = completed(count = 8, weight = 70.0)),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0),
                        sessionSet(id = "s3", targetCount = 6, targetWeight = 85.0),
                    ),
                ),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s4", targetCount = 10, targetWeight = 60.0))),
            ),
        )

        val current = requireNotNull(session.toActiveSessionUiModel().current)

        assertEquals("e1", current.exerciseId)
        assertEquals(1, current.exerciseIndex)
        assertEquals(2, current.exerciseCount)
        assertEquals("Жим лёжа", current.exerciseName)
        assertEquals("s2", current.currentSetId)
        assertEquals(3, current.setCount)
        assertEquals(1, current.currentSetIndex)
        assertEquals(8, current.targetReps)
        assertEquals(80.0, current.targetWeight)
    }

    @Test
    fun setStatusesReflectOutcomeAndCurrent() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 8, weight = 80.0)),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0, result = completed(count = 9, weight = 80.0)),
                        sessionSet(id = "s3", targetCount = 8, targetWeight = 80.0, result = completed(count = 6, weight = 80.0)),
                        sessionSet(id = "s4", targetCount = 8, targetWeight = 80.0, result = SessionSetResultEntity.Skipped),
                        sessionSet(id = "s5", targetCount = 8, targetWeight = 80.0),
                        sessionSet(id = "s6", targetCount = 8, targetWeight = 80.0),
                    ),
                ),
            ),
        )

        val sets = requireNotNull(session.toActiveSessionUiModel().current).sets

        assertEquals(
            listOf(
                ActiveSessionSetStatus.Hit,
                ActiveSessionSetStatus.Exceeded,
                ActiveSessionSetStatus.Missed,
                ActiveSessionSetStatus.Skipped,
                ActiveSessionSetStatus.Current,
                ActiveSessionSetStatus.Todo,
            ),
            sets.map { set -> set.status },
        )
    }

    @Test
    fun setValueUsesActualForDoneTargetForPendingNullForSkipped() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 9, weight = 82.5)),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0, result = SessionSetResultEntity.Skipped),
                        sessionSet(id = "s3", targetCount = 6, targetWeight = 85.0),
                    ),
                ),
            ),
        )

        val sets = requireNotNull(session.toActiveSessionUiModel().current).sets

        // Выполненный — факт (9×82.5), пропущенный — прочерк (null), текущий — цель (6×85).
        assertEquals(LyteSetValue(reps = 9, weight = 82.5), sets[0].value)
        assertNull(sets[1].value)
        assertEquals(LyteSetValue(reps = 6, weight = 85.0), sets[2].value)
    }

    @Test
    fun setNoteReachesRow() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(
                            id = "s1",
                            targetCount = 8,
                            targetWeight = 80.0,
                            result = completed(count = 8, weight = 80.0),
                            note = "Пояс затянул туго",
                        ),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0),
                    ),
                ),
            ),
        )

        val sets = requireNotNull(session.toActiveSessionUiModel().current).sets

        assertEquals("Пояс затянул туго", sets[0].note)
        assertEquals("", sets[1].note)
    }

    @Test
    fun completedCountExcludesSkipped() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 8, weight = 80.0)),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0, result = SessionSetResultEntity.Skipped),
                        sessionSet(id = "s3", targetCount = 8, targetWeight = 80.0, result = completed(count = 8, weight = 80.0)),
                    ),
                ),
            ),
        )

        val model = session.toActiveSessionUiModel()

        assertEquals(2, model.completedCount)
        assertEquals(3, model.totalCount)
    }

    @Test
    fun bodyweightTargetHasNullWeightAndBodyweightValue() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Брусья",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 12, targetWeight = null),
                        sessionSet(id = "s2", targetCount = 12, targetWeight = 0.0),
                    ),
                ),
            ),
        )

        val current = requireNotNull(session.toActiveSessionUiModel().current)

        assertNull(current.targetWeight)
        assertEquals(LyteSetValue(reps = 12), current.target)
        // Вес-0 тоже bodyweight: второй подход без веса в строке.
        assertEquals(LyteSetValue(reps = 12, weight = null), current.sets[1].value)
    }

    @Test
    fun allResolvedProducesNoCurrent() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 8, weight = 80.0))),
                ),
            ),
        )

        val model = session.toActiveSessionUiModel()

        assertNull(model.current)
        assertEquals(1, model.completedCount)
    }

    @Test
    fun switcherRowsCarryStatusSubtitlesPillsAndSelectability() {
        val session = workoutSession(
            currentExerciseId = "e2",
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 8, weight = 80.0)),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0, result = SessionSetResultEntity.Skipped),
                    ),
                ),
                sessionExercise(
                    id = "e2",
                    name = "Тяга",
                    sets = listOf(
                        sessionSet(id = "s3", targetCount = 10, targetWeight = 60.0, result = completed(count = 10, weight = 60.0)),
                        sessionSet(id = "s4", targetCount = 10, targetWeight = 60.0),
                    ),
                ),
                sessionExercise(
                    id = "e3",
                    name = "Брусья",
                    sets = listOf(sessionSet(id = "s5", targetCount = 12, targetWeight = null)),
                ),
            ),
        )

        val rows = session.toActiveSessionUiModel().switcherRows

        val done = rows[0]
        assertEquals(ActiveSessionSwitcherStatus.Done, done.status)
        assertEquals(2, done.doneCount)
        assertTrue(done.targetPills.isEmpty())
        assertEquals(false, done.isSelectable)

        val current = rows[1]
        assertEquals(ActiveSessionSwitcherStatus.Current, current.status)
        assertEquals(2, current.currentSetIndex)
        assertEquals(true, current.isSelectable)

        val pending = rows[2]
        assertEquals(ActiveSessionSwitcherStatus.Pending, pending.status)
        assertEquals(listOf(LyteSetValue(reps = 12)), pending.targetPills)
        assertEquals(true, pending.isSelectable)
    }

    @Test
    fun exerciseWithoutSetsIsDoneAndNotSelectable() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(id = "e1", name = "Пустое", sets = emptyList()),
                sessionExercise(id = "e2", name = "Тяга", sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0))),
            ),
        )

        val rows = session.toActiveSessionUiModel().switcherRows

        assertEquals(ActiveSessionSwitcherStatus.Done, rows[0].status)
        assertEquals(false, rows[0].isSelectable)
    }
}
