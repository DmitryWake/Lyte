package com.nikolaevskii.lyte.core.db.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Подход (rep) внутри связки «тренировка ↔ упражнение». */
@Entity(
    tableName = "workout_set",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseCrossRefDatabaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("workout_exercise_id"),
    ],
)
data class WorkoutSetDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "workout_exercise_id")
    val workoutExerciseId: String,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "count")
    val count: Int,
    @ColumnInfo(name = "weight")
    val weight: Double?,
)
