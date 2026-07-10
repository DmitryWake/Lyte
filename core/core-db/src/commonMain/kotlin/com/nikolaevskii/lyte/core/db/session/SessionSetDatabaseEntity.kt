package com.nikolaevskii.lyte.core.db.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Подход внутри упражнения сессии: план (`target_*`) и факт (`result_*`).
 *
 * `result_status`: `NULL` — подход ещё не выполнялся (pending); [RESULT_STATUS_COMPLETED] — выполнен
 * с фактическими `result_count`/`result_weight`; [RESULT_STATUS_SKIPPED] — пропущен (факт пустой).
 * Статус хранится строкой без Room-`enum`/`TypeConverter` — модуль остаётся без конвертеров.
 *
 * `target_weight`/`result_weight` `NULL` — упражнение со своим весом (bodyweight).
 */
@Entity(
    tableName = "session_set",
    foreignKeys = [
        ForeignKey(
            entity = SessionExerciseDatabaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("session_exercise_id"),
    ],
)
data class SessionSetDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_exercise_id")
    val sessionExerciseId: String,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "target_count")
    val targetCount: Int,
    @ColumnInfo(name = "target_weight")
    val targetWeight: Double?,
    @ColumnInfo(name = "result_status")
    val resultStatus: String?,
    @ColumnInfo(name = "result_count")
    val resultCount: Int?,
    @ColumnInfo(name = "result_weight")
    val resultWeight: Double?,
    @ColumnInfo(name = "note")
    val note: String,
) {

    companion object {
        const val RESULT_STATUS_COMPLETED = "COMPLETED"
        const val RESULT_STATUS_SKIPPED = "SKIPPED"
    }
}
