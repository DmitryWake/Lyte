package com.nikolaevskii.lyte.core.app.di

import com.nikolaevskii.lyte.core.app.data.repository.AppLaunchStateRepositoryImpl
import com.nikolaevskii.lyte.core.app.data.repository.FakeAppLaunchStateDao
import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository
import com.nikolaevskii.lyte.core.app.testing.FakeAppLaunchStateRepository
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * [FakeAppLaunchStateRepository] лежит в `commonMain` вынужденно (KMP не публикует тестовые source
 * set'ы между модулями), а значит виден и продакшен-коду: его можно связать в [coreAppModule] по
 * опечатке, и ни один тест потребителя этого не заметит — они и так работают с фейком. Цена ошибки
 * несоразмерна её заметности: `hasCompletedFirstLaunch()` начинает всегда возвращать `false`, и
 * библиотека упражнений засеивается заново при каждом запуске.
 */
class CoreAppModuleTest {

    private val koin = koinApplication {
        modules(
            coreAppModule(),
            module { single<AppLaunchStateDao> { FakeAppLaunchStateDao() } },
        )
    }

    @AfterTest
    fun tearDown() {
        koin.close()
    }

    @Test
    fun bindsRepositoryBackedByDatabaseRatherThanTestDouble() {
        val repository = koin.koin.get<AppLaunchStateRepository>()

        assertTrue(
            actual = repository is AppLaunchStateRepositoryImpl,
            message = "coreAppModule() обязан отдавать репозиторий поверх БД, а получил " +
                "${repository::class.simpleName}",
        )
    }
}
