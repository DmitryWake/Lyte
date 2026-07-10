package com.nikolaevskii.lyte.core.db.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nikolaevskii.lyte.core.db.workout.ExerciseDatabaseEntity

/**
 * Упражнение внутри сессии. Имя/описание НЕ снапшотятся: они читаются живыми из `exercise` по
 * [exerciseId] (join в [SessionExerciseWithSets]) — переименование упражнения отражается и в истории.
 *
 * FK на `exercise` (без каскада) гарантирует, что строка упражнения существует, пока на неё ссылается
 * сессия: удаление упражнения при наличии ссылок запрещено — вместо этого оно архивируется
 * (`exercise.is_archived`, см. [com.nikolaevskii.lyte.core.db.workout.ExerciseDao.deleteOrArchiveExercise]).
 */
@Entity(
    tableName = "session_exercise",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionDatabaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseDatabaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
        ),
    ],
    indices = [
        Index("session_id"),
        Index("exercise_id"),
    ],
)
data class SessionExerciseDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,
    @ColumnInfo(name = "position")
    val position: Int,
)
