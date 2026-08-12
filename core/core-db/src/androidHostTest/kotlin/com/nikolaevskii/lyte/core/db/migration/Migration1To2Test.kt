package com.nikolaevskii.lyte.core.db.migration

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Миграция прогоняется на настоящей SQLite: БД версии 1 собирается по DDL из закоммиченной схемы
 * (`core-db/schemas/…/1.json`, поле `createSql`), заполняется данными и мигрируется. Так проверяется
 * ровно то, что выполнится на устройстве пользователя, — а не эквивалент SQL в Kotlin.
 *
 * Дублирование DDL здесь намеренное: тест обязан описывать состояние «до» независимо от текущих
 * `@Entity`-классов, иначе он проверял бы миграцию против уже изменённой схемы.
 *
 * Тест живёт в `androidHostTest`, а не в `commonTest`, и берёт [AndroidSQLiteDriver] под Robolectric:
 * приложение работает на `BundledSQLiteDriver`, но его JNI собран под Android и на host-JVM не
 * грузится (`UnsatisfiedLinkError`), а Robolectric подкладывает нативную SQLite хоста. SQL от выбора
 * драйвера не зависит, поэтому проверка на Android покрывает и iOS.
 */
@RunWith(RobolectricTestRunner::class)
class Migration1To2Test {

    @Test
    fun keepsExistingDataAndFillsMarkersWithDefaults() = withMigratedDatabase(
        arrange = { connection ->
            connection.execSQL(
                "INSERT INTO exercise (id, name, name_normalized, description, is_archived) " +
                    "VALUES ('own-1', 'Выпады', 'выпады', 'Своё упражнение', 0)",
            )
            connection.execSQL(
                "INSERT INTO workout (id, name, description, is_archived) " +
                    "VALUES ('own-program', 'Своя программа', NULL, 0)",
            )
        },
        assert = { connection ->
            assertEquals(
                expected = listOf("Выпады", "выпады", "Своё упражнение", "0", "slate", "squat"),
                actual = connection.row(
                    "SELECT name, name_normalized, description, is_archived, accent, glyph " +
                        "FROM exercise WHERE id = 'own-1'",
                ),
                message = "Данные упражнения должны пережить миграцию, маркер — стать дефолтным",
            )
            assertEquals(
                expected = listOf("Своя программа", "slate", "squat"),
                actual = connection.row("SELECT name, accent, glyph FROM workout WHERE id = 'own-program'"),
            )
        },
    )

    @Test
    fun appliesDesignMarkersToSeedRows() = withMigratedDatabase(
        arrange = { connection ->
            connection.execSQL(
                "INSERT INTO exercise (id, name, name_normalized, description, is_archived) VALUES " +
                    "('seed-back-squat', 'Приседания со штангой', 'приседания со штангой', NULL, 0)," +
                    "('seed-dip', 'Отжимания на брусьях', 'отжимания на брусьях', NULL, 0)",
            )
            connection.execSQL(
                "INSERT INTO workout (id, name, description, is_archived) " +
                    "VALUES ('seed-program-push-day', 'Push Day', NULL, 0)",
            )
        },
        assert = { connection ->
            assertEquals(
                expected = listOf("lime", "squat"),
                actual = connection.row("SELECT accent, glyph FROM exercise WHERE id = 'seed-back-squat'"),
            )
            assertEquals(
                expected = listOf("teal", "pull-up"),
                actual = connection.row("SELECT accent, glyph FROM exercise WHERE id = 'seed-dip'"),
            )
            assertEquals(
                expected = listOf("indigo", "bench-press"),
                actual = connection.row("SELECT accent, glyph FROM workout WHERE id = 'seed-program-push-day'"),
            )
        },
    )

