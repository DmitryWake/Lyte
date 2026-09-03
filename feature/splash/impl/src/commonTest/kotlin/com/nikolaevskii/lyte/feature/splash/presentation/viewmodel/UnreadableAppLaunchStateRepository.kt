package com.nikolaevskii.lyte.feature.splash.presentation.viewmodel

import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository

/** Общий фейк умеет только успех, а здесь проверяется именно недоступное чтение флага. */
internal class UnreadableAppLaunchStateRepository : AppLaunchStateRepository {

    override suspend fun hasCompletedFirstLaunch(): Boolean = true

    override suspend fun markFirstLaunchCompleted() = Unit

    override suspend fun hasCompletedOnboarding(): Boolean = throw IllegalStateException("БД недоступна")

    override suspend fun markOnboardingCompleted() = Unit
}
