package com.nikolaevskii.lyte.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import com.nikolaevskii.lyte.core.db.workout.WorkoutEntity

@Database(
    entities = [
        WorkoutEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(LyteDatabaseConstructor::class)
abstract class LyteDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object LyteDatabaseConstructor : RoomDatabaseConstructor<LyteDatabase> {

    override fun initialize(): LyteDatabase
}
