package com.nikolaevskii.lyte.feature.splash.domain.initializer

/** Прогоняет все зарегистрированные [AppInitializer] последовательно, в порядке инъекции Koin. */
class AppInitializationManager(
    private val initializers: List<AppInitializer>,
) {
    suspend fun initialize() {
        initializers.forEach { initializer -> initializer.initialize() }
    }
}
