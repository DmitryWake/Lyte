package com.nikolaevskii.lyte.core.workout.domain.repository

import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    suspend fun getWorkouts(): List<WorkoutItemEntity>

    /** Реактивный список программ (SSOT — БД): эмитит при создании/правке/удалении. */
    fun observeWorkouts(): Flow<List<WorkoutItemEntity>>

    /**
     * Программа по id или `null`, если её нет — в том числе когда она удалена и живёт в БД только
     * архивной строкой (см. [deleteWorkout]). Вызывающий обязан показать это состояние, а не
     * продолжать работать с последним прочитанным графом: по удалённой программе нельзя ни начать
     * тренировку, ни сохранить правку.
     */
    suspend fun getWorkout(id: String): WorkoutEntity?

    suspend fun createWorkout(workoutEntity: WorkoutEntity)

    suspend fun editWorkout(workoutEntity: WorkoutEntity)

    /**
     * Обновляет **только** цели подходов программы — по значениям [workoutEntity]; состав и порядок
     * упражнений, их число подходов и архивность берутся из БД и не меняются. Подходы сопоставляются
     * по позициям, лишние позиции в [workoutEntity] игнорируются.
     *
     * Точка входа прогрессии по итогам сессии. Отдельно от [editWorkout] намеренно: тот пересобирает
     * граф целиком — сносит и вставляет заново связки с подходами, апсертит строки программы и
     * упражнений, — тогда как прогрессии нужно поправить только цели.
     */
    suspend fun updateWorkoutTargets(workoutEntity: WorkoutEntity)

    /**
     * Удаляет программу. Если на неё уже ссылаются завершённые/активные сессии трекера, программа
     * не удаляется физически, а архивируется — строка остаётся в БД, но перестаёт быть видимой:
     * ни в списках, ни через [getWorkout]. История от этого не страдает: сессия хранит снапшот
     * программы и на её строку не ссылается.
     */
    suspend fun deleteWorkout(id: String)
}