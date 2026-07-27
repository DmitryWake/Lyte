package com.nikolaevskii.lyte.core.workout.domain.repository

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    suspend fun getWorkouts(): List<WorkoutItemEntity>

    /** Реактивный список программ (SSOT — БД): эмитит при создании/правке/удалении. */
    fun observeWorkouts(): Flow<List<WorkoutItemEntity>>

    suspend fun getWorkout(id: String): WorkoutEntity?

    suspend fun createWorkout(workoutEntity: WorkoutEntity)

    suspend fun editWorkout(workoutEntity: WorkoutEntity)

    /**
     * Обновляет **только** цели подходов программы — по значениям [workoutEntity]; состав и порядок
     * упражнений, их число подходов и архивность берутся из БД и не меняются. Подходы сопоставляются
     * по позициям, лишние позиции в [workoutEntity] игнорируются.
     *
     * Точка входа прогрессии по итогам сессии. Отдельно от [editWorkout] намеренно: тот пересобирает
     * граф программы целиком и заодно разархивировал бы удалённую программу и её упражнения.
     */
    suspend fun updateWorkoutTargets(workoutEntity: WorkoutEntity)

    /**
     * Удаляет программу. Если на неё уже ссылаются завершённые/активные сессии трекера, программа
     * не удаляется физически, а архивируется (пропадает из списков, но остаётся доступной по id) —
     * чтобы история сессий сохранилась.
     */
    suspend fun deleteWorkout(id: String)
}