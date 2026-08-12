package com.nikolaevskii.lyte.core.db

/**
 * Значения маркера (цвет и знак) по умолчанию — в SQL-виде, вместе с кавычками: одна и та же строка
 * подставляется и в `@ColumnInfo(defaultValue = ...)` сущностей, и в DDL миграции. Заявленный
 * сущностью дефолт обязан совпадать с фактическим дефолтом колонки, иначе Room не примет схему
 * при открытии БД.
 *
 * Ключи повторяют доменные `ExerciseAccent.Slate` и `ExerciseGlyph.Squat` из `:core:core-workout`:
 * `core-db` про домен не знает и знать не должен, поэтому значения дублируются строкой. Расхождение
 * безопасно (читающая сторона подставит дефолт вместо неизвестного ключа) и ловится тестом миграции.
 */
internal const val DEFAULT_ACCENT_SQL: String = "'slate'"

internal const val DEFAULT_GLYPH_SQL: String = "'squat'"
