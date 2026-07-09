package com.nikolaevskii.lyte.feature.splash.data.initializer

import com.nikolaevskii.lyte.feature.splash.domain.repository.AppLaunchStateRepository

internal class FakeAppLaunchStateRepository(
    private var hasCompletedFirstLaunch: Boolean = false,
) : AppLaunchStateRepository {

    override suspend fun hasCompletedFirstLaunch(): Boolean = hasCompletedFirstLaunch

    override suspend fun markFirstLaunchCompleted() {
        hasCompletedFirstLaunch = true
    }
}
