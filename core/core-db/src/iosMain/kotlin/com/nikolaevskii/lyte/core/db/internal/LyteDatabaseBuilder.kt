package com.nikolaevskii.lyte.core.db.internal

import androidx.room.Room
import androidx.room.RoomDatabase
import com.nikolaevskii.lyte.core.db.LyteDatabase
import com.nikolaevskii.lyte.core.db.iosDatabaseFilePath

internal actual fun lyteDatabaseBuilder(): RoomDatabase.Builder<LyteDatabase> =
    Room.databaseBuilder<LyteDatabase>(
        name = iosDatabaseFilePath(DATABASE_NAME),
    )

private const val DATABASE_NAME: String = "lyte.db"
