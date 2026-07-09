package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.feature.splash.data.seed.DefaultExerciseLibrary
import com.nikolaevskii.lyte.feature.splash.data.seed.DefaultWorkoutPrograms
import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkoutLibraryInitializerTest {

    @Test
    fun initializeSeedsAllExercisesOnFirstLaunch() = runTest {
        val exerciseRepository = FakeWorkoutExerciseRepository()
        val initializer = initializer(exerciseRepository = exerciseRepository)

        initializer.initialize()

        assertEquals(DefaultExerciseLibrary.exercises.toSet(), exerciseRepository.getExercises().toSet())
    }

    @Test
    fun initializeSeedsAllStarterProgramsOnFirstLaunch() = runTest {
        val workoutRepository = FakeWorkoutRepository()
        val initializer = initializer(workoutRepository = workoutRepository)

        initializer.initialize()

        assertEquals(
            DefaultWorkoutPrograms.programs.map { it.id }.toSet(),
            workoutRepository.getWorkouts().map { it.id }.toSet(),
        )
        DefaultWorkoutPrograms.programs.forEach { program ->
            assertEquals(program, workoutRepository.getWorkout(program.id))
        }
    }

    @Test
    fun initializeMarksFirstLaunchCompleted() = runTest {
        val launchStateRepository = FakeAppLaunchStateRepository(hasCompletedFirstLaunch = false)
        val initializer = initializer(appLaunchStateRepository = launchStateRepository)

        initializer.initialize()

        assertTrue(launchStateRepository.hasCompletedFirstLaunch())
    }

    @Test
    fun initializeDoesNotReseedWhenFirstLaunchAlreadyCompletedEvenIfLibraryWasEmptied() = runTest {
        // Пользователь мог удалить все упражнения/программы после первого запуска — это не первый
        // запуск, и таблицы не должны быть заново засеяны из-за одной лишь их пустоты.
        val exerciseRepository = FakeWorkoutExerciseRepository()
        val seeded = WorkoutExerciseEntity(id = "seeded", name = "Засеянное")
        exerciseRepository.createExercise(seeded)
        exerciseRepository.deleteExercise(seeded.id)
        val workoutRepository = FakeWorkoutRepository()
        val launchStateRepository = FakeAppLaunchStateRepository(hasCompletedFirstLaunch = true)
        val initializer = initializer(
            exerciseRepository = exerciseRepository,
            workoutRepository = workoutRepository,
            appLaunchStateRepository = launchStateRepository,
        )

        initializer.initialize()

        assertEquals(emptyList(), exerciseRepository.getExercises())
        assertEquals(emptyList(), workoutRepository.getWorkouts())
    }

    private fun initializer(
        exerciseRepository: FakeWorkoutExerciseRepository = FakeWorkoutExerciseRepository(),
        workoutRepository: FakeWorkoutRepository = FakeWorkoutRepository(),
        appLaunchStateRepository: FakeAppLaunchStateRepository = FakeAppLaunchStateRepository(hasCompletedFirstLaunch = false),
    ): WorkoutLibraryInitializer = WorkoutLibraryInitializer(
        exerciseRepository = exerciseRepository,
        workoutRepository = workoutRepository,
        appLaunchStateRepository = appLaunchStateRepository,
    )
}
