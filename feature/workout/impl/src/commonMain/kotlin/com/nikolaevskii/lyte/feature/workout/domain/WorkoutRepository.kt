package com.nikolaevskii.lyte.feature.workout.domain

import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    fun observeAll(): Flow<List<Workout>>

    fun observeById(id: Long): Flow<Workout?>

    suspend fun seedIfEmpty()
}
