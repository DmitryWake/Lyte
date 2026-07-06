package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout ORDER BY started_at DESC")
    fun observeAll(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workout WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<WorkoutEntity?>

    @Query("SELECT COUNT(*) FROM workout")
    suspend fun count(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<WorkoutEntity>)
}
