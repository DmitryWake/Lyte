package com.nikolaevskii.lyte.core.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun <T : RoomDatabase> RoomDatabase.Builder<T>.applyLyteDefaults(): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
