package com.nikolaevskii.lyte.core.db.session

/** Дата последней завершённой сессии по программе (`MAX(finished_at)` с группировкой по `program_id`). */
data class ProgramLastSessionRow(
    val programId: String,
    val finishedAt: Long,
)
