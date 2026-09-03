package com.nikolaevskii.lyte.feature.onboarding.presentation.viewmodel

import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository

/** Общий фейк умеет только успех, а здесь проверяется именно провал записи флага. */
internal class FailingAppLaunchStateRepository : AppLaunchStateRepository {

    override suspend fun hasCompletedFirstLaunch(): Boolean = true

    override suspend fun markFirstLaunchCompleted() = Unit

    override suspend fun hasCompletedOnboarding(): Boolean = false

    override suspend fun markOnboardingCompleted(): Unit = throw IllegalStateException("БД недоступна")
}
