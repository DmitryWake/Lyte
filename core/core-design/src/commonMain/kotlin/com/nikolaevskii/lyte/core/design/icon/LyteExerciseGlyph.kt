package com.nikolaevskii.lyte.core.design.icon

import androidx.compose.runtime.Composable
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_bench_press
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_crunch
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_curl
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_deadlift
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_dumbbell_press
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_machine
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_pull_up
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_rack
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_squat
import com.nikolaevskii.lyte.core.design.generated.resources.exercise_glyph_stretch
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_bench_press
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_crunch
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_curl
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_deadlift
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_dumbbell_press
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_machine
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_pull_up
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_rack
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_squat
import com.nikolaevskii.lyte.core.design.generated.resources.ic_exercise_stretch
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Десять движений, которые может нести упражнение. Вместе с [LyteAccent] образуют круг-маркер
 * упражнения: цвет и знак — два обычных свойства упражнения, ничего не выводится из данных.
 *
 * Набор намеренно не добит до «всех упражнений»: для жима стоя, брусьев и выпадов отдельного знака
 * нет, и они берут ближайший корректный, а не похожий-неверный — приблизительный глиф хуже честно
 * разделённого. Порядок значений — порядок сетки в пикере знака.
 *
 * [Squat] — дефолт: упражнение без выбранного знака всё равно выглядит осознанным.
 */
enum class LyteExerciseGlyph {
    Squat,
    Deadlift,
    BenchPress,
    PullUp,
    DumbbellPress,
    Curl,
    Crunch,
    Stretch,
    Rack,
    Machine,
    ;

    companion object {
        val Default: LyteExerciseGlyph = Squat
    }
}

/** Файл пиктограммы движения. Рисует его [LyteExerciseIcon] — напрямую ресурс никому не нужен. */
internal val LyteExerciseGlyph.drawable: DrawableResource
    get() = when (this) {
        LyteExerciseGlyph.Squat -> Res.drawable.ic_exercise_squat
        LyteExerciseGlyph.Deadlift -> Res.drawable.ic_exercise_deadlift
        LyteExerciseGlyph.BenchPress -> Res.drawable.ic_exercise_bench_press
        LyteExerciseGlyph.PullUp -> Res.drawable.ic_exercise_pull_up
        LyteExerciseGlyph.DumbbellPress -> Res.drawable.ic_exercise_dumbbell_press
        LyteExerciseGlyph.Curl -> Res.drawable.ic_exercise_curl
        LyteExerciseGlyph.Crunch -> Res.drawable.ic_exercise_crunch
        LyteExerciseGlyph.Stretch -> Res.drawable.ic_exercise_stretch
        LyteExerciseGlyph.Rack -> Res.drawable.ic_exercise_rack
        LyteExerciseGlyph.Machine -> Res.drawable.ic_exercise_machine
    }

private val LyteExerciseGlyph.label: StringResource
    get() = when (this) {
        LyteExerciseGlyph.Squat -> Res.string.exercise_glyph_squat
        LyteExerciseGlyph.Deadlift -> Res.string.exercise_glyph_deadlift
        LyteExerciseGlyph.BenchPress -> Res.string.exercise_glyph_bench_press
        LyteExerciseGlyph.PullUp -> Res.string.exercise_glyph_pull_up
        LyteExerciseGlyph.DumbbellPress -> Res.string.exercise_glyph_dumbbell_press
        LyteExerciseGlyph.Curl -> Res.string.exercise_glyph_curl
        LyteExerciseGlyph.Crunch -> Res.string.exercise_glyph_crunch
        LyteExerciseGlyph.Stretch -> Res.string.exercise_glyph_stretch
        LyteExerciseGlyph.Rack -> Res.string.exercise_glyph_rack
        LyteExerciseGlyph.Machine -> Res.string.exercise_glyph_machine
    }

/** Подпись движения («Присед», «Становая», …) — для пикера знака и `contentDescription`. */
@Composable
fun lyteExerciseGlyphLabel(glyph: LyteExerciseGlyph): String = stringResource(glyph.label)
