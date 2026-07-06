package com.nikolaevskii.lyte.core.db

import android.content.Context
import org.koin.core.context.GlobalContext

fun androidDatabaseContext(): Context =
    GlobalContext.get().get<Context>().applicationContext

fun androidDatabaseFile(name: String): String =
    androidDatabaseContext().getDatabasePath(name).absolutePath
