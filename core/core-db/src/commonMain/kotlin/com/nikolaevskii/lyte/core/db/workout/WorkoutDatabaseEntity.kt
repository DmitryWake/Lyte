package com.nikolaevskii.lyte.core.db.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nikolaevskii.lyte.core.db.DEFAULT_ACCENT_SQL
import com.nikolaevskii.lyte.core.db.DEFAULT_GLYPH_SQL

/**
 * [isArchived] — soft delete: программа, на которую ссылаются сессии трекера, не удаляется физически,
 * а прячется из списков (иначе история потеряла бы ссылку на программу). См. [WorkoutDao.deleteOrArchiveWorkout].
 *
 * [accent]/[glyph] — маркер программы. Свой, а не выведенный из первого упражнения: программу
 * выбирают из списка по цвету и знаку, и он не должен меняться при правке состава упражнений.
 * Формат хранения — как у [ExerciseDatabaseEntity.accent].
 */
@Entity(tableName = "workout")
data class WorkoutDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,
    @ColumnInfo(name = "accent", defaultValue = DEFAULT_ACCENT_SQL)
    val accent: String,
    @ColumnInfo(name = "glyph", defaultValue = DEFAULT_GLYPH_SQL)
    val glyph: String,
)
