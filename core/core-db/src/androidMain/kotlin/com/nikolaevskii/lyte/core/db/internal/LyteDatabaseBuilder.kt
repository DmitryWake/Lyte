package com.nikolaevskii.lyte.core.db.internal

import androidx.room.Room
import androidx.room.RoomDatabase
import com.nikolaevskii.lyte.core.db.LyteDatabase
import com.nikolaevskii.lyte.core.db.androidDatabaseContext
import com.nikolaevskii.lyte.core.db.androidDatabaseFile

internal actual fun lyteDatabaseBuilder(): RoomDatabase.Builder<LyteDatabase> =
    Room.databaseBuilder<LyteDatabase>(
        context = androidDatabaseContext(),
        name = androidDatabaseFile(DATABASE_NAME),
    )

private const val DATABASE_NAME: String = "lyte.db"
