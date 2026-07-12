package com.nikolaevskii.lyte.feature.history

import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionProgramEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionItemEntity
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** Фиксированная зона тестов — чтобы разложение даты было детерминированным. */
internal val TEST_TIME_ZONE: TimeZone = TimeZone.UTC

/**
 * Собирает завершённую сессию с моментом окончания [finishedAt] (в [timeZone]) и стартом на
 * [durationMinutes] раньше — так `finishedAt − startedAt` даёт ровно [durationMinutes].
 */
internal fun finishedSession(
    id: String,
    programName: String,
    finishedAt: LocalDateTime,
    durationMinutes: Int,
    completedSetCount: Int,
    totalSetCount: Int,
    timeZone: TimeZone = TEST_TIME_ZONE,
): WorkoutSessionItemEntity {
    val finished = finishedAt.toInstant(timeZone)
    return WorkoutSessionItemEntity(
        id = id,
        program = SessionProgramEntity(id = "prog-$id", name = programName),
        startedAt = finished - durationMinutes.minutes,
        finishedAt = finished,
        completedSetCount = completedSetCount,
        totalSetCount = totalSetCount,
    )
}
