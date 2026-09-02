package com.nikolaevskii.lyte.core.session.domain.repository

import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity

/**
 * Владелец данных сессий тренировки (SSOT — локальная БД). Пишет прогресс трекинга и владеет активной
 * сессией (она переживает смерть процесса); историю как список отдаёт через
 * [SessionHistoryRepository]. Единственная реализация регистрируется в Koin под обоими интерфейсами.
 *
 * Одно чтение истории живёт всё-таки здесь — [getPreviousSetResults]: оно про активную сессию, а не
 * про список завершённых (обоснование — в KDoc метода).
 */
interface WorkoutSessionRepository : SessionHistoryRepository {

    /** Активная (незавершённая) сессия или `null`, если её нет. */
    suspend fun getActiveSession(): WorkoutSessionEntity?

    /**
     * Ориентир «в прошлый раз» для подходов [session]: что реально сделали в этом же подходе на
     * предыдущей тренировке по той же программе. Ключ карты — [SessionSetEntity.id] **переданной**
     * сессии, поэтому карта действительна только для неё; подход, у которого истории нет, в карте
     * отсутствует (пустого или нулевого ориентира нет — он хуже отсутствующего).
     *
     * Берётся последняя завершённая сессия, где подход **реально делали**: пропущенный не считается
     * ориентиром и поиск уходит глубже. Подход опознаётся по упражнению-библиотеке, номеру его
     * вхождения в программу и позиции внутри упражнения — правка программы между тренировками
     * ориентиры не теряет.
     *
     * Метод лежит здесь, а не на read-интерфейсе [SessionHistoryRepository], хотя формально это
     * чтение: он рассчитан на активную сессию (для завершённой «прошлый раз» — она сама), а
     * единственный потребитель — трекер. На историческом контракте он читался бы как пригодный для
     * любой сессии и потянул бы за собой реализацию в фейках истории, которые его не вызывают.
     */
    suspend fun getPreviousSetResults(session: WorkoutSessionEntity): Map<String, SessionSetValueEntity>

    /**
     * Создаёт сессию как снапшот программы (имена и цели копируются, факты пустые).
     *
     * @return id созданной сессии.
     * @throws IllegalStateException если активная сессия уже существует.
     */
    suspend fun startSession(workout: WorkoutEntity): String

    suspend fun completeSet(setId: String, count: Int, weight: Double?)

    suspend fun skipSet(setId: String)

    suspend fun saveSetNote(setId: String, note: String)

    suspend fun setCurrentExercise(sessionId: String, sessionExerciseId: String)

    /**
     * Завершает сессию: все ещё не выполненные подходы помечаются пропущенными, ставится время
     * завершения. Покрывает и обычное завершение (незакрытых подходов нет), и досрочное.
     */
    suspend fun finishSession(id: String)
}
