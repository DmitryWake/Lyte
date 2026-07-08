package com.nikolaevskii.lyte.core.db.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Упорядоченная связка «тренировка ↔ упражнение».
 * Одна строка соответствует одному упражнению внутри тренировки со своими подходами.
 */
@Entity(
    tableName = "workout_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutDatabaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseDatabaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("workout_id"),
        Index("exercise_id"),
    ],
)
data class WorkoutExerciseCrossRefDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "workout_id")
    val workoutId: String,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,
    @ColumnInfo(name = "position")
    val position: Int,
)
