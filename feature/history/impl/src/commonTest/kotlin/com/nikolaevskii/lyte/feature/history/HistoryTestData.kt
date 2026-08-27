package com.nikolaevskii.lyte.feature.history

import com.nikolaevskii.lyte.core.session.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionProgramEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import kotlin.time.Duration.Companion.minutes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/** Фиксированная зона тестов — чтобы разложение даты было детерминированным. */
internal val TEST_TIME_ZONE: TimeZone = TimeZone.UTC

/**
 * «Сегодня» по умолчанию — заведомо позже всех фикстур, поэтому относительной даты у них нет и тесты
 * группировки не зависят от неё. Тесты самой относительной даты передают своё значение.
 */
internal val TEST_TODAY: LocalDate = LocalDate(2026, Month.AUGUST, 20)

/**
 * Собирает завершённую сессию с моментом окончания [finishedAt] (в [timeZone]) и стартом на
 * [durationMinutes] раньше — так `finishedAt − startedAt` даёт ровно [durationMinutes].
 */
internal fun finishedSession(
    id: String,
    programName: String,
    finishedAt: LocalDateTime,
    durationMinutes: Int,
    setOutcomes: List<SessionSetOutcomeEntity?> = emptyList(),
    accent: ExerciseAccent = ExerciseAccent.Default,
    glyph: ExerciseGlyph = ExerciseGlyph.Default,
    timeZone: TimeZone = TEST_TIME_ZONE,
): WorkoutSessionItemEntity {
    val finished = finishedAt.toInstant(timeZone)
    return WorkoutSessionItemEntity(
        id = id,
        program = SessionProgramEntity(id = "prog-$id", name = programName, accent = accent, glyph = glyph),
        startedAt = finished - durationMinutes.minutes,
        finishedAt = finished,
        setOutcomes = setOutcomes,
    )
}

/**
 * Полный граф завершённой сессии для деталей (5.2): старт [startedAt] (в [timeZone]), финиш —
 * на [durationMinutes] позже.
 */
internal fun finishedSessionEntity(
    id: String = "session-1",
    programName: String = "Push Day",
    startedAt: LocalDateTime = LocalDateTime(2026, Month.JULY, 2, 18, 24),
    durationMinutes: Int = 52,
    exercises: List<SessionExerciseEntity>,
    timeZone: TimeZone = TEST_TIME_ZONE,
): WorkoutSessionEntity {
    val start = startedAt.toInstant(timeZone)
    return WorkoutSessionEntity(
        id = id,
        program = SessionProgramEntity(id = "prog-$id", name = programName),
        startedAt = start,
        finishedAt = start + durationMinutes.minutes,
        currentExerciseId = null,
        exercises = exercises,
    )
}

internal fun sessionExercise(id: String, name: String, sets: List<SessionSetEntity>): SessionExerciseEntity =
    SessionExerciseEntity(id = id, exercise = WorkoutExerciseEntity(id = "ex-$id", name = name), sets = sets)

internal fun sessionSet(
    id: String,
    targetCount: Int,
    targetWeight: Double?,
    result: SessionSetResultEntity?,
    note: String = "",
): SessionSetEntity = SessionSetEntity(
    id = id,
    target = SessionSetValueEntity(count = targetCount, weight = targetWeight),
    result = result,
    note = note,
)

internal fun completed(count: Int, weight: Double?): SessionSetResultEntity.Completed =
    SessionSetResultEntity.Completed(SessionSetValueEntity(count = count, weight = weight))
