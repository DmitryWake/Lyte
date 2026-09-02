package com.nikolaevskii.lyte.core.app.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLaunchStateRepositoryImplTest {

    @Test
    fun flagsAreFalseWhenNoRowExistsYet() = runTest {
        val repository = repository()

        assertFalse(repository.hasCompletedFirstLaunch())
        assertFalse(repository.hasCompletedOnboarding())
    }

    @Test
    fun markFirstLaunchCompletedPersistsTheFlag() = runTest {
        val repository = repository()

        repository.markFirstLaunchCompleted()

        assertTrue(repository.hasCompletedFirstLaunch())
    }

    @Test
    fun markOnboardingCompletedPersistsTheFlag() = runTest {
        val repository = repository()

        repository.markOnboardingCompleted()

        assertTrue(repository.hasCompletedOnboarding())
    }

    @Test
    fun markOnboardingCompletedKeepsFirstLaunchFlag() = runTest {
        // Ради этого репозиторий и переехал в свой модуль: запись строки целиком обнулила бы флаг
        // первого запуска, и сид библиотеки засеял бы её повторно, продублировав всё содержимое.
        val repository = repository()
        repository.markFirstLaunchCompleted()

        repository.markOnboardingCompleted()

        assertTrue(repository.hasCompletedFirstLaunch())
        assertTrue(repository.hasCompletedOnboarding())
    }

    @Test
    fun markFirstLaunchCompletedKeepsOnboardingFlag() = runTest {
        val repository = repository()
        repository.markOnboardingCompleted()

        repository.markFirstLaunchCompleted()

        assertTrue(repository.hasCompletedOnboarding())
        assertTrue(repository.hasCompletedFirstLaunch())
    }

    private fun repository(): AppLaunchStateRepositoryImpl =
        AppLaunchStateRepositoryImpl(appLaunchStateDao = FakeAppLaunchStateDao())
}
