package com.nikolaevskii.lyte.feature.history.presentation.model

import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/** Порог относительной даты: с этого числа дней карточка показывает обычную дату, а не «N дней назад». */
private const val RELATIVE_DATE_DAY_LIMIT = 7

/**
 * Маппит завершённые сессии в сгруппированный по месяцам список для экрана Истории (5.1). Чистая
 * функция без ресурсов: раскладывает [WorkoutSessionItemEntity.finishedAt] по локальному календарю
 * [timeZone], считает длительность как `finishedAt − startedAt`, переводит исходы подходов в тона
 * трека, а локализованные подписи (название месяца, единицы) оставляет UI-слою. Сессии — по
 * убыванию `finishedAt`; месяцы разных лет не сливаются (группировка по паре год+месяц).
 *
 * [today] приходит параметром, а не берётся из часов: маппер обязан оставаться чистым и
 * воспроизводимым в тестах.
 */
internal fun List<WorkoutSessionItemEntity>.toMonthGroups(
    timeZone: TimeZone,
    today: LocalDate,
): List<HistoryMonthGroupUiModel> =
    sortedByDescending { session -> session.finishedAt }
        .map { session -> session.toSessionUiModel(timeZone = timeZone, today = today) }
        .groupBy { session -> session.year to session.monthNumber }
        .map { (yearMonth, sessions) ->
            HistoryMonthGroupUiModel(year = yearMonth.first, monthNumber = yearMonth.second, sessions = sessions)
        }

private fun WorkoutSessionItemEntity.toSessionUiModel(timeZone: TimeZone, today: LocalDate): HistorySessionUiModel {
    val localDate = finishedAt.toLocalDateTime(timeZone).date
    return HistorySessionUiModel(
        id = id,
        programName = program.name,
        year = localDate.year,
        // Month.ordinal 0-based (JANUARY == 0) — версионно-стабильно в отличие от monthNumber/month.number.
        monthNumber = localDate.month.ordinal + 1,
        dayOfMonth = localDate.day,
        daysAgo = localDate.daysUntil(today).takeIf { days -> days in 0 until RELATIVE_DATE_DAY_LIMIT },
        durationMinutes = (finishedAt - startedAt).inWholeMinutes.toInt().coerceAtLeast(0),
        setTones = setOutcomes.map { outcome -> outcome.toProgressTone() },
        accent = program.accent.toLyteAccent(),
        glyph = program.glyph.toLyteGlyph(),
    )
}
