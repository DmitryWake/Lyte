package com.nikolaevskii.lyte.core.db.session

/**
 * Лёгкая проекция завершённой сессии для списка истории — без графа упражнений/подходов,
 * только агрегаты: сколько всего подходов и сколько из них выполнено ([SessionSetDatabaseEntity]
 * с `result_status = 'COMPLETED'`). Маркер программы берётся из снапшота сессии, а не из `workout`:
 * карточка истории переживает переименование, перекраску и удаление программы.
 */
data class SessionItemWithSetCounts(
    val id: String,
    val programId: String,
    val programName: String,
    val programAccent: String,
    val programGlyph: String,
    val startedAt: Long,
    val finishedAt: Long,
    val totalSetCount: Int,
    val completedSetCount: Int,
)
