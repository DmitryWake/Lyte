package com.nikolaevskii.lyte.core.design.component.session

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone

/**
 * Состояние одного подхода на экране тренировки. Две формы — и это разные сущности, а не флаг у
 * одной: [Resting] — спокойная строка в 36dp, [Current] — фокус-карточка со степперами, единственный
 * элемент экрана, до которого дотягивается палец.
 */
sealed interface LyteTrackSetState {

    /**
     * Спокойная строка. Что показано справа — решает [tone], он же задаёт цвет строки и иконку:
     *
     * | Тон | Иконка | Значение справа |
     * |---|---|---|
     * | `Met` / `Positive` / `Negative` | галочка / стрелка вверх / стрелка вниз | факт из [reps] и [weight] |
     * | `Skipped` | минус | «пропущен» |
     * | `Todo` | пустой круг | «цель [target]» |
     *
     * Тон — общий словарь системы ([LyteProgressTone]), а не собственный enum строки: один и тот же
     * исход обязан выглядеть одинаково и здесь, и в треке сводки, и в деталях сессии. **Крестика нет
     * ни в одном состоянии**: недобор до цели — это направление, а не провал.
     *
     * [note] — заметка к подходу, одной строкой с многоточием между иконкой и значением.
     */
    data class Resting(
        val tone: LyteProgressTone,
        val reps: Int = 0,
        val weight: Double = 0.0,
        val target: String? = null,
        val note: String? = null,
    ) : LyteTrackSetState

    /**
     * Текущий подход — фокус-карточка. [total] — сколько подходов в упражнении («из M» в шапке);
     * [target] и [last] — ориентиры «Цель» и «В прошлый раз» готовыми значениями («10×60 кг»),
     * `null` — строка не показывается. Заметку или чип под степперы кладёт вызывающая сторона
     * слотом `content`.
     */
    data class Current(
        val total: Int,
        val reps: Int,
        val weight: Double,
        val target: String? = null,
        val last: String? = null,
        val repsStep: Int = 1,
        val weightStep: Double = 2.5,
    ) : LyteTrackSetState
}
