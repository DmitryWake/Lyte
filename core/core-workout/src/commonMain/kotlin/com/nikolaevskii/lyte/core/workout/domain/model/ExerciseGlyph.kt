package com.nikolaevskii.lyte.core.workout.domain.model

/**
 * Знак маркера — движение, которое несёт упражнение или программа. Набор намеренно не добит до
 * «всех упражнений»: у жима стоя, брусьев и выпадов своего знака нет, они берут ближайший
 * корректный, а не похожий-неверный.
 *
 * [key] — значение, которым знак лежит в БД; про стабильность ключа см. [ExerciseAccent.key].
 * Порядок значений — порядок сетки в пикере знака. Пара к UI-шному `LyteExerciseGlyph`
 * из `:core:core-design`.
 */
enum class ExerciseGlyph(val key: String) {
    Squat(key = "squat"),
    Deadlift(key = "deadlift"),
    BenchPress(key = "bench-press"),
    PullUp(key = "pull-up"),
    DumbbellPress(key = "dumbbell-press"),
    Curl(key = "curl"),
    Crunch(key = "crunch"),
    Stretch(key = "stretch"),
    Rack(key = "rack"),
    Machine(key = "machine"),
    ;

    companion object {

        /** Знак упражнения, для которого его не выбирали. */
        val Default: ExerciseGlyph = Squat

        /** Знак по значению из БД; неизвестный ключ даёт [Default] — см. [ExerciseAccent.fromKey]. */
        fun fromKey(key: String): ExerciseGlyph =
            entries.firstOrNull { glyph -> glyph.key == key } ?: Default
    }
}
