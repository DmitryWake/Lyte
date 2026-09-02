package com.nikolaevskii.lyte.core.db.migration

import androidx.room.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.nikolaevskii.lyte.core.db.LYTE_MIGRATIONS
import com.nikolaevskii.lyte.core.db.LyteDatabase
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `Migration1To2Test` и `Migration2To3Test` вызывают свой `Migration` напрямую и потому слепы к
 * единственной ошибке, которая ломает приложение целиком: миграция написана, оттестирована — и не
 * попала в [LYTE_MIGRATIONS]. Вся сборка при этом зелёная, а установленная база при открытии
 * получает `A migration from N to N+1 was required but not found` и приложение не стартует.
 *
 * Текущая версия не выписана числом, а берётся из БД, которую построил настоящий Room-билдер:
 * константа в тесте — это второе место, где живёт версия, и забыть обновить её так же легко, как
 * зарегистрировать миграцию. Тогда тест молчал бы ровно про ту дыру, ради которой написан.
 */
@RunWith(RobolectricTestRunner::class)
class LyteMigrationChainTest {

    @Test
    fun migrationsCoverEveryVersionUpToCurrentSchema() = runTest {
        val currentVersion = createDatabaseAndReadItsVersion()

        assertEquals(
            expected = (FIRST_VERSION until currentVersion).map { version -> version to version + 1 },
            actual = LYTE_MIGRATIONS.map { migration -> migration.startVersion to migration.endVersion },
            message = "LYTE_MIGRATIONS обязан вести от первой схемы к текущей ($currentVersion) без " +
                "пропусков: и незарегистрированная миграция, и бамп версии без миграции проходят " +
                "все остальные тесты, а установленную базу перестаёт открывать Room",
        )
    }

    /**
     * Room ставит `user_version` при создании файла, поэтому БД поднимается на диске, а не в памяти:
     * после закрытия версию читает обычный `PRAGMA` — тот же путь, которым её узнаёт Room при
     * следующем открытии, когда решает, какие миграции прогонять.
     */
    private suspend fun createDatabaseAndReadItsVersion(): Int {
        val file = File.createTempFile("lyte-schema-version", ".db").apply { delete() }
        try {
            Room
                .databaseBuilder<LyteDatabase>(
                    context = RuntimeEnvironment.getApplication(),
                    name = file.absolutePath,
                )
                .setDriver(AndroidSQLiteDriver())
                .build()
                .apply { appLaunchStateDao().get() }
                .close()

            return AndroidSQLiteDriver().open(file.absolutePath).use { connection ->
                connection.userVersion()
            }
        } finally {
            file.delete()
        }
    }

    private fun SQLiteConnection.userVersion(): Int =
        prepare("PRAGMA user_version").use { statement ->
            check(statement.step()) { "PRAGMA user_version не вернул значения" }
            statement.getInt(0)
        }

    private companion object {

        /** История до первого релиза схлопнута в `schemas/…/1.json` — раньше версии 1 ничего нет. */
        const val FIRST_VERSION: Int = 1
    }
}
