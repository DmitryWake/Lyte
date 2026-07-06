package com.nikolaevskii.lyte.core.db.internal

import androidx.room.RoomDatabase
import com.nikolaevskii.lyte.core.db.LyteDatabase

internal expect fun lyteDatabaseBuilder(): RoomDatabase.Builder<LyteDatabase>
