package com.nikolaevskii.lyte.core.db.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Единственная строка (`id = [SINGLETON_ROW_ID]`) — переживает удаление любых доменных данных пользователем. */
@Entity(tableName = "app_launch_state")
data class AppLaunchStateEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ROW_ID,
    @ColumnInfo(name = "has_completed_first_launch")
    val hasCompletedFirstLaunch: Boolean,
) {

    companion object {
        const val SINGLETON_ROW_ID = 0
    }
}
