package com.nikolaevskii.lyte.core.design.component.session

import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone

/**
 * Состояние одного подхода в списке трекинга. Формы взаимоисключающие — поэтому sealed, а не набор
 * флагов: спокойная строка и фокус-карточка не делят ни одного поля.
 *
 * Тон берётся из общесистемного [LyteProgressTone], а не из собственного словаря состояний: один и
 * тот же исход обязан выглядеть одинаково и в треке сводки, и в строке подхода, и в деталях сессии.
 */
sealed interface LyteTrackSetState {

    /**
     * Спокойная строка: выполненный, пропущенный или будущий подход.
     *
     * [value] — то, что строка показывает справа: факт для выполненного подхода и цель для
     * [LyteProgressTone.Todo]. У [LyteProgressTone.Skipped] значения нет — строка печатает
     * «пропущен». [note] рисуется одной строкой с многоточием между иконкой и значением.
     */
    data class Quiet(
        val tone: LyteProgressTone,
        val value: LyteSetValue? = null,
        val note: String? = null,
    ) : LyteTrackSetState

    /**
     * Фокус-карточка текущего подхода: степперы повторов и веса плюс строки-ориентиры.
     *
     * [weight] `null` — упражнение со своим весом: секция веса не рисуется вовсе, а не показывает
     * ноль. [setCount] нужен для счётчика «из M» в шапке.
     */
    data class Focus(
        val setCount: Int,
        val reps: Int,
        val weight: Double? = null,
        val references: List<LyteTrackSetReference> = emptyList(),
    ) : LyteTrackSetState
}
