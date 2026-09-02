package com.nikolaevskii.lyte.core.db.session

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Подход завершённой сессии вместе с id упражнения, в котором он стоял. Плоская проекция под
 * ориентир «в прошлый раз»: подходы всех завершённых сессий **одной программы** приезжают одним
 * запросом, а сопоставляет их с текущей сессией потребитель (`:core:core-session`).
 *
 * Отдельно от [FinishedSessionSetRow], а не расширением: тому нужен только [sessionId] — список
 * истории раскладывает подходы по сессиям и в упражнение не заглядывает.
 *
 * Соседние вхождения одного упражнения (движение можно поставить в программу дважды) различает
 * `session_exercise_id` внутри [set] — он уникален по определению. Позиция упражнения в проекцию не
 * берётся: уникальность пары `(session_id, position)` схемой не гарантирована, а для порядка строк
 * достаточно `ORDER BY` в самом запросе.
 */
data class ProgramSetHistoryRow(
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,
    @Embedded
    val set: SessionSetDatabaseEntity,
)
