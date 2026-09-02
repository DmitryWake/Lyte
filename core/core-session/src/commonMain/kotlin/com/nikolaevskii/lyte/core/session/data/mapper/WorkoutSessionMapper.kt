package com.nikolaevskii.lyte.core.session.data.mapper

import com.nikolaevskii.lyte.core.db.session.FinishedSessionSetRow
import com.nikolaevskii.lyte.core.db.session.ProgramSetHistoryRow
import com.nikolaevskii.lyte.core.db.session.SessionExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionExerciseWithSets
import com.nikolaevskii.lyte.core.db.session.SessionSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.session.SessionWithExercises
import com.nikolaevskii.lyte.core.db.session.WorkoutSessionDatabaseEntity
import com.nikolaevskii.lyte.core.session.data.model.SessionRowsModel
import com.nikolaevskii.lyte.core.session.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionProgramEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionItemEntity
import com.nikolaevskii.lyte.core.session.domain.util.outcome
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import kotlin.time.Instant

/** Разделитель сегментов синтетических id упражнений/подходов сессии. */
private const val ID_SEGMENT_SEPARATOR: String = "#"

/**
 * Строит граф строк сессии — снапшот программы: имена и цели копируются, факты пустые
 * ([SessionSetDatabaseEntity.resultStatus] `= null`), заметки пустые.
 */
internal fun WorkoutEntity.toSessionRows(sessionId: String, startedAt: Instant): SessionRowsModel {
    val sessionRow = WorkoutSessionDatabaseEntity(
        id = sessionId,
        programId = id,
        programName = name,
        programAccent = accent.key,
        programGlyph = glyph.key,
        startedAt = startedAt.toEpochMilliseconds(),
        finishedAt = null,
        currentExerciseId = null,
    )
    val exerciseRows = mutableListOf<SessionExerciseDatabaseEntity>()
    val setRows = mutableListOf<SessionSetDatabaseEntity>()

    exercises.forEachIndexed { exerciseIndex, exerciseWithReps ->
        val sessionExerciseId = "$sessionId$ID_SEGMENT_SEPARATOR$exerciseIndex"

        // Имя/описание не копируются: session_exercise ссылается на живое упражнение по exercise_id.
        exerciseRows += SessionExerciseDatabaseEntity(
            id = sessionExerciseId,
            sessionId = sessionId,
            exerciseId = exerciseWithReps.exercise.id,
            position = exerciseIndex,
        )
        exerciseWithReps.reps.forEachIndexed { repIndex, rep ->
            setRows += SessionSetDatabaseEntity(
                id = "$sessionExerciseId$ID_SEGMENT_SEPARATOR$repIndex",
                sessionExerciseId = sessionExerciseId,
                position = repIndex,
                targetCount = rep.count,
                targetWeight = rep.weight,
                resultStatus = null,
                resultCount = null,
                resultWeight = null,
                note = "",
            )
        }
    }

    return SessionRowsModel(
        session = sessionRow,
        exercises = exerciseRows,
        sets = setRows,
    )
}

internal fun SessionWithExercises.toDomainEntity(): WorkoutSessionEntity =
    WorkoutSessionEntity(
        id = session.id,
        program = session.toProgramEntity(),
        startedAt = Instant.fromEpochMilliseconds(session.startedAt),
        finishedAt = session.finishedAt?.let(Instant::fromEpochMilliseconds),
        currentExerciseId = session.currentExerciseId,
        exercises = exercises
            .sortedBy { it.sessionExercise.position }
            .map { it.toDomainEntity() },
    )

/**
 * Строка завершённой сессии в модель списка истории. [setOutcomes] приходят снаружи — их считает
 * [toOutcomesBySession] по отдельному запросу подходов, чтобы не тянуть граф на каждую карточку.
 *
 * `null` — сессия не завершена. Запрос списка такие строки не отдаёт, так что ветка холостая;
 * подставлять вместо даты завершения ноль или «сейчас» было бы хуже — история показала бы выдумку.
 */
internal fun WorkoutSessionDatabaseEntity.toItemEntity(
    setOutcomes: List<SessionSetOutcomeEntity?>,
): WorkoutSessionItemEntity? {
    val finishedAt = finishedAt ?: return null
    return WorkoutSessionItemEntity(
        id = id,
        program = toProgramEntity(),
        startedAt = Instant.fromEpochMilliseconds(startedAt),
        finishedAt = Instant.fromEpochMilliseconds(finishedAt),
        setOutcomes = setOutcomes,
    )
}

