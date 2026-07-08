package com.nikolaevskii.lyte.feature.workout.data.repository

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WorkoutExerciseRepositoryImplTest {

    @Test
    fun createThenGetReturnsExercise() = runTest {
        val repository = repository()
        val exercise = WorkoutExerciseEntity(id = "ex-1", name = "Жим", description = "Грудь")

        repository.createExercise(exercise)

        assertEquals(exercise, repository.getExercise("ex-1"))
    }

    @Test
    fun getExercisesReturnsAll() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))
        repository.createExercise(WorkoutExerciseEntity(id = "ex-2", name = "Тяга"))

        assertEquals(2, repository.getExercises().size)
    }

    @Test
    fun editExercisesUpdatesInPlace() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))

        repository.editExercises(
            WorkoutExerciseEntity(id = "ex-1", name = "Жим лёжа", description = "Грудь"),
        )

        val loaded = repository.getExercise("ex-1")
        assertEquals("Жим лёжа", loaded?.name)
        assertEquals("Грудь", loaded?.description)
        assertEquals(1, repository.getExercises().size)
    }

    @Test
    fun deleteExerciseRemovesIt() = runTest {
        val repository = repository()
        repository.createExercise(WorkoutExerciseEntity(id = "ex-1", name = "Жим"))

        repository.deleteExercise("ex-1")

        assertNull(repository.getExercise("ex-1"))
    }

    private fun repository(): WorkoutExerciseRepositoryImpl =
        WorkoutExerciseRepositoryImpl(exerciseDao = FakeExerciseDao())
}
