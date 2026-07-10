package com.nikolaevskii.lyte.core.db.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [isArchived] — soft delete: программа, на которую ссылаются сессии трекера, не удаляется физически,
 * а прячется из списков (иначе история потеряла бы ссылку на программу). См. [WorkoutDao.deleteOrArchiveWorkout].
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
)
