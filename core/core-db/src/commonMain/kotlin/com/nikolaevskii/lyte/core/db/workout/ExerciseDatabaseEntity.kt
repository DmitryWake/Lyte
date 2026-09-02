package com.nikolaevskii.lyte.core.db.workout

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nikolaevskii.lyte.core.db.DEFAULT_ACCENT_SQL
import com.nikolaevskii.lyte.core.db.DEFAULT_GLYPH_SQL

/**
 * [nameNormalized] — [name] в нижнем регистре и с обычными пробелами вместо неразрывных, служебная
 * колонка под поиск и сортировку (см. [ExerciseDao.search]). Отдельная колонка нужна потому, что
 * `LIKE`, `lower()` и коллация `NOCASE` в SQLite игнорируют регистр только у ASCII: без неё «жим»
 * не нашёл бы «Жим лёжа», а упражнение, названное со строчной буквы, уехало бы в конец списка.
 * Неразрывный пробел (его подставляют клавиатуры) приводится к обычному по той же причине — иначе
 * запрос с обычным пробелом не совпал бы с именем. Заполняется маппером при записи; тем же правилом
 * маппер приводит и поисковый запрос — иначе стороны сравнения разъедутся.
 *
 * [isArchived] — soft delete: упражнение, на которое ссылаются программы или сессии, не удаляется
 * физически, а прячется из библиотеки (иначе `session_exercise`/`workout_exercise` потеряли бы строку,
 * от которой зависит отображение имени). См. [ExerciseDao.deleteOrArchiveExercise].
 *
 * [accent]/[glyph] — маркер упражнения: цвет и знак, два обычных свойства, выбранные пользователем.
 * Хранятся строковыми ключами домена, а не порядковыми номерами enum'а: набор значений будет
 * расширяться, и порядок не должен быть частью формата хранения. Неизвестный ключ читающая сторона
 * заменяет дефолтом, поэтому колонки `NOT NULL` с `DEFAULT` — строка без маркера невозможна.
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
    @ColumnInfo(name = "accent", defaultValue = DEFAULT_ACCENT_SQL)
    val accent: String,
    @ColumnInfo(name = "glyph", defaultValue = DEFAULT_GLYPH_SQL)
    val glyph: String,
)
