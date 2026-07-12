package com.nikolaevskii.lyte.feature.tracker.presentation.model

import com.nikolaevskii.lyte.feature.tracker.domain.currentSet
import com.nikolaevskii.lyte.feature.tracker.domain.effectiveCurrentExercise
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionExerciseEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetOutcomeEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetResultEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.SessionSetValueEntity
import com.nikolaevskii.lyte.feature.tracker.domain.model.WorkoutSessionEntity
import com.nikolaevskii.lyte.feature.tracker.domain.util.hasWeight
import com.nikolaevskii.lyte.feature.tracker.domain.util.outcome
import com.nikolaevskii.lyte.feature.tracker.presentation.util.formatWeight

/**
 * Маппит доменную сессию в готовую к отрисовке [ActiveSessionUiModel]: эффективное текущее
 * упражнение/подход (правила — `SessionProgression`), статусы плашек из [outcome], счётчики и строки
 * шторки переключения. Чистая функция без ресурсов — локализованные единицы подставляет UI-слой.
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
        plaques = exercise.sets.mapIndexed { index, set ->
            set.toPlaque(number = index + 1, isCurrent = index == currentSetIndex)
        },
        currentPlaqueIndex = currentSetIndex,
        setIndex = currentSetIndex + 1,
        setCount = exercise.sets.size,
        currentSetId = currentSet.id,
        targetReps = currentSet.target.count,
        targetWeight = currentSet.target.weight.takeIf { currentSet.target.hasWeight },
        target = currentSet.target.toValueUiModel(),
        note = currentSet.note,
    )
}

private fun SessionSetEntity.toPlaque(number: Int, isCurrent: Boolean): ActiveSessionSetPlaqueUiModel {
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
        ActiveSessionSetStatus.Current, ActiveSessionSetStatus.Todo -> target.toValueUiModel()

        ActiveSessionSetStatus.Hit, ActiveSessionSetStatus.Exceeded, ActiveSessionSetStatus.Missed ->
            (result as SessionSetResultEntity.Completed).actual.toValueUiModel()

        ActiveSessionSetStatus.Skipped -> null
    }
    return ActiveSessionSetPlaqueUiModel(index = number, status = status, value = value)
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
            sets.map { set -> set.target.toValueUiModel() }
        } else {
            emptyList()
        },
        isSelectable = status != ActiveSessionSwitcherStatus.Done,
    )
}

private fun SessionSetValueEntity.toValueUiModel(): ActiveSessionSetValueUiModel = if (hasWeight) {
    ActiveSessionSetValueUiModel.Weighted(reps = count, weight = formatWeight(checkNotNull(weight)))
} else {
    ActiveSessionSetValueUiModel.Bodyweight(reps = count)
}
