package com.nikolaevskii.lyte.core.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.nikolaevskii.lyte.core.db.DEFAULT_ACCENT_SQL
import com.nikolaevskii.lyte.core.db.DEFAULT_GLYPH_SQL

/**
 * Маркер сид-строки: ключи те же, что у доменных `ExerciseAccent`/`ExerciseGlyph`
 * в `:core:core-workout`.
 */
private data class SeedMarker(val accent: String, val glyph: String)

/**
 * Маркеры стартовой библиотеки и стартовых программ — по макету v2
 * (`design/v2/LyteScreen.dc.html`, строки 557–606).
 *
 * Id-шники дублируют `DefaultExerciseLibrary`/`DefaultWorkoutPrograms` из `:feature:splash:impl`:
 * core-модуль БД не может зависеть от фичи. Расхождение не ломает миграцию — `UPDATE` по
 * несуществующему id меняет ноль строк, и упражнение просто останется с дефолтным маркером.
 */
private val SEED_EXERCISE_MARKERS: Map<String, SeedMarker> = mapOf(
    "seed-back-squat" to SeedMarker(accent = "lime", glyph = "squat"),
    "seed-deadlift" to SeedMarker(accent = "coral", glyph = "deadlift"),
    "seed-bench-press" to SeedMarker(accent = "indigo", glyph = "bench-press"),
    "seed-bent-over-row" to SeedMarker(accent = "coral", glyph = "deadlift"),
    "seed-pull-up" to SeedMarker(accent = "coral", glyph = "pull-up"),
    "seed-overhead-press" to SeedMarker(accent = "indigo", glyph = "dumbbell-press"),
    "seed-biceps-curl" to SeedMarker(accent = "amber", glyph = "curl"),
    "seed-incline-dumbbell-press" to SeedMarker(accent = "indigo", glyph = "dumbbell-press"),
    "seed-dip" to SeedMarker(accent = "teal", glyph = "pull-up"),
    "seed-triceps-pushdown" to SeedMarker(accent = "amber", glyph = "machine"),
)

private val SEED_PROGRAM_MARKERS: Map<String, SeedMarker> = mapOf(
    "seed-program-push-day" to SeedMarker(accent = "indigo", glyph = "bench-press"),
    "seed-program-pull-day" to SeedMarker(accent = "coral", glyph = "pull-up"),
    "seed-program-leg-day" to SeedMarker(accent = "lime", glyph = "squat"),
)

/**
 * v1 → v2: у упражнения и программы появляется маркер (цвет и знак), у сессии — его снапшот.
 *
 * Три шага:
 * 1. `ALTER TABLE ... ADD COLUMN` с тем же `DEFAULT`, что заявлен сущностями — иначе Room не примет
 *    схему при открытии БД. Существующие строки получают дефолтный маркер, данные не теряются.
 * 2. Строки стартовой библиотеки и стартовых программ (стабильные `seed-*` id) получают маркеры из
 *    макета: без этого у пользователя, поставившего v1, вся библиотека осталась бы серой.
 * 3. Снапшот маркера в `workout_session` заполняется из программы, на которую сессия ссылается.
 *    Программы уже нет (жёстко удалена) — остаётся дефолт: `COALESCE` вместо `NULL`, колонка `NOT NULL`.
 */
internal val MIGRATION_1_2: Migration = object : Migration(1, 2) {

    override fun migrate(connection: SQLiteConnection) {
        addMarkerColumns(connection = connection, table = "exercise")
        addMarkerColumns(connection = connection, table = "workout")
        applySeedMarkers(connection = connection, table = "exercise", markers = SEED_EXERCISE_MARKERS)
        applySeedMarkers(connection = connection, table = "workout", markers = SEED_PROGRAM_MARKERS)

        addMarkerColumns(connection = connection, table = "workout_session", prefix = "program_")
        connection.execSQL(
            """
            UPDATE workout_session SET
                program_accent = COALESCE(
                    (SELECT accent FROM workout WHERE workout.id = workout_session.program_id),
                    $DEFAULT_ACCENT_SQL
                ),
                program_glyph = COALESCE(
                    (SELECT glyph FROM workout WHERE workout.id = workout_session.program_id),
                    $DEFAULT_GLYPH_SQL
                )
            """,
        )
    }

    /** [prefix] — для снапшота в `workout_session`, где колонки называются `program_*`. */
    private fun addMarkerColumns(connection: SQLiteConnection, table: String, prefix: String = "") {
        connection.execSQL(
            "ALTER TABLE $table ADD COLUMN ${prefix}accent TEXT NOT NULL DEFAULT $DEFAULT_ACCENT_SQL",
        )
        connection.execSQL(
            "ALTER TABLE $table ADD COLUMN ${prefix}glyph TEXT NOT NULL DEFAULT $DEFAULT_GLYPH_SQL",
        )
    }

    private fun applySeedMarkers(
        connection: SQLiteConnection,
        table: String,
        markers: Map<String, SeedMarker>,
    ) {
        connection.prepare("UPDATE $table SET accent = ?, glyph = ? WHERE id = ?").use { statement ->
            markers.forEach { (id, marker) ->
                statement.bindText(index = 1, value = marker.accent)
                statement.bindText(index = 2, value = marker.glyph)
                statement.bindText(index = 3, value = id)
                statement.step()
                statement.reset()
            }
        }
    }
}
