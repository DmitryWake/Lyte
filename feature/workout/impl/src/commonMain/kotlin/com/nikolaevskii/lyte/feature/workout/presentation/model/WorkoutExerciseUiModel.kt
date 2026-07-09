package com.nikolaevskii.lyte.feature.workout.presentation.model

import com.nikolaevskii.lyte.feature.workout.domain.model.WorkoutExerciseWithRepsEntity
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Упражнение в редакторе программы. [key] — синтетический идентификатор слота, стабильный в рамках
 * экрана: у [WorkoutExerciseWithRepsEntity] нет собственного id (только у вложенного упражнения), так
 * что одно и то же упражнение, добавленное в программу дважды, не даёт коллизии ключей в списке при
 * перетаскивании.
 */
data class WorkoutExerciseUiModel(
    val key: String,
    val exercise: WorkoutExerciseWithRepsEntity,
)

@OptIn(ExperimentalUuidApi::class)
internal fun WorkoutExerciseWithRepsEntity.toUiModel(): WorkoutExerciseUiModel =
    WorkoutExerciseUiModel(key = Uuid.random().toString(), exercise = this)
