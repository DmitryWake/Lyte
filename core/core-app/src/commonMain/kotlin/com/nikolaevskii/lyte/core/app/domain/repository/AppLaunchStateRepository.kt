package com.nikolaevskii.lyte.core.app.domain.repository

/**
 * Персистентные флаги старта приложения — не зависят от содержимого доменных таблиц: пользователь
 * мог очистить библиотеку упражнений, и это не делает запуск первым.
 *
 * Флаги независимы и пишутся разными сценариями, поэтому у каждого своя точка записи: сид библиотеки
 * на сплэше отмечает первый запуск, выход из обучения — обучение. Общей записи «сохрани состояние
 * целиком» здесь нет намеренно — она затирала бы чужой флаг.
 */
interface AppLaunchStateRepository {

    suspend fun hasCompletedFirstLaunch(): Boolean

    suspend fun markFirstLaunchCompleted()

    suspend fun hasCompletedOnboarding(): Boolean

    suspend fun markOnboardingCompleted()
}
