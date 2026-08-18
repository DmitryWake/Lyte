package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetState
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.completed
import com.nikolaevskii.lyte.feature.tracker.sessionExercise
import com.nikolaevskii.lyte.feature.tracker.sessionSet
import com.nikolaevskii.lyte.feature.tracker.workoutSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ActiveSessionTrackSetMapperTest {

    @Test
    fun currentSetCarriesDraftsAndRestingSetsCarryTones() {
        val current = requireNotNull(
            workoutSession(
                exercises = listOf(
                    sessionExercise(
                        id = "e1",
                        name = "Жим",
                        sets = listOf(
                            sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0, result = completed(count = 9, weight = 80.0)),
                            sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0, result = SessionSetResultEntity.Skipped),
                            sessionSet(id = "s3", targetCount = 8, targetWeight = 80.0),
                            sessionSet(id = "s4", targetCount = 8, targetWeight = 80.0),
                        ),
                    ),
                ),
            ).toActiveSessionUiModel().current,
        )

        val states = current.toTrackSetStates(draftReps = 9, draftWeight = 82.5)

        assertEquals(
            LyteTrackSetState.Resting(tone = LyteProgressTone.Positive, value = LyteSetValue(reps = 9, weight = 80.0)),
            states[0],
        )
        assertEquals(LyteTrackSetState.Resting(tone = LyteProgressTone.Skipped), states[1])
        assertEquals(
            LyteTrackSetState.Current(total = 4, reps = 9, weight = 82.5, target = LyteSetValue(reps = 8, weight = 80.0)),
            states[2],
        )
        assertEquals(
            LyteTrackSetState.Resting(tone = LyteProgressTone.Todo, value = LyteSetValue(reps = 8, weight = 80.0)),
            states[3],
        )
    }

    @Test
    fun bodyweightCurrentSetStillCarriesWeight() {
        val current = requireNotNull(
            workoutSession(
                exercises = listOf(
                    sessionExercise(
                        id = "e1",
                        name = "Подтягивания",
                        sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = null)),
                    ),
                ),
            ).toActiveSessionUiModel().current,
        )

        // Вес есть даже у цели «свой вес»: без него в карточке не было бы степпера, а значит и
        // способа отметить пояс.
        val state = current.toTrackSetStates(draftReps = 8, draftWeight = 10.0).single()

        assertEquals(
            LyteTrackSetState.Current(total = 1, reps = 8, weight = 10.0, target = LyteSetValue(reps = 8)),
            state,
        )
    }

    @Test
    fun lastSetLabelDependsOnPositionOfSetAndExercise() {
        val notLast = requireNotNull(
            workoutSession(
                exercises = listOf(
                    sessionExercise(
                        id = "e1",
                        name = "Жим",
                        sets = listOf(
                            sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0),
                            sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0),
                        ),
                    ),
                ),
            ).toActiveSessionUiModel().current,
        )
        val lastInExercise = requireNotNull(
            workoutSession(
                exercises = listOf(
                    sessionExercise(
                        id = "e1",
                        name = "Жим",
                        sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0)),
                    ),
                    sessionExercise(
                        id = "e2",
                        name = "Тяга",
                        sets = listOf(sessionSet(id = "s2", targetCount = 8, targetWeight = 80.0)),
                    ),
                ),
            ).toActiveSessionUiModel().current,
        )
        val lastInSession = requireNotNull(
            workoutSession(
                exercises = listOf(
                    sessionExercise(
                        id = "e1",
                        name = "Жим",
                        sets = listOf(sessionSet(id = "s1", targetCount = 8, targetWeight = 80.0)),
                    ),
                ),
            ).toActiveSessionUiModel().current,
        )

        assertNull(notLast.lastSetLabel())
        assertEquals(ActiveSessionLastSetLabel.LastInExercise, lastInExercise.lastSetLabel())
        assertEquals(ActiveSessionLastSetLabel.LastInSession, lastInSession.lastSetLabel())
    }
}
