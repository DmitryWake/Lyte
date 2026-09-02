package com.nikolaevskii.lyte.core.app.data.repository

import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao

internal class AppLaunchStateRepositoryImpl(
    private val appLaunchStateDao: AppLaunchStateDao,
) : AppLaunchStateRepository {

    override suspend fun hasCompletedFirstLaunch(): Boolean =
        appLaunchStateDao.get()?.hasCompletedFirstLaunch == true

    override suspend fun markFirstLaunchCompleted() {
        appLaunchStateDao.markFirstLaunchCompleted()
    }

    override suspend fun hasCompletedOnboarding(): Boolean =
        appLaunchStateDao.get()?.hasCompletedOnboarding == true

    override suspend fun markOnboardingCompleted() {
        appLaunchStateDao.markOnboardingCompleted()
    }
}
