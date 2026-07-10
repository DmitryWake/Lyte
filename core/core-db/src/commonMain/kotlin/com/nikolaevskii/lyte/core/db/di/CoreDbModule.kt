package com.nikolaevskii.lyte.core.db.di

import com.nikolaevskii.lyte.core.db.LyteDatabase
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao
import com.nikolaevskii.lyte.core.db.applyLyteDefaults
import com.nikolaevskii.lyte.core.db.internal.lyteDatabaseBuilder
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDao
import com.nikolaevskii.lyte.core.db.workout.ExerciseDao
import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import org.koin.core.module.Module
import org.koin.dsl.module

fun coreDbModule(): Module = module {
    single<LyteDatabase> {
        lyteDatabaseBuilder()
            .applyLyteDefaults()
            .build()
    }
    single<WorkoutDao> { get<LyteDatabase>().workoutDao() }
    single<ExerciseDao> { get<LyteDatabase>().exerciseDao() }
    single<WorkoutSessionDao> { get<LyteDatabase>().workoutSessionDao() }
    single<AppLaunchStateDao> { get<LyteDatabase>().appLaunchStateDao() }
}
