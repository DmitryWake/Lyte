package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.feature.splash.data.seed.DefaultExerciseLibrary
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializer
import com.nikolaevskii.lyte.feature.splash.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutExerciseRepository

/**
 * Засеивает библиотеку упражнений один раз за всё время жизни приложения. Гейт — персистентный флаг
 * [AppLaunchStateRepository], а не пустота таблицы упражнений: пользователь мог удалить все упражнения
 * после первого запуска, и это не должно приводить к повторному засеву.
 */
internal class ExerciseLibraryInitializer(
    private val exerciseRepository: WorkoutExerciseRepository,
    private val appLaunchStateRepository: AppLaunchStateRepository,
) : AppInitializer {

    override suspend fun initialize() {
        if (appLaunchStateRepository.hasCompletedFirstLaunch()) return

        DefaultExerciseLibrary.exercises.forEach { exercise ->
            exerciseRepository.createExercise(exercise)
        }
        appLaunchStateRepository.markFirstLaunchCompleted()
    }
}
