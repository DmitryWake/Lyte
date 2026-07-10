package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutRepEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutRepositoryImplTest {

    @Test
    fun createThenGetReturnsSameGraphInOrder() = runTest {
        val repository = repository()
        val workout = sampleWorkout()

        repository.createWorkout(workout)
        val loaded = repository.getWorkout(workout.id)

        // Fake-DAO отдаёт детей в обратном порядке — совпадение доказывает сортировку по position.
        assertEquals(workout, loaded)
    }

    @Test
    fun getWorkoutsReturnsLightweightItems() = runTest {
        val repository = repository()
        repository.createWorkout(sampleWorkout(id = "w1", name = "A"))
        repository.createWorkout(sampleWorkout(id = "w2", name = "B"))

        val items = repository.getWorkouts()

        assertEquals(2, items.size)
        assertEquals(setOf("w1", "w2"), items.map { it.id }.toSet())
    }

    @Test
    fun getWorkoutsReturnsExerciseCountPerWorkout() = runTest {
        val repository = repository()
        repository.createWorkout(sampleWorkout(id = "w1", name = "A"))

        val items = repository.getWorkouts()

        assertEquals(2, items.single { it.id == "w1" }.exerciseCount)
    }

    @Test
    fun editWorkoutReplacesExercises() = runTest {
        val repository = repository()
        val original = sampleWorkout()
        repository.createWorkout(original)

        val edited = original.copy(
            exercises = listOf(
                WorkoutExerciseWithRepsEntity(
                    exercise = WorkoutExerciseEntity(id = "ex-new", name = "Присед"),
                    reps = listOf(WorkoutRepEntity(count = 5, weight = 100.0)),
                ),
            ),
        )
        repository.editWorkout(edited)
        val loaded = repository.getWorkout(original.id)

        assertEquals(edited, loaded)
        assertEquals(1, loaded?.exercises?.size)
    }

    @Test
    fun deleteWorkoutWithoutSessionsRemovesIt() = runTest {
        val repository = repository()
        val workout = sampleWorkout()
        repository.createWorkout(workout)

        repository.deleteWorkout(workout.id)

        assertNull(repository.getWorkout(workout.id))
        assertTrue(repository.getWorkouts().isEmpty())
    }

    @Test
    fun deleteWorkoutWithSessionsArchivesItInsteadOfDeleting() = runTest {
        val dao = FakeWorkoutDao()
        val repository = WorkoutRepositoryImpl(workoutDao = dao)
        val workout = sampleWorkout()
        repository.createWorkout(workout)
        // На программу ссылается сессия трекера — физическое удаление запрещено.
        dao.sessionCountByWorkout[workout.id] = 1

        repository.deleteWorkout(workout.id)

        // Программа скрыта из списка, но остаётся доступной по id (история сохранена).
        assertTrue(repository.getWorkouts().none { it.id == workout.id })
        assertEquals(workout, repository.getWorkout(workout.id))
    }

    private fun repository(): WorkoutRepositoryImpl =
        WorkoutRepositoryImpl(workoutDao = FakeWorkoutDao())

    private fun sampleWorkout(
        id: String = "workout-1",
        name: String = "Верх тела",
    ): WorkoutEntity = WorkoutEntity(
        id = id,
        name = name,
        description = "Описание",
        exercises = listOf(
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = "ex-1", name = "Жим", description = null),
                reps = listOf(
                    WorkoutRepEntity(count = 10, weight = 40.0),
                    WorkoutRepEntity(count = 8, weight = 45.0),
                ),
            ),
            WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(id = "ex-2", name = "Тяга", description = "Спина"),
                reps = listOf(
                    WorkoutRepEntity(count = 12, weight = null),
                ),
            ),
        ),
    )
}
