package com.nikolaevskii.lyte.core.session.domain

import com.nikolaevskii.lyte.core.session.completed
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.sessionExercise
import com.nikolaevskii.lyte.core.session.sessionSet
import com.nikolaevskii.lyte.core.session.workoutSession
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionPlanProgressionTest {

    @Test
    fun completedSetBecomesNewTarget() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 12, weight = 62.5)),
                    ),
                ),
            ),
        )

        val progressed = session.applyProgressionTo(plan(reps = listOf(WorkoutRepEntity(count = 10, weight = 60.0))))

        assertEquals(listOf(WorkoutRepEntity(count = 12, weight = 62.5)), progressed.exercises.single().reps)
    }

    @Test
    fun missedSetAlsoBecomesNewTarget() {
        // Прогрессия «топорная»: план идёт за фактом и вниз тоже.
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 6, weight = 55.0)),
                    ),
                ),
            ),
        )

        val progressed = session.applyProgressionTo(plan(reps = listOf(WorkoutRepEntity(count = 10, weight = 60.0))))

        assertEquals(listOf(WorkoutRepEntity(count = 6, weight = 55.0)), progressed.exercises.single().reps)
    }

    @Test
    fun skippedAndPendingSetsKeepTarget() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = SessionSetResultEntity.Skipped),
                        sessionSet(id = "s2", targetCount = 8, targetWeight = 65.0),
                        sessionSet(id = "s3", targetCount = 6, targetWeight = 70.0, result = completed(count = 7, weight = 72.5)),
                    ),
                ),
            ),
        )
        val original = plan(
            reps = listOf(
                WorkoutRepEntity(count = 10, weight = 60.0),
                WorkoutRepEntity(count = 8, weight = 65.0),
                WorkoutRepEntity(count = 6, weight = 70.0),
            ),
        )

        val progressed = session.applyProgressionTo(original)

        assertEquals(
            listOf(
                WorkoutRepEntity(count = 10, weight = 60.0),
                WorkoutRepEntity(count = 8, weight = 65.0),
                WorkoutRepEntity(count = 7, weight = 72.5),
            ),
            progressed.exercises.single().reps,
        )
    }

    @Test
    fun bodyweightSetKeepsNullWeight() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Подтягивания",
                    sets = listOf(
                        sessionSet(id = "s1", targetCount = 8, targetWeight = null, result = completed(count = 11, weight = null)),
                    ),
                ),
            ),
        )

        val progressed = session.applyProgressionTo(plan(reps = listOf(WorkoutRepEntity(count = 8, weight = null))))

        assertEquals(listOf(WorkoutRepEntity(count = 11, weight = null)), progressed.exercises.single().reps)
    }

    @Test
    fun exerciseReplacedInPlanKeepsItsTargets() {
        // Программу отредактировали после старта: на позиции 0 теперь другое упражнение — не трогаем.
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 12, weight = 62.5))),
                ),
            ),
        )
        val original = plan(exerciseId = "lib-other", reps = listOf(WorkoutRepEntity(count = 10, weight = 60.0)))

        assertEquals(original, session.applyProgressionTo(original))
    }

    @Test
    fun planWithExtraSetsKeepsUnmatchedTargets() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 12, weight = 62.5))),
                ),
            ),
        )
        val original = plan(
            reps = listOf(
                WorkoutRepEntity(count = 10, weight = 60.0),
                WorkoutRepEntity(count = 8, weight = 65.0),
            ),
        )

        val progressed = session.applyProgressionTo(original)

        assertEquals(
            listOf(
                WorkoutRepEntity(count = 12, weight = 62.5),
                WorkoutRepEntity(count = 8, weight = 65.0),
            ),
            progressed.exercises.single().reps,
        )
    }

    @Test
    fun sessionWithExtraExercisesDoesNotGrowPlan() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 12, weight = 62.5))),
                ),
                sessionExercise(
                    id = "e2",
                    name = "Тяга",
                    sets = listOf(sessionSet(id = "s2", targetCount = 12, targetWeight = 50.0, result = completed(count = 14, weight = 52.5))),
                ),
            ),
        )

        val progressed = session.applyProgressionTo(plan(reps = listOf(WorkoutRepEntity(count = 10, weight = 60.0))))

        assertEquals(1, progressed.exercises.size)
        assertEquals(listOf(WorkoutRepEntity(count = 12, weight = 62.5)), progressed.exercises.single().reps)
    }

    @Test
    fun foreignWorkoutIsNotTouched() {
        val session = workoutSession(
            exercises = listOf(
                sessionExercise(
                    id = "e1",
                    name = "Жим",
                    sets = listOf(sessionSet(id = "s1", targetCount = 10, targetWeight = 60.0, result = completed(count = 12, weight = 62.5))),
                ),
            ),
        )
        val original = plan(workoutId = "prog-other", reps = listOf(WorkoutRepEntity(count = 10, weight = 60.0)))

        assertEquals(original, session.applyProgressionTo(original))
    }

    /** Программа из одного упражнения; id упражнения совпадает с тем, что даёт `sessionExercise("e1")`. */
    private fun plan(
        workoutId: String = "prog-1",
        exerciseId: String = "lib-e1",
        reps: List<WorkoutRepEntity>,
    ): WorkoutEntity = WorkoutEntity(
        id = workoutId,
        name = "Push Day",
        description = null,
        exercises = listOf(
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = exerciseId, name = "Жим"),
                reps = reps,
            ),
        ),
    )
}
