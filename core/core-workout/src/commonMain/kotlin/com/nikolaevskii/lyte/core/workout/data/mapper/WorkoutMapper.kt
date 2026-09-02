package com.nikolaevskii.lyte.core.workout.data.mapper

import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutExerciseCrossRefDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutExerciseWithSets
import com.nikolaevskii.lyte.core.db.workout.WorkoutItemWithExerciseCount
import com.nikolaevskii.lyte.core.db.workout.WorkoutSetDatabaseEntity
import com.nikolaevskii.lyte.core.db.workout.WorkoutSetTargetUpdate
import com.nikolaevskii.lyte.core.db.workout.WorkoutWithExercises
import com.nikolaevskii.lyte.core.workout.data.model.WorkoutRowsModel
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseAccent
import com.nikolaevskii.lyte.core.workout.domain.model.ExerciseGlyph
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutItemEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity

/** Разделитель сегментов синтетических id связок/подходов. */
private const val ID_SEGMENT_SEPARATOR: String = "#"

internal fun WorkoutItemWithExerciseCount.toItemEntity(): WorkoutItemEntity =
    WorkoutItemEntity(
        id = id,
        name = name,
        description = description,
        accent = ExerciseAccent.fromKey(accent),
        glyph = ExerciseGlyph.fromKey(glyph),
        exerciseCount = exerciseCount,
    )

internal fun ExerciseDatabaseEntity.toDomainEntity(): WorkoutExerciseEntity =
    WorkoutExerciseEntity(
        id = id,
        name = name,
        description = description,
        accent = ExerciseAccent.fromKey(accent),
        glyph = ExerciseGlyph.fromKey(glyph),
    )

internal fun WorkoutExerciseEntity.toDatabaseEntity(): ExerciseDatabaseEntity =
    ExerciseDatabaseEntity(
        id = id,
        name = name,
        // Служебная колонка под поиск и сортировку — см. ExerciseDatabaseEntity.nameNormalized.
        // Нормализуется тем же правилом, что и поисковый запрос, иначе LIKE сравнивает разное.
        nameNormalized = name.normalizedForSearch(),
        description = description,
        accent = accent.key,
        glyph = glyph.key,
    )

internal fun WorkoutWithExercises.toDomainEntity(): WorkoutEntity =
    WorkoutEntity(
        id = workout.id,
        name = workout.name,
        description = workout.description,
        accent = ExerciseAccent.fromKey(workout.accent),
        glyph = ExerciseGlyph.fromKey(workout.glyph),
        exercises = exercises
            .sortedBy { it.crossRef.position }
            .map { it.toDomainEntity() },
    )

internal fun WorkoutEntity.toRows(): WorkoutRowsModel {
    val workoutRow = WorkoutDatabaseEntity(
        id = id,
        name = name,
        description = description,
        accent = accent.key,
        glyph = glyph.key,
    )
    val exerciseRows = mutableListOf<ExerciseDatabaseEntity>()
    val crossRefRows = mutableListOf<WorkoutExerciseCrossRefDatabaseEntity>()
    val setRows = mutableListOf<WorkoutSetDatabaseEntity>()

    exercises.forEachIndexed { exerciseIndex, exerciseWithReps ->
        val crossRefId = "$id$ID_SEGMENT_SEPARATOR$exerciseIndex"

        exerciseRows += exerciseWithReps.exercise.toDatabaseEntity()
        crossRefRows += WorkoutExerciseCrossRefDatabaseEntity(
            id = crossRefId,
            workoutId = id,
            exerciseId = exerciseWithReps.exercise.id,
            position = exerciseIndex,
        )
        exerciseWithReps.reps.forEachIndexed { repIndex, rep ->
            setRows += WorkoutSetDatabaseEntity(
                id = "$crossRefId$ID_SEGMENT_SEPARATOR$repIndex",
                workoutExerciseId = crossRefId,
                position = repIndex,
                count = rep.count,
                weight = rep.weight,
            )
        }
    }

    return WorkoutRowsModel(
        workout = workoutRow,
        exercises = exerciseRows,
        crossRefs = crossRefRows,
        sets = setRows,
    )
}

/** Цели подходов программы как точечные апдейты по позициям — см. [WorkoutSetTargetUpdate]. */
internal fun WorkoutEntity.toSetTargetUpdates(): List<WorkoutSetTargetUpdate> =
    exercises.flatMapIndexed { exerciseIndex, exerciseWithReps ->
        exerciseWithReps.reps.mapIndexed { repIndex, rep ->
            WorkoutSetTargetUpdate(
                exercisePosition = exerciseIndex,
                setPosition = repIndex,
                count = rep.count,
                weight = rep.weight,
            )
        }
    }

private fun WorkoutExerciseWithSets.toDomainEntity(): WorkoutExerciseWithRepsEntity =
    WorkoutExerciseWithRepsEntity(
        exercise = exercise.toDomainEntity(),
        reps = sets
            .sortedBy { it.position }
            .map { set -> WorkoutRepEntity(count = set.count, weight = set.weight) },
    )