/**
 * Исходы подходов всех завершённых сессий, разложенные по id сессии. Порядок внутри списка —
 * порядок строк запроса (упражнение → подход), пересортировки здесь нет.
 */
internal fun List<FinishedSessionSetRow>.toOutcomesBySession(): Map<String, List<SessionSetOutcomeEntity?>> =
    groupBy { row -> row.sessionId }
        .mapValues { (_, rows) -> rows.map { row -> row.set.toDomainEntity().outcome() } }

/**
 * Факты предыдущих сессий, разложенные по id подходов [session]: «в прошлый раз здесь сделали вот
 * столько». Источник строк — `getProgramSetHistory`: подходы завершённых сессий той же программы,
 * свежие сессии первыми.
 *
 * Подход сопоставляется по [ProgramSetCoordinate], а не по позиции упражнения: программу могли
 * отредактировать между сессиями, и упражнение, вставленное в начало, сдвинуло бы все ориентиры.
 *
 * Берётся **первое** совпадение, то есть последняя сессия, где подход реально делали: пропущенные в
 * карту не попадают, и поиск уходит глубже (у досрочно завершённой сессии пропущенные есть всегда).
 * Совпадения нет — нет и записи в карте: подставлять ноль или цель значило бы выдумать ориентир.
 *
 * Известная граница плоского запроса: упражнение, у которого в прошлой сессии не было ни одного
 * подхода, строк не даёт и в нумерации вхождений той сессии не участвует, а текущая сессия нумерует
 * все свои упражнения. Если одно движение стоит в программе дважды и у раннего вхождения подходов
 * не было, нумерация сторон разойдётся, и второму вхождению приедет **чужой** факт — не только
 * «ориентира нет». Достижимо: редактор позволяет сохранить упражнение с пустым списком подходов.
 * Лечится `LEFT JOIN session_set` в запросе (бесподходное упражнение сохранит место в нумерации)
 * либо запретом сохранять упражнение без подходов; и то и другое — за «Объёмом» RD-26, находка
 * записана в долг роадмапа.
 */
internal fun List<ProgramSetHistoryRow>.toPreviousSetResults(
    session: WorkoutSessionEntity,
): Map<String, SessionSetValueEntity> {
    val factsByCoordinate = mutableMapOf<ProgramSetCoordinate, SessionSetValueEntity>()
    groupBy { row -> row.sessionId }.values.forEach { sessionRows ->
        val occurrences = mutableMapOf<String, Int>()
        // Группируем по id строки session_exercise: он уникален по определению, в отличие от
        // позиции — уникальность пары (session_id, position) схемой не гарантирована. Порядок групп
        // и строк внутри задал ORDER BY запроса.
        sessionRows.groupBy { row -> row.set.sessionExerciseId }.values.forEach { exerciseRows ->
            val exerciseId = exerciseRows.first().exerciseId
            // Вхождения нумеруются по всем упражнениям сессии, до отсева по статусу: если сначала
            // выбросить пропущенные, вхождения перенумеруются и факты приедут не в тот подход.
            val occurrence = occurrences.takeNextOccurrence(exerciseId)
            exerciseRows.forEach { row ->
                val actual = row.set.completedValue() ?: return@forEach
                val coordinate = ProgramSetCoordinate(
                    exerciseId = exerciseId,
                    occurrence = occurrence,
                    setPosition = row.set.position,
                )
                factsByCoordinate.getOrPut(coordinate) { actual }
            }
        }
    }
    return session.resultsBySetId(factsByCoordinate)
}

/** Снапшот программы: всё, что нужно карточке истории, лежит в самой строке сессии. */
private fun WorkoutSessionDatabaseEntity.toProgramEntity(): SessionProgramEntity =
    SessionProgramEntity(
        id = programId,
        name = programName,
        accent = ExerciseAccent.fromKey(programAccent),
        glyph = ExerciseGlyph.fromKey(programGlyph),
    )

