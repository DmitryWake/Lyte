package com.nikolaevskii.lyte.core.db.workout

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercise")
    suspend fun getAll(): List<ExerciseDatabaseEntity>

    @Query("SELECT * FROM exercise WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExerciseDatabaseEntity?

    @Upsert
    suspend fun upsert(exercise: ExerciseDatabaseEntity)

    @Query("DELETE FROM exercise WHERE id = :id")
    suspend fun deleteById(id: String)
}
