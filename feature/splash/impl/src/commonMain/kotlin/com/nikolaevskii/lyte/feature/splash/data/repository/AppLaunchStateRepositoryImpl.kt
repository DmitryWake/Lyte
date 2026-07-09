package com.nikolaevskii.lyte.feature.splash.data.repository

import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateEntity
import com.nikolaevskii.lyte.feature.splash.domain.repository.AppLaunchStateRepository

internal class AppLaunchStateRepositoryImpl(
    private val appLaunchStateDao: AppLaunchStateDao,
) : AppLaunchStateRepository {

    override suspend fun hasCompletedFirstLaunch(): Boolean =
        appLaunchStateDao.get()?.hasCompletedFirstLaunch == true

    override suspend fun markFirstLaunchCompleted() {
        appLaunchStateDao.upsert(AppLaunchStateEntity(hasCompletedFirstLaunch = true))
    }
}