private fun SessionExerciseWithSets.toDomainEntity(): SessionExerciseEntity =
    SessionExerciseEntity(
        id = sessionExercise.id,
        // Упражнение читается живым из библиотеки — вместе с маркером, как и имя с описанием.
        exercise = WorkoutExerciseEntity(
            id = exercise.id,
            name = exercise.name,
            description = exercise.description,
            accent = ExerciseAccent.fromKey(exercise.accent),
            glyph = ExerciseGlyph.fromKey(exercise.glyph),
        ),
        sets = sets
            .sortedBy { it.position }
            .map { it.toDomainEntity() },
    )

private fun SessionSetDatabaseEntity.toDomainEntity(): SessionSetEntity =
    SessionSetEntity(
        id = id,
        target = SessionSetValueEntity(count = targetCount, weight = targetWeight),
        result = toResultEntity(),
        note = note,
    )

/**
 * Те же факты, но под id подходов самой сессии: потребителю ориентира про вхождения знать не нужно —
 * у него на руках уже есть id подхода, который он рисует.
 *
 * Индекс подхода в списке — это и есть его `position`: [toSessionRows] нумерует подходы подряд с
 * нуля, а `toDomainEntity` восстанавливает порядок сортировкой по той же колонке.
 */
private fun WorkoutSessionEntity.resultsBySetId(
    factsByCoordinate: Map<ProgramSetCoordinate, SessionSetValueEntity>,
): Map<String, SessionSetValueEntity> {
    val occurrences = mutableMapOf<String, Int>()
    val previousBySetId = mutableMapOf<String, SessionSetValueEntity>()
    exercises.forEach { sessionExercise ->
        val occurrence = occurrences.takeNextOccurrence(sessionExercise.exercise.id)
        sessionExercise.sets.forEachIndexed { setPosition, set ->
            val coordinate = ProgramSetCoordinate(
                exerciseId = sessionExercise.exercise.id,
                occurrence = occurrence,
                setPosition = setPosition,
            )
            factsByCoordinate[coordinate]?.let { value -> previousBySetId[set.id] = value }
        }
    }
    return previousBySetId
}

/**
 * Забирает номер очередного вхождения упражнения (0 — первое) и **сдвигает** счётчик-приёмник.
 * Считается
 * отдельно для истории и для текущей сессии, поэтому вынесен: одинаковая нумерация с обеих сторон —
 * это и есть сопоставление.
 */
private fun MutableMap<String, Int>.takeNextOccurrence(exerciseId: String): Int {
    val occurrence = getOrElse(exerciseId) { 0 }
    this[exerciseId] = occurrence + 1
    return occurrence
}

/**
 * Факт подхода — или `null`, если ориентира нет: подход пропущен, не выполнялся либо выполнен без
 * числа повторений (`result_count` в схеме nullable). Ноль вместо пустого значения был бы хуже
 * отсутствия строки — поэтому здесь нет `?: 0`, в отличие от [toResultEntity], которому нужен
 * непустой факт для трека исходов.
 */
private fun SessionSetDatabaseEntity.completedValue(): SessionSetValueEntity? {
    if (resultStatus != SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED) {
        return null
    }
    val count = resultCount ?: return null
    return SessionSetValueEntity(count = count, weight = resultWeight)
}

private fun SessionSetDatabaseEntity.toResultEntity(): SessionSetResultEntity? =
    when (resultStatus) {
        null -> null
        SessionSetDatabaseEntity.RESULT_STATUS_SKIPPED -> SessionSetResultEntity.Skipped
        SessionSetDatabaseEntity.RESULT_STATUS_COMPLETED -> SessionSetResultEntity.Completed(
            actual = SessionSetValueEntity(count = resultCount ?: 0, weight = resultWeight),
        )
        else -> null
    }

/**
 * Координата подхода внутри программы, устойчивая к её правкам: id упражнения-библиотеки, номер
 * вхождения этого упражнения в сессию и позиция подхода внутри вхождения.
 *
 * Позиции упражнения в координате нет намеренно: она меняется от одной правки программы к другой, а
 * вхождение — нет. Голого [exerciseId] мало: одно упражнение можно поставить в программу дважды, и
 * тогда факты второго вхождения подставились бы первому.
 */
private data class ProgramSetCoordinate(
    val exerciseId: String,
    val occurrence: Int,
    val setPosition: Int,
)
