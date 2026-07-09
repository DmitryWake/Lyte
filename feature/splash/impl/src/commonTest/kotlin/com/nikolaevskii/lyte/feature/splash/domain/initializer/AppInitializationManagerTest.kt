package com.nikolaevskii.lyte.feature.splash.domain.initializer

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AppInitializationManagerTest {

    private class RecordingInitializer(
        private val log: MutableList<String>,
        private val name: String,
    ) : AppInitializer {
        override suspend fun initialize() {
            log += name
        }
    }

    @Test
    fun initializeRunsAllInitializersInOrder() = runTest {
        val log = mutableListOf<String>()
        val manager = AppInitializationManager(
            initializers = listOf(
                RecordingInitializer(log, "first"),
                RecordingInitializer(log, "second"),
            ),
        )

        manager.initialize()

        assertEquals(listOf("first", "second"), log)
    }

    @Test
    fun initializeWithNoInitializersCompletesWithoutError() = runTest {
        val manager = AppInitializationManager(initializers = emptyList())

        manager.initialize()
    }
}
