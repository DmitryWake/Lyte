package com.nikolaevskii.lyte.core.db.session

import androidx.room.ColumnInfo
import androidx.room.Embedded

/**
 * Подход завершённой сессии вместе с id самой сессии. Плоская проекция под список истории: она
 * забирает подходы **всех** завершённых сессий одним запросом, а потребитель раскладывает их по
 * [sessionId] — иначе на каждую карточку списка пришлось бы дотягивать граф сессии (N+1).
 *
 * Строка подхода embedded целиком, а не расписана по колонкам: маппер
 * [SessionSetDatabaseEntity] в доменную модель тогда переиспользуется как есть.
 */
data class FinishedSessionSetRow(
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @Embedded
    val set: SessionSetDatabaseEntity,
)
