package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.design.format.formatWeight
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDiffTone
import com.nikolaevskii.lyte.core.session.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.util.hasWeight
import com.nikolaevskii.lyte.core.session.domain.util.outcome
import kotlin.time.Duration
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Маппит доменную сессию в готовую к отрисовке [HistorySessionDetailsUiModel] (5.2): дата/время начала
 * по локальному календарю [timeZone], длительность как `finishedAt − startedAt`, счётчики подходов и
 * группы упражнений с диффом «план→факт». Тоны — из единой точки правды [outcome]. Чистая функция без
 * ресурсов: локализованные подписи (месяц, единицы) подставляет UI-слой.
 */
internal fun WorkoutSessionEntity.toDetailsUiModel(timeZone: TimeZone): HistorySessionDetailsUiModel {
    val startLocal = startedAt.toLocalDateTime(timeZone)
    val allSets = exercises.flatMap { exercise -> exercise.sets }
    val duration = finishedAt?.let { finished -> finished - startedAt } ?: Duration.ZERO
    return HistorySessionDetailsUiModel(
        programName = program.name,
        year = startLocal.year,
        // Month.ordinal 0-based (JANUARY == 0) — версионно-стабильно в отличие от monthNumber/month.number.
        monthNumber = startLocal.month.ordinal + 1,
        dayOfMonth = startLocal.day,
        startHour = startLocal.hour,
        startMinute = startLocal.minute,
        durationMinutes = duration.inWholeMinutes.toInt().coerceAtLeast(0),
        completedSetCount = allSets.count { set -> set.result is SessionSetResultEntity.Completed },
        totalSetCount = allSets.size,
        exercises = exercises.map { exercise -> exercise.toGroupUiModel() },
    )
}

private fun SessionExerciseEntity.toGroupUiModel(): HistoryExerciseGroupUiModel =
    HistoryExerciseGroupUiModel(
        exerciseId = id,
        exerciseName = exercise.name,
        rows = sets.mapIndexed { index, set -> set.toDiffRowUiModel(index + 1) },
    )

private fun SessionSetEntity.toDiffRowUiModel(number: Int): HistoryDiffRowUiModel =
    HistoryDiffRowUiModel(
        id = id,
        index = number,
        tone = outcome().toDiffTone(),
        target = target.toSetValueUiModel(),
        actual = (result as? SessionSetResultEntity.Completed)?.actual?.toSetValueUiModel(),
        note = note.takeIf { text -> text.isNotBlank() },
    )

private fun SessionSetOutcomeEntity?.toDiffTone(): LyteDiffTone = when (this) {
    SessionSetOutcomeEntity.MET -> LyteDiffTone.Met
    SessionSetOutcomeEntity.EXCEEDED -> LyteDiffTone.Positive
    SessionSetOutcomeEntity.MISSED -> LyteDiffTone.Negative
    // SKIPPED и невыполненный (null, в завершённой сессии не встречается) — «пропущено».
    SessionSetOutcomeEntity.SKIPPED, null -> LyteDiffTone.Skipped
}

private fun SessionSetValueEntity.toSetValueUiModel(): HistorySetValueUiModel =
    if (hasWeight) {
        HistorySetValueUiModel.Weighted(reps = count, weight = formatWeight(checkNotNull(weight)))
    } else {
        HistorySetValueUiModel.Bodyweight(reps = count)
    }