    @Test
    fun backfillsSessionSnapshotFromProgram() = withMigratedDatabase(
        arrange = { connection ->
            connection.execSQL(
                "INSERT INTO workout (id, name, description, is_archived) " +
                    "VALUES ('seed-program-leg-day', 'Leg Day', NULL, 0)",
            )
            connection.execSQL(
                "INSERT INTO workout_session (id, program_id, program_name, started_at, finished_at, current_exercise_id) " +
                    "VALUES ('s-1', 'seed-program-leg-day', 'Leg Day', 1000, 2000, NULL)," +
                    "('s-2', 'deleted-program', 'Удалённая', 3000, 4000, NULL)",
            )
        },
        assert = { connection ->
            assertEquals(
                expected = listOf("lime", "squat"),
                actual = connection.row("SELECT program_accent, program_glyph FROM workout_session WHERE id = 's-1'"),
                message = "Сессия существующей программы получает её маркер",
            )
            assertEquals(
                expected = listOf("slate", "squat"),
                actual = connection.row("SELECT program_accent, program_glyph FROM workout_session WHERE id = 's-2'"),
                message = "Сессия удалённой программы получает дефолтный маркер, а не NULL",
            )
        },
    )

    /**
     * Собирает БД версии 1 в памяти, даёт тесту наполнить её, прогоняет миграцию и отдаёт результат
     * на проверку. Соединение закрывается в любом случае — иначе упавший тест утащил бы за собой
     * остальные.
     */
    private fun withMigratedDatabase(
        arrange: (SQLiteConnection) -> Unit,
        assert: (SQLiteConnection) -> Unit,
    ) {
        AndroidSQLiteDriver().open(":memory:").use { connection ->
            SCHEMA_V1.forEach { statement -> connection.execSQL(statement) }
            arrange(connection)

            MIGRATION_1_2.migrate(connection)

            assert(connection)
        }
    }

    /** Значения первой строки результата как строки — сравнивать список нагляднее, чем поле за полем. */
    private fun SQLiteConnection.row(query: String): List<String> =
        prepare(query).use { statement ->
            check(statement.step()) { "Запрос не вернул ни одной строки: $query" }
            List(statement.getColumnCount()) { index -> statement.getText(index) }
        }

    private companion object {

        /** DDL схемы v1 — дословно из `schemas/…/1.json`, только `${'$'}{TABLE_NAME}` раскрыт в имена таблиц. */
        val SCHEMA_V1: List<String> = listOf(
            "CREATE TABLE IF NOT EXISTS `workout` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                "`description` TEXT, `is_archived` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))",
            "CREATE TABLE IF NOT EXISTS `exercise` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                "`name_normalized` TEXT NOT NULL, `description` TEXT, " +
                "`is_archived` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`id`))",
            "CREATE TABLE IF NOT EXISTS `workout_exercise` (`id` TEXT NOT NULL, `workout_id` TEXT NOT NULL, " +
                "`exercise_id` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`workout_id`) REFERENCES `workout`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`exercise_id`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            "CREATE TABLE IF NOT EXISTS `workout_set` (`id` TEXT NOT NULL, `workout_exercise_id` TEXT NOT NULL, " +
                "`position` INTEGER NOT NULL, `count` INTEGER NOT NULL, `weight` REAL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`workout_exercise_id`) REFERENCES `workout_exercise`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
            "CREATE TABLE IF NOT EXISTS `workout_session` (`id` TEXT NOT NULL, `program_id` TEXT NOT NULL, " +
                "`program_name` TEXT NOT NULL, `started_at` INTEGER NOT NULL, `finished_at` INTEGER, " +
                "`current_exercise_id` TEXT, PRIMARY KEY(`id`))",
            "CREATE TABLE IF NOT EXISTS `session_exercise` (`id` TEXT NOT NULL, `session_id` TEXT NOT NULL, " +
                "`exercise_id` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`), " +
                "FOREIGN KEY(`session_id`) REFERENCES `workout_session`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
                "FOREIGN KEY(`exercise_id`) REFERENCES `exercise`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )",
            "CREATE TABLE IF NOT EXISTS `session_set` (`id` TEXT NOT NULL, `session_exercise_id` TEXT NOT NULL, " +
                "`position` INTEGER NOT NULL, `target_count` INTEGER NOT NULL, `target_weight` REAL, " +
                "`result_status` TEXT, `result_count` INTEGER, `result_weight` REAL, `note` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`), FOREIGN KEY(`session_exercise_id`) REFERENCES `session_exercise`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )",
            "CREATE TABLE IF NOT EXISTS `app_launch_state` (`id` INTEGER NOT NULL, " +
                "`has_completed_first_launch` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }
}
