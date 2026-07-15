package com.nikolaevskii.lyte.feature.splash.data.seed

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity

/** Стартовые программы тренировок — сеются в БД при первом запуске, см. [com.nikolaevskii.lyte.feature.splash.data.initializer.WorkoutLibraryInitializer]. */
internal object DefaultWorkoutPrograms {

    val programs: List<WorkoutEntity> = listOf(
        WorkoutEntity(
            id = "seed-program-push-day",
            name = "Push Day",
            description = null,
            exercises = listOf(
                exercise("seed-bench-press", 8 to 70.0, 8 to 80.0, 6 to 85.0, 6 to 85.0),
                exercise("seed-incline-dumbbell-press", 10 to 24.0, 10 to 26.0, 8 to 26.0),
                exercise("seed-dip", 12 to null, 12 to null, 10 to null),
                exercise("seed-overhead-press", 8 to 40.0, 8 to 45.0, 6 to 45.0),
                exercise("seed-triceps-pushdown", 12 to 25.0, 12 to 25.0, 12 to 25.0),
            ),
        ),
        WorkoutEntity(
            id = "seed-program-pull-day",
            name = "Pull Day",
            description = null,
            exercises = listOf(
                exercise("seed-deadlift", 6 to 110.0, 6 to 110.0, 5 to 120.0),
                exercise("seed-bent-over-row", 10 to 60.0, 10 to 60.0, 8 to 65.0),
                exercise("seed-pull-up", 10 to null, 8 to null, 8 to null),
                exercise("seed-biceps-curl", 12 to 14.0, 12 to 14.0, 10 to 16.0),
            ),
        ),
        WorkoutEntity(
            id = "seed-program-leg-day",
            name = "Leg Day",
            description = null,
            exercises = listOf(
                exercise("seed-back-squat", 8 to 90.0, 8 to 90.0, 6 to 95.0),
                exercise("seed-deadlift", 6 to 110.0, 6 to 110.0, 5 to 120.0),
                exercise("seed-triceps-pushdown", 12 to 25.0, 12 to 25.0, 12 to 25.0),
            ),
        ),
    )

    private fun exercise(
        exerciseId: String,
        vararg plan: Pair<Int, Double?>,
    ): WorkoutExerciseWithRepsEntity =
        WorkoutExerciseWithRepsEntity(
            exercise = DefaultExerciseLibrary.exercises.first { it.id == exerciseId },
            reps = plan.map { (count, weight) -> WorkoutRepEntity(count = count, weight = weight) },
        )
}
