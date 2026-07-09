package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.feature.splash.data.seed.DefaultExerciseLibrary
import com.nikolaevskii.lyte.feature.splash.data.seed.DefaultWorkoutPrograms
import com.nikolaevskii.lyte.feature.splash.domain.initializer.AppInitializer
import com.nikolaevskii.lyte.feature.splash.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutExerciseRepository
import com.nikolaevskii.lyte.feature.workout.domain.repository.WorkoutRepository

/**
 * Засеивает библиотеку упражнений и стартовые программы один раз за всё время жизни приложения.
 * Гейт — персистентный флаг [AppLaunchStateRepository], а не пустота таблиц: пользователь мог удалить
 * все упражнения/программы после первого запуска, и это не должно приводить к повторному засеву.
 *
 * Оба вида сидов проверяют и выставляют один и тот же флаг внутри одного вызова [initialize] —
 * специально не разнесены на два отдельных [AppInitializer], иначе второй молча пропустил бы себя,
 * увидев флаг уже выставленным первым.
 */
internal class WorkoutLibraryInitializer(
    private val exerciseRepository: WorkoutExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val appLaunchStateRepository: AppLaunchStateRepository,
) : AppInitializer {

    override suspend fun initialize() {
        if (appLaunchStateRepository.hasCompletedFirstLaunch()) return

        DefaultExerciseLibrary.exercises.forEach { exercise ->
            exerciseRepository.createExercise(exercise)
        }
        DefaultWorkoutPrograms.programs.forEach { program ->
            workoutRepository.createWorkout(program)
        }
        appLaunchStateRepository.markFirstLaunchCompleted()
    }
}
