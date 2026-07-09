package com.nikolaevskii.lyte.core.db.app

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AppLaunchStateDao {

    @Query("SELECT * FROM app_launch_state WHERE id = ${AppLaunchStateEntity.SINGLETON_ROW_ID} LIMIT 1")
    suspend fun get(): AppLaunchStateEntity?

    @Upsert
    suspend fun upsert(state: AppLaunchStateEntity)
}
