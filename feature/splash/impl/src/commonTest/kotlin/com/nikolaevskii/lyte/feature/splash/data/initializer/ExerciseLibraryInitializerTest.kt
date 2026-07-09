package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.feature.splash.data.seed.DefaultExerciseLibrary
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExerciseLibraryInitializerTest {

    @Test
    fun initializeSeedsAllExercisesOnFirstLaunch() = runTest {
        val exerciseRepository = FakeWorkoutExerciseRepository()
        val launchStateRepository = FakeAppLaunchStateRepository(hasCompletedFirstLaunch = false)
        val initializer = ExerciseLibraryInitializer(
            exerciseRepository = exerciseRepository,
            appLaunchStateRepository = launchStateRepository,
        )

        initializer.initialize()

        assertEquals(DefaultExerciseLibrary.exercises.toSet(), exerciseRepository.getExercises().toSet())
        assertTrue(launchStateRepository.hasCompletedFirstLaunch())
    }

    @Test
    fun initializeDoesNotReseedWhenFirstLaunchAlreadyCompletedEvenIfLibraryWasEmptied() = runTest {
        // Пользователь мог удалить все упражнения после первого запуска — это не первый запуск,
        // и таблица не должна быть заново засеяна из-за одной лишь пустоты.
        val exerciseRepository = FakeWorkoutExerciseRepository()
        val seeded = WorkoutExerciseEntity(id = "seeded", name = "Засеянное")
        exerciseRepository.createExercise(seeded)
        exerciseRepository.deleteExercise(seeded.id)
        val launchStateRepository = FakeAppLaunchStateRepository(hasCompletedFirstLaunch = true)
        val initializer = ExerciseLibraryInitializer(
            exerciseRepository = exerciseRepository,
            appLaunchStateRepository = launchStateRepository,
        )

        initializer.initialize()

        assertEquals(emptyList(), exerciseRepository.getExercises())
    }
}
