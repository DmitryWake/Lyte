package com.nikolaevskii.lyte.core.session.di

import com.nikolaevskii.lyte.core.session.data.repository.WorkoutSessionRepositoryImpl
import com.nikolaevskii.lyte.core.session.domain.repository.SessionHistoryRepository
import com.nikolaevskii.lyte.core.session.domain.repository.WorkoutSessionRepository
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Одна реализация под двумя интерфейсами: трекер инжектит [WorkoutSessionRepository] (запись),
 * история — [SessionHistoryRepository] (чтение). `WorkoutSessionDao` приходит из `coreDbModule()`,
 * `WorkoutRepository` (прогрессия плана по итогам сессии) — из `coreWorkoutModule()`.
 */
fun coreSessionModule(): Module = module {
    single<WorkoutSessionRepository> {
        WorkoutSessionRepositoryImpl(
            workoutSessionDao = get(),
            workoutRepository = get(),
            clock = Clock.System,
        )
    }
    single<SessionHistoryRepository> { get<WorkoutSessionRepository>() }
}
