package com.nikolaevskii.lyte.feature.workout.data

import com.nikolaevskii.lyte.core.db.workout.WorkoutDao
import com.nikolaevskii.lyte.core.db.workout.WorkoutEntity
import com.nikolaevskii.lyte.feature.workout.domain.Workout
import com.nikolaevskii.lyte.feature.workout.domain.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WorkoutRepositoryImpl(
    private val dao: WorkoutDao,
) : WorkoutRepository {

    override fun observeAll(): Flow<List<Workout>> =
        dao.observeAll().map { entities -> entities.map(WorkoutEntity::toDomain) }

    override fun observeById(id: Long): Flow<Workout?> =
        dao.observeById(id).map { entity -> entity?.toDomain() }

    override suspend fun seedIfEmpty() {
        if (dao.count() == 0L) {
            val now = SEED_BASE_TIMESTAMP_MS
            dao.upsertAll(
                items = SEED_NAMES.mapIndexed { index, name ->
                    WorkoutEntity(
                        name = name,
                        startedAt = now + index,
                    )
                },
            )
        }
    }

    private companion object {
        const val SEED_BASE_TIMESTAMP_MS: Long = 1_700_000_000_000L

        val SEED_NAMES: List<String> = listOf(
            "Утренняя пробежка",
            "Силовая тренировка",
            "Растяжка",
        )
    }
}
