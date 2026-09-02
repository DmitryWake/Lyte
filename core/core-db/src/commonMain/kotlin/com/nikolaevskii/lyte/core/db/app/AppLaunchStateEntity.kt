package com.nikolaevskii.lyte.core.db.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Единственная строка (`id = [SINGLETON_ROW_ID]`) — переживает удаление любых доменных данных пользователем.
 *
 * Флаги независимы и пишутся разными сценариями: [hasCompletedFirstLaunch] выставляет сид библиотеки
 * на сплэше, [hasCompletedOnboarding] — выход из обучения. Поэтому у сущности нет значений по
 * умолчанию: собрать её целиком и записать одним апсертом означало бы затереть чужой флаг соседнего
 * писателя. Запись — только точечными `UPDATE` из [AppLaunchStateDao].
 */
@Entity(tableName = "app_launch_state")
data class AppLaunchStateEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ROW_ID,
    @ColumnInfo(name = "has_completed_first_launch")
    val hasCompletedFirstLaunch: Boolean,
    @ColumnInfo(name = "has_completed_onboarding", defaultValue = "0")
    val hasCompletedOnboarding: Boolean,
) {

    companion object {
        const val SINGLETON_ROW_ID = 0
    }
}
