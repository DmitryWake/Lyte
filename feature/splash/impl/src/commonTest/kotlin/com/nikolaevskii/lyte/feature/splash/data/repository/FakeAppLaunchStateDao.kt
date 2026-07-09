package com.nikolaevskii.lyte.feature.splash.data.repository

import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateEntity

internal class FakeAppLaunchStateDao : AppLaunchStateDao {

    private val rows = mutableMapOf<Int, AppLaunchStateEntity>()

    override suspend fun get(): AppLaunchStateEntity? = rows[AppLaunchStateEntity.SINGLETON_ROW_ID]

    override suspend fun upsert(state: AppLaunchStateEntity) {
        rows[state.id] = state
    }
}
