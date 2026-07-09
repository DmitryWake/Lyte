package com.nikolaevskii.lyte.feature.splash.domain.initializer

/**
 * Один процесс, который должен выполниться на старте приложения (до показа основного UI).
 * Новый стартовый процесс — новая реализация, зарегистрированная в Koin как [AppInitializer].
 */
interface AppInitializer {
    suspend fun initialize()
}
