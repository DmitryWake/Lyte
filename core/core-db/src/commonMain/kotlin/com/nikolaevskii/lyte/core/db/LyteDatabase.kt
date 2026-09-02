package com.nikolaevskii.lyte.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateEntity
import com.nikolaevskii.lyte.core.db.session.SessionExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDao
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import com.nikolaevskii.lyte.core.db.workout.WorkoutDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutExerciseCrossRefDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutSetDatabaseEntity

@Database(
    entities = [
        WorkoutDatabaseEntity::class,
        ExerciseDatabaseEntity::class,
        WorkoutExerciseCrossRefDatabaseEntity::class,
        WorkoutSetDatabaseEntity::class,
        WorkoutSessionDatabaseEntity::class,
        SessionExerciseDatabaseEntity::class,
        SessionSetDatabaseEntity::class,
        AppLaunchStateEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@ConstructedBy(LyteDatabaseConstructor::class)
abstract class LyteDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    abstract fun exerciseDao(): ExerciseDao

    abstract fun workoutSessionDao(): WorkoutSessionDao

    abstract fun appLaunchStateDao(): AppLaunchStateDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object LyteDatabaseConstructor : RoomDatabaseConstructor<LyteDatabase> {

    override fun initialize(): LyteDatabase
}
