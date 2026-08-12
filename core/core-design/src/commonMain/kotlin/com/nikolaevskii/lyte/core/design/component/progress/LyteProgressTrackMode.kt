package com.nikolaevskii.lyte.core.design.component.progress

import com.nikolaevskii.lyte.core.design.theme.LyteAccent

/**
 * Что именно показывает [LyteProgressTrack]. Вынесено в sealed-тип, чтобы исключить бессмысленные
 * сочетания: у плана не бывает выполненных подходов, у списка исходов — акцента, а у прогресса —
 * тонов. Веб-версия принимала все поля разом и разбиралась в них по месту.
 */
sealed interface LyteProgressTrackMode {

    /**
     * Как прошла сессия: по сегменту на подход, высота — исход относительно цели ([tones]).
     * Сводка завершённой тренировки — карточка истории, экран итога, детали сессии.
     */
    data class Tones(val tones: List<LyteProgressTone>) : LyteProgressTrackMode

    /**
     * Что запланировано и ещё не начиналось: [total] одинаковых сегментов в цвете упражнения
     * ([accent]). Тон приглушён подмешиванием фона, поэтому светлые акценты не пропадают.
     */
    data class Plan(val total: Int, val accent: LyteAccent) : LyteProgressTrackMode

    /**
     * Сколько подходов позади: [done] закрашенных из [total], остальные пустые. Индексы из [missed]
     * рисуются полой обводкой — подход был, но цель не взята.
     */
    data class Progress(
        val total: Int,
        val done: Int,
        val missed: Set<Int> = emptySet(),
    ) : LyteProgressTrackMode
}
