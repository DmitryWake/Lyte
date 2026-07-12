package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionItemEntity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Маппит завершённые сессии в сгруппированный по месяцам список для экрана Истории (5.1). Чистая
 * функция без ресурсов: раскладывает [WorkoutSessionItemEntity.finishedAt] по локальному календарю
 * [timeZone], считает длительность как `finishedAt − startedAt`, а локализованные подписи (название
 * месяца, единицы) оставляет UI-слою. Сессии — по убыванию `finishedAt`; месяцы разных лет не
 * сливаются (группировка по паре год+месяц).
 */
internal fun List<WorkoutSessionItemEntity>.toMonthGroups(timeZone: TimeZone): List<HistoryMonthGroupUiModel> =
    sortedByDescending { session -> session.finishedAt }
        .map { session -> session.toSessionUiModel(timeZone) }
        .groupBy { session -> session.year to session.monthNumber }
        .map { (yearMonth, sessions) ->
            HistoryMonthGroupUiModel(year = yearMonth.first, monthNumber = yearMonth.second, sessions = sessions)
        }

private fun WorkoutSessionItemEntity.toSessionUiModel(timeZone: TimeZone): HistorySessionUiModel {
    val localDate = finishedAt.toLocalDateTime(timeZone).date
    return HistorySessionUiModel(
        id = id,
        programName = program.name,
        year = localDate.year,
        // Month.ordinal 0-based (JANUARY == 0) — версионно-стабильно в отличие от monthNumber/month.number.
        monthNumber = localDate.month.ordinal + 1,
        dayOfMonth = localDate.day,
        durationMinutes = (finishedAt - startedAt).inWholeMinutes.toInt().coerceAtLeast(0),
        completedSetCount = completedSetCount,
        totalSetCount = totalSetCount,
    )
}
