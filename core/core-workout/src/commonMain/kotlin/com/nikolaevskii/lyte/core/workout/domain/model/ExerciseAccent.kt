package com.nikolaevskii.lyte.core.workout.domain.model

/**
 * Цвет маркера упражнения или программы — обычное свойство, выбранное пользователем: из данных
 * (группа мышц, оборудование) он не выводится.
 *
 * [key] — значение, которым цвет лежит в БД. Стабильная строка, а не `ordinal`: набор цветов может
 * расшириться, и порядок значений не должен быть частью формата хранения. Переименование ключа —
 * это миграция БД, а не правка enum'а.
 *
 * Пара к UI-шному `LyteAccent` из `:core:core-design`: домен не знает про Compose, дизайн-система —
 * про домен, маппинг делает фича в своём `UiMapper`.
 */
enum class ExerciseAccent(val key: String) {
    Coral(key = "coral"),
    Indigo(key = "indigo"),
    Lime(key = "lime"),
    Amber(key = "amber"),
    Teal(key = "teal"),
    Slate(key = "slate"),
    ;

    companion object {

        /** Цвет упражнения, для которого его не выбирали. */
        val Default: ExerciseAccent = Slate

        /**
         * Цвет по значению из БД. Неизвестный ключ (строка из будущей версии, ручная правка файла
         * БД) даёт [Default] — чтение данных не должно падать из-за одного поля.
         */
        fun fromKey(key: String): ExerciseAccent =
            entries.firstOrNull { accent -> accent.key == key } ?: Default
    }
}
