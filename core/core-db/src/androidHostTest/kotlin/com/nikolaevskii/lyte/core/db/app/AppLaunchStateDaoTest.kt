package com.nikolaevskii.lyte.core.db.app

import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.nikolaevskii.lyte.core.db.LyteDatabase
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Независимость флагов `app_launch_state` на настоящей SQLite. Фейк DAO этого доказать не может:
 * убери из него точечный `UPDATE` и верни запись строки целиком — фейк останется зелёным, а в
 * приложении второй писатель обнулит `has_completed_first_launch`, и сид библиотеки на следующем
 * запуске засеет её повторно, продублировав все упражнения и программы.
 *
 * БД поднимается **настоящим** Room-билдером in-memory: проверять надо тот же сгенерированный код,
 * который работает в приложении. Про `AndroidSQLiteDriver` под Robolectric вместо
 * `BundledSQLiteDriver` — см. `WorkoutSessionDaoProgramHistoryTest`.
 */
@RunWith(RobolectricTestRunner::class)
class AppLaunchStateDaoTest {

    @Test
    fun freshInstallHasNoRow() = withDao { dao ->
        assertNull(dao.get(), "Отсутствие строки — и есть «ничего ещё не проходили»")
    }

    @Test
    fun markingOnboardingKeepsFirstLaunchFlag() = withDao { dao ->
        dao.markFirstLaunchCompleted()

        dao.markOnboardingCompleted()

        val state = assertNotNull(dao.get())
        assertTrue(state.hasCompletedFirstLaunch, "Флаг первого запуска сброшен записью обучения")
        assertTrue(state.hasCompletedOnboarding)
    }

    @Test
    fun markingFirstLaunchKeepsOnboardingFlag() = withDao { dao ->
        dao.markOnboardingCompleted()

        dao.markFirstLaunchCompleted()

        val state = assertNotNull(dao.get())
        assertTrue(state.hasCompletedOnboarding, "Флаг обучения сброшен записью первого запуска")
        assertTrue(state.hasCompletedFirstLaunch)
    }

    @Test
    fun repeatedWritesStayOnTheSingletonRow() = withDao { dao ->
        dao.markFirstLaunchCompleted()
        dao.markOnboardingCompleted()
        dao.markFirstLaunchCompleted()

        val state = assertNotNull(dao.get())
        assertEquals(expected = AppLaunchStateEntity.SINGLETON_ROW_ID, actual = state.id)
        assertTrue(state.hasCompletedFirstLaunch)
        assertTrue(state.hasCompletedOnboarding)
    }

    /**
     * Поднимает БД в памяти и отдаёт DAO тесту. Закрывается в любом случае — иначе упавший тест
     * утащил бы за собой соседей.
     */
    private fun withDao(block: suspend (AppLaunchStateDao) -> Unit) = runTest {
        val database = Room
            .inMemoryDatabaseBuilder<LyteDatabase>(context = RuntimeEnvironment.getApplication())
            .setDriver(AndroidSQLiteDriver())
            .build()
        try {
            block(database.appLaunchStateDao())
        } finally {
            database.close()
        }
    }
}
