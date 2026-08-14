package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.session.domain.currentSet
import com.nikolaevskii.lyte.core.session.domain.effectiveCurrentExercise
import com.nikolaevskii.lyte.core.session.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.core.session.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.core.session.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.core.session.domain.util.hasWeight
import com.nikolaevskii.lyte.core.session.domain.util.outcome

/**
 * Маппит доменную сессию в готовую к отрисовке [ActiveSessionUiModel]: эффективное текущее
 * упражнение/подход (правила — `SessionProgression`), статусы строк подходов из [outcome], счётчики и
 * строки шторки переключения. Чистая функция без ресурсов — единицы («повт», «кг») подставляет
 * дизайн-система по [LyteSetValue].
 *
 * Ориентира «В прошлый раз» здесь нет намеренно: фактов предыдущей сессии домен не хранит
 * (`SessionSetEntity` знает только цель и факт текущей), а выдумывать их нельзя. Строка появится
 * вместе с источником данных — запросом фактов того же упражнения из последней завершённой сессии.
 */
internal fun WorkoutSessionEntity.toActiveSessionUiModel(): ActiveSessionUiModel {
    val currentExercise = effectiveCurrentExercise()
    val allSets = exercises.flatMap { exercise -> exercise.sets }
    return ActiveSessionUiModel(
        sessionId = id,
        programName = program.name,
        startedAt = startedAt,
        completedCount = allSets.count { set -> set.result is SessionSetResultEntity.Completed },
        totalCount = allSets.size,
        current = currentExercise?.let { exercise -> toCurrentUiModel(exercise) },
        switcherRows = exercises.map { exercise ->
            exercise.toSwitcherRow(isCurrent = exercise.id == currentExercise?.id)
        },
    )
}

private fun WorkoutSessionEntity.toCurrentUiModel(exercise: SessionExerciseEntity): ActiveSessionCurrentUiModel {
    // Эффективное текущее упражнение по построению имеет незакрытый подход.
    val currentSet = checkNotNull(exercise.currentSet()) { "Exercise ${exercise.id} has no pending sets" }
    val currentSetIndex = exercise.sets.indexOfFirst { set -> set.id == currentSet.id }
    return ActiveSessionCurrentUiModel(
        exerciseId = exercise.id,
        exerciseIndex = exercises.indexOfFirst { candidate -> candidate.id == exercise.id } + 1,
        exerciseCount = exercises.size,
        exerciseName = exercise.exercise.name,
        sets = exercise.sets.mapIndexed { index, set ->
            set.toSetUiModel(number = index + 1, isCurrent = index == currentSetIndex)
        },
        currentSetIndex = currentSetIndex,
        setCount = exercise.sets.size,
        currentSetId = currentSet.id,
        targetReps = currentSet.target.count,
        targetWeight = currentSet.target.weight.takeIf { currentSet.target.hasWeight },
        target = currentSet.target.toSetValue(),
        note = currentSet.note,
    )
}

private fun SessionSetEntity.toSetUiModel(number: Int, isCurrent: Boolean): ActiveSessionSetUiModel {
    val status = when {
        isCurrent -> ActiveSessionSetStatus.Current
        else -> when (outcome()) {
            null -> ActiveSessionSetStatus.Todo
            SessionSetOutcomeEntity.MET -> ActiveSessionSetStatus.Hit
            SessionSetOutcomeEntity.EXCEEDED -> ActiveSessionSetStatus.Exceeded
            SessionSetOutcomeEntity.MISSED -> ActiveSessionSetStatus.Missed
            SessionSetOutcomeEntity.SKIPPED -> ActiveSessionSetStatus.Skipped
        }
    }
    val value = when (status) {
        ActiveSessionSetStatus.Current, ActiveSessionSetStatus.Todo -> target.toSetValue()

        ActiveSessionSetStatus.Hit, ActiveSessionSetStatus.Exceeded, ActiveSessionSetStatus.Missed ->
            (result as SessionSetResultEntity.Completed).actual.toSetValue()

        ActiveSessionSetStatus.Skipped -> null
    }
    return ActiveSessionSetUiModel(index = number, status = status, value = value, note = note)
}

private fun SessionExerciseEntity.toSwitcherRow(isCurrent: Boolean): ActiveSessionSwitcherRowUiModel {
    val doneCount = sets.count { set -> set.result != null }
    val status = when {
        isCurrent -> ActiveSessionSwitcherStatus.Current
        // Упражнение без подходов тоже считается закрытым — выбирать его текущим нечем.
        doneCount == sets.size -> ActiveSessionSwitcherStatus.Done
        else -> ActiveSessionSwitcherStatus.Pending
    }
    return ActiveSessionSwitcherRowUiModel(
        exerciseId = id,
        name = exercise.name,
        status = status,
        doneCount = doneCount,
        setCount = sets.size,
        currentSetIndex = if (isCurrent) sets.indexOfFirst { set -> set.result == null } + 1 else null,
        targetPills = if (status == ActiveSessionSwitcherStatus.Pending) {
            sets.map { set -> set.target.toSetValue() }
        } else {
            emptyList()
        },
        isSelectable = status != ActiveSessionSwitcherStatus.Done,
    )
}

private fun SessionSetValueEntity.toSetValue(): LyteSetValue =
    LyteSetValue(reps = count, weight = weight.takeIf { hasWeight })
