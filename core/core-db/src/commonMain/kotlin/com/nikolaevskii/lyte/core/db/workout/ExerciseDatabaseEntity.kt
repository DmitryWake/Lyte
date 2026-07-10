package com.nikolaevskii.lyte.core.db.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [nameNormalized] — [name] в нижнем регистре, служебная колонка под поиск и сортировку
 * (см. [ExerciseDao.search]). Отдельная колонка нужна потому, что `LIKE`, `lower()` и коллация
 * `NOCASE` в SQLite игнорируют регистр только у ASCII: без неё «жим» не нашёл бы «Жим лёжа», а
 * упражнение, названное со строчной буквы, уехало бы в конец списка. Заполняется маппером при записи.
 *
 * [isArchived] — soft delete: упражнение, на которое ссылаются программы или сессии, не удаляется
 * физически, а прячется из библиотеки (иначе `session_exercise`/`workout_exercise` потеряли бы строку,
 * от которой зависит отображение имени). См. [ExerciseDao.deleteOrArchiveExercise].
 */
@Entity(tableName = "exercise")
data class ExerciseDatabaseEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "name_normalized")
    val nameNormalized: String,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    val isArchived: Boolean = false,
)
