package com.nikolaevskii.lyte.core.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.nikolaevskii.lyte.core.db.migration.MIGRATION_1_2
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Миграции схемы БД, применяемые по порядку версий.
 *
 * Любое изменение схемы обязано добавить сюда `Migration`-объект и тест миграции по закоммиченным
 * схемам из `core-db/schemas/`. Деструктивный сброс (`fallbackToDestructiveMigration`) в релизной
 * сборке запрещён — он стирает историю тренировок пользователя при любом бампе версии.
 */
internal val LYTE_MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)

fun <T : RoomDatabase> RoomDatabase.Builder<T>.applyLyteDefaults(): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(*LYTE_MIGRATIONS)
