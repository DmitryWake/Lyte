package com.nikolaevskii.lyte.core.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Миграции схемы БД, применяемые по порядку версий.
 *
 * Схема заморожена на `version = 1` (см. [LyteDatabase]) — до первого релиза история версий была
 * схлопнута. Любое дальнейшее изменение схемы обязано добавить сюда `Migration`-объект (v1→v2 и т.д.)
 * и тест миграции по закоммиченным схемам из `core-db/schemas/`. Деструктивный сброс
 * (`fallbackToDestructiveMigration`) в релизной сборке запрещён — он стирает историю тренировок
 * пользователя при любом бампе версии.
 */
internal val LYTE_MIGRATIONS: Array<Migration> = emptyArray()

fun <T : RoomDatabase> RoomDatabase.Builder<T>.applyLyteDefaults(): RoomDatabase.Builder<T> =
    setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(*LYTE_MIGRATIONS)
