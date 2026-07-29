package com.nikolaevskii.lyte.core.session.data.repository

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * In-memory реализация [WorkoutRepository] для тестов сессий: нужна, чтобы проверить прогрессию плана
 * по итогам сессии. `updateWorkoutTargets` повторяет семантику `WorkoutDao.updateSetTargets` — правит
 * только цели по позициям, структуру программы не меняет.
 */
internal class FakeWorkoutRepository : WorkoutRepository {

    private val workouts = mutableMapOf<String, WorkoutEntity>()

    override suspend fun getWorkouts(): List<WorkoutItemEntity> =
        workouts.values.map { workout ->
            WorkoutItemEntity(
                id = workout.id,
                name = workout.name,
                description = workout.description,
                exerciseCount = workout.exercises.size,
            )
        }

    override fun observeWorkouts(): Flow<List<WorkoutItemEntity>> = flow { emit(getWorkouts()) }

    override suspend fun getWorkout(id: String): WorkoutEntity? = workouts[id]

    override suspend fun createWorkout(workoutEntity: WorkoutEntity) {
        workouts[workoutEntity.id] = workoutEntity
    }

    override suspend fun editWorkout(workoutEntity: WorkoutEntity) {
        workouts[workoutEntity.id] = workoutEntity
    }

    override suspend fun updateWorkoutTargets(workoutEntity: WorkoutEntity) {
        val stored = workouts[workoutEntity.id] ?: return
        val exercises = stored.exercises.mapIndexed { exerciseIndex, storedExercise ->
            val source = workoutEntity.exercises.getOrNull(exerciseIndex) ?: return@mapIndexed storedExercise
            val reps = storedExercise.reps.mapIndexed { repIndex, storedRep ->
                source.reps.getOrNull(repIndex) ?: storedRep
            }
            storedExercise.copy(reps = reps)
        }
        workouts[workoutEntity.id] = stored.copy(exercises = exercises)
    }

    override suspend fun deleteWorkout(id: String) {
        workouts.remove(id)
    }
}
