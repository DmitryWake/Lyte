package com.nikolaevskii.lyte.core.design.component.session

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.model.LyteSetValue

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
     * | `Met` / `Positive` / `Negative` | галочка / стрелка вверх / стрелка вниз | факт из [value] |
     * | `Skipped` | минус | «пропущен» ([value] не читается) |
     * | `Todo` | пустой круг | «цель [value]» |
     *
     * Тон — общий словарь системы ([LyteProgressTone]), а не собственный enum строки: один и тот же
     * исход обязан выглядеть одинаково и здесь, и в треке сводки, и в деталях сессии. **Крестика нет
     * ни в одном состоянии**: недобор до цели — это направление, а не провал.
     *
     * [value] — одно поле, а не пара «факт + цель»: по тону всегда валидно ровно одно из них, и
     * хранить оба значило бы допускать невозможные комбинации. Формат («10×60 кг» / «10 повт»)
     * компонент собирает сам — единицы это его хром, а не доменный текст вызывающей фичи.
     *
     * [note] — заметка к подходу, одной строкой с многоточием между иконкой и значением.
     */
    data class Resting(
        val tone: LyteProgressTone,
        val value: LyteSetValue? = null,
        val note: String? = null,
    ) : LyteTrackSetState

    /**
     * Текущий подход — фокус-карточка. [total] — сколько подходов в упражнении («из M» в шапке);
     * [target] и [last] — ориентиры «Цель» и «В прошлый раз», `null` — строка не показывается.
     * Заметку или чип под степперы кладёт вызывающая сторона слотом `content`.
     *
     * [weight] `null` — упражнение своего веса: степпер веса не показывается вовсе, как и в модели
     * подхода ([LyteSetValue.weight]). Это не «вес 0», а «веса нет».
     */
    data class Current(
        val total: Int,
        val reps: Int,
        val weight: Double? = null,
        val target: LyteSetValue? = null,
        val last: LyteSetValue? = null,
        val repsStep: Int = 1,
        val weightStep: Double = 2.5,
    ) : LyteTrackSetState
}
