package com.nikolaevskii.lyte.core.app.data.repository

import com.nikolaevskii.lyte.core.db.app.AppLaunchStateDao
import com.nikolaevskii.lyte.core.db.app.AppLaunchStateEntity

/**
 * Наследуется от настоящего [AppLaunchStateDao] и переопределяет только его примитивы: `@Transaction`
 * методы (`markFirstLaunchCompleted`/`markOnboardingCompleted`) достаются из базового класса, то есть
 * фейк исполняет ту же связку «вставить строку при отсутствии → обновить свою колонку», что и Room.
 *
 * [insertIfAbsent] повторяет `OnConflictStrategy.IGNORE`: существующая строка не трогается. Замени
 * это на перезапись — и тест независимости флагов покраснеет, как и должен.
 */
internal class FakeAppLaunchStateDao : AppLaunchStateDao() {

    private var row: AppLaunchStateEntity? = null

    override suspend fun get(): AppLaunchStateEntity? = row

    override suspend fun insertIfAbsent(state: AppLaunchStateEntity) {
        if (row == null) {
            row = state
        }
    }

    override suspend fun setFirstLaunchCompleted() {
        row = row?.copy(hasCompletedFirstLaunch = true)
    }

    override suspend fun setOnboardingCompleted() {
        row = row?.copy(hasCompletedOnboarding = true)
    }
}
