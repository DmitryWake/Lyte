package com.nikolaevskii.lyte.core.app.testing

import com.nikolaevskii.lyte.core.app.domain.repository.AppLaunchStateRepository

/**
 * Единственный фейк [AppLaunchStateRepository] в проекте: его используют тесты всех потребителей
 * контракта (сид библиотеки на сплэше, обучение).
 *
 * Лежит в `commonMain`, а не в `commonTest`, потому что общего фейка иначе не получится: Kotlin
 * Multiplatform 2.3 умеет `java-test-fixtures` только для JVM-таргета, а тестовые source set'ы между
 * модулями не публикует вовсе — потребители компилируются и под iOS. Второй фейк в каждом модуле
 * стоил бы дороже: копии одного контракта расходятся молча, а флаг первого запуска — тот, при
 * ошибке в котором библиотека упражнений засеивается повторно.
 *
 * Флаги независимы, как и в БД: [markOnboardingCompleted] не трогает [hasCompletedFirstLaunch] и
 * наоборот — фейк, который пишет состояние целиком, скрыл бы именно ту ошибку, ради которой запись
 * в DAO сделана точечной.
 */
class FakeAppLaunchStateRepository(
    private var hasCompletedFirstLaunch: Boolean = false,
    private var hasCompletedOnboarding: Boolean = false,
) : AppLaunchStateRepository {

    override suspend fun hasCompletedFirstLaunch(): Boolean = hasCompletedFirstLaunch

    override suspend fun markFirstLaunchCompleted() {
        hasCompletedFirstLaunch = true
    }

    override suspend fun hasCompletedOnboarding(): Boolean = hasCompletedOnboarding

    override suspend fun markOnboardingCompleted() {
        hasCompletedOnboarding = true
    }
}
