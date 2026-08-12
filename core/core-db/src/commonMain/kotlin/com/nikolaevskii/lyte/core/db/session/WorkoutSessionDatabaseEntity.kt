package com.nikolaevskii.lyte.core.db.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nikolaevskii.lyte.core.db.DEFAULT_ACCENT_SQL
import com.nikolaevskii.lyte.core.db.DEFAULT_GLYPH_SQL

/**
 * Сессия тренировки. `finished_at IS NULL` — сессия активна (в приложении может быть только одна;
 * инвариант держит [WorkoutSessionDao.insertSessionGraph]).
 *
 * `program_id`/`program_name`/`program_accent`/`program_glyph` — снапшот программы на момент старта:
 * программа может быть переименована, перекрашена или заархивирована (см. `workout.is_archived`),
 * история показывает данные на момент тренировки. FK на `workout` намеренно нет — сессия
 * самодостаточна и переживает жизненный цикл программы.
 *
 * Маркер упражнения, в отличие от маркера программы, НЕ снапшотится: он читается живым из `exercise`
 * по `session_exercise.exercise_id` — там же, откуда берётся имя (см. [SessionExerciseWithSets]).
 */
@Entity(
    tableName = "workout_session",
    indices = [
        Index("program_id"),
        Index("finished_at"),
    ],
)
data class WorkoutSessionDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "program_id")
    val programId: String,
    @ColumnInfo(name = "program_name")
    val programName: String,
    @ColumnInfo(name = "program_accent", defaultValue = DEFAULT_ACCENT_SQL)
    val programAccent: String,
    @ColumnInfo(name = "program_glyph", defaultValue = DEFAULT_GLYPH_SQL)
    val programGlyph: String,
    @ColumnInfo(name = "started_at")
    val startedAt: Long,
    @ColumnInfo(name = "finished_at")
    val finishedAt: Long?,
    @ColumnInfo(name = "current_exercise_id")
    val currentExerciseId: String?,
)
