package com.nikolaevskii.lyte.feature.splash.data.repository

import com.nikolaevskii.lyte.core.db.app.AppLaunchStateEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLaunchStateRepositoryImplTest {

    @Test
    fun hasCompletedFirstLaunchIsFalseWhenNoRowExistsYet() = runTest {
        val repository = repository()

        assertFalse(repository.hasCompletedFirstLaunch())
    }

    @Test
    fun markFirstLaunchCompletedPersistsTheFlag() = runTest {
        val repository = repository()

        repository.markFirstLaunchCompleted()

        assertTrue(repository.hasCompletedFirstLaunch())
    }

    @Test
    fun markFirstLaunchCompletedTwiceOverwritesTheSameSingletonRow() = runTest {
        val dao = FakeAppLaunchStateDao()
        val repository = AppLaunchStateRepositoryImpl(appLaunchStateDao = dao)

        repository.markFirstLaunchCompleted()
        repository.markFirstLaunchCompleted()

        assertTrue(repository.hasCompletedFirstLaunch())
        assertEquals(AppLaunchStateEntity.SINGLETON_ROW_ID, dao.get()?.id)
    }

    private fun repository(): AppLaunchStateRepositoryImpl =
        AppLaunchStateRepositoryImpl(appLaunchStateDao = FakeAppLaunchStateDao())
}
