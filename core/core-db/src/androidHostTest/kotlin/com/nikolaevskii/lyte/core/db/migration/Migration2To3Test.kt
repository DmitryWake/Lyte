package com.nikolaevskii.lyte.core.db.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Миграция прогоняется на настоящей SQLite: таблица версии 2 собирается по DDL из закоммиченной
 * схемы (`core-db/schemas/…/2.json`, поле `createSql`), заполняется данными и мигрируется. Так
 * проверяется ровно то, что выполнится на устройстве пользователя, — а не эквивалент SQL в Kotlin.
 *
 * Дублирование DDL здесь намеренное: тест обязан описывать состояние «до» независимо от текущих
 * `@Entity`-классов, иначе он проверял бы миграцию против уже изменённой схемы.
 *
 * Из восьми таблиц v2 создаётся одна: `MIGRATION_2_3` не упоминает остальные, и появление там
 * запроса к чужой таблице должно уронить тест на «no such table», а не утонуть в неиспользуемом DDL.
 *
 * Про `AndroidSQLiteDriver` под Robolectric вместо `BundledSQLiteDriver` — см. [Migration1To2Test].
 */
@RunWith(RobolectricTestRunner::class)
class Migration2To3Test {

    @Test
    fun marksOnboardingCompletedForUsersWhoAlreadyPassedFirstLaunch() = withMigratedDatabase(
        arrange = { connection ->
            connection.execSQL("INSERT INTO app_launch_state (id, has_completed_first_launch) VALUES (0, 1)")
        },
        assert = { connection ->
            assertEquals(
                expected = listOf("1", "1"),
                actual = connection.row(
                    "SELECT has_completed_first_launch, has_completed_onboarding FROM app_launch_state WHERE id = 0",
                ),
                message = "Обучение не должно вылезти установленной базе поверх её реальных данных",
            )
        },
    )

    @Test
    fun copiesFirstLaunchFlagInsteadOfSettingOnboardingUnconditionally() = withMigratedDatabase(
        arrange = { connection ->
            connection.execSQL("INSERT INTO app_launch_state (id, has_completed_first_launch) VALUES (0, 0)")
        },
        assert = { connection ->
            assertEquals(
                expected = listOf("0"),
                actual = connection.row("SELECT has_completed_onboarding FROM app_launch_state WHERE id = 0"),
                message = "Бэкфилл — копия флага первого запуска, а не безусловная единица",
            )
        },
    )

    @Test
    fun leavesTableEmptyWhenNobodyLaunchedTheAppYet() = withMigratedDatabase(
        arrange = { },
        assert = { connection ->
            assertEquals(
                expected = listOf("0"),
                actual = connection.row("SELECT COUNT(*) FROM app_launch_state"),
                message = "Миграция не заводит строку: её отсутствие и означает «обучение ещё не проходили»",
            )
        },
    )

    @Test
    fun migratedTableMatchesCommittedSchemaV3() = withMigratedDatabase(
        arrange = { },
        assert = { connection ->
            // Room сверяет схему при открытии и падает на расхождении — в том числе на разном
            // DEFAULT одной и той же колонки, а не только на отсутствующей. Эталон — не список
            // ожидаемых полей руками, а такая же таблица, созданная по DDL схемы v3: сравниваются
            // «что построила миграция» и «что построил бы Room на чистой установке».
            val expected = AndroidSQLiteDriver().open(":memory:").use { fresh ->
                fresh.execSQL(APP_LAUNCH_STATE_V3)
                fresh.tableInfo()
            }

            assertEquals(expected = expected, actual = connection.tableInfo())
        },
    )

    /**
     * Собирает таблицу версии 2 в памяти, даёт тесту наполнить её, прогоняет миграцию и отдаёт
     * результат на проверку. Соединение закрывается в любом случае — иначе упавший тест утащил бы
     * за собой остальные.
     */
    private fun withMigratedDatabase(
        arrange: (SQLiteConnection) -> Unit,
        assert: (SQLiteConnection) -> Unit,
    ) {
        AndroidSQLiteDriver().open(":memory:").use { connection ->
            connection.execSQL(APP_LAUNCH_STATE_V2)
            arrange(connection)

            MIGRATION_2_3.migrate(connection)

            assert(connection)
        }
    }

    /** Значения первой строки результата как строки — сравнивать список нагляднее, чем поле за полем. */
    private fun SQLiteConnection.row(query: String): List<String> =
        prepare(query).use { statement ->
            check(statement.step()) { "Запрос не вернул ни одной строки: $query" }
            List(statement.getColumnCount()) { index -> statement.getText(index) }
        }

    /** Имя, тип, `NOT NULL` и `DEFAULT` каждой колонки — ровно то, что сверяет Room при открытии БД. */
    private fun SQLiteConnection.tableInfo(): List<List<String?>> =
        prepare("SELECT name, type, `notnull`, dflt_value FROM pragma_table_info('app_launch_state')").use { statement ->
            buildList {
                while (statement.step()) {
                    add(
                        List(statement.getColumnCount()) { index ->
                            if (statement.isNull(index)) null else statement.getText(index)
                        },
                    )
                }
            }
        }

    private companion object {

        /** DDL схемы v2 — дословно из `schemas/…/2.json`, только `${'$'}{TABLE_NAME}` раскрыт в имя таблицы. */
        const val APP_LAUNCH_STATE_V2: String =
            "CREATE TABLE IF NOT EXISTS `app_launch_state` (`id` INTEGER NOT NULL, " +
                "`has_completed_first_launch` INTEGER NOT NULL, PRIMARY KEY(`id`))"

        /** То же для схемы v3 — эталон, с которым сверяется результат миграции. */
        const val APP_LAUNCH_STATE_V3: String =
            "CREATE TABLE IF NOT EXISTS `app_launch_state` (`id` INTEGER NOT NULL, " +
                "`has_completed_first_launch` INTEGER NOT NULL, " +
                "`has_completed_onboarding` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))"
    }
}
