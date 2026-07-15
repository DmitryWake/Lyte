package com.nikolaevskii.lyte.core.workout.domain.repository

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity

interface WorkoutRepository {

    suspend fun getWorkouts(): List<WorkoutItemEntity>

    suspend fun getWorkout(id: String): WorkoutEntity?

    suspend fun createWorkout(workoutEntity: WorkoutEntity)

    suspend fun editWorkout(workoutEntity: WorkoutEntity)

    /**
     * Удаляет программу. Если на неё уже ссылаются завершённые/активные сессии трекера, программа
     * не удаляется физически, а архивируется (пропадает из списков, но остаётся доступной по id) —
     * чтобы история сессий сохранилась.
     */
    suspend fun deleteWorkout(id: String)
}