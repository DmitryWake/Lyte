package com.nikolaevskii.lyte.core.db.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

/**
 * Флаги старта приложения. У строки несколько независимых писателей, поэтому **записи только
 * точечные**: каждый сценарий трогает свою колонку.
 *
 * Апсерта строки целиком здесь нет намеренно. Он собирал бы сущность из значений, которые писатель
 * знает (свой флаг) и не знает (чужой), и вторым вызовом обнулял бы чужой: обнулённый
 * `has_completed_first_launch` заставил бы сид библиотеки на следующем запуске засеять её повторно,
 * продублировав все упражнения и программы.
 */
@Dao
abstract class AppLaunchStateDao {

    @Query("SELECT * FROM app_launch_state WHERE id = ${AppLaunchStateEntity.SINGLETON_ROW_ID} LIMIT 1")
    abstract suspend fun get(): AppLaunchStateEntity?

    /**
     * Заводит строку с обоими флагами «не пройдено», если её ещё нет. `IGNORE` — чтобы уже
     * записанные флаги остались нетронутыми: это подготовка `UPDATE`, а не запись состояния.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(state: AppLaunchStateEntity)

    @Query(
        "UPDATE app_launch_state SET has_completed_first_launch = 1 " +
            "WHERE id = ${AppLaunchStateEntity.SINGLETON_ROW_ID}",
    )
    abstract suspend fun setFirstLaunchCompleted()

    @Query(
        "UPDATE app_launch_state SET has_completed_onboarding = 1 " +
            "WHERE id = ${AppLaunchStateEntity.SINGLETON_ROW_ID}",
    )
    abstract suspend fun setOnboardingCompleted()

    /**
     * Одной транзакцией: `UPDATE` по отсутствующей строке меняет ноль строк и флаг молча потерялся бы,
     * а вставка и обновление порознь оставили бы окно, в котором соседний писатель видит строку с
     * обнулёнными флагами.
     */
    @Transaction
    open suspend fun markFirstLaunchCompleted() {
        insertIfAbsent(NOT_COMPLETED_STATE)
        setFirstLaunchCompleted()
    }

    /** См. [markFirstLaunchCompleted] — та же схема «вставить при отсутствии, затем обновить свою колонку». */
    @Transaction
    open suspend fun markOnboardingCompleted() {
        insertIfAbsent(NOT_COMPLETED_STATE)
        setOnboardingCompleted()
    }

    private companion object {

        val NOT_COMPLETED_STATE = AppLaunchStateEntity(
            hasCompletedFirstLaunch = false,
            hasCompletedOnboarding = false,
        )
    }
}
