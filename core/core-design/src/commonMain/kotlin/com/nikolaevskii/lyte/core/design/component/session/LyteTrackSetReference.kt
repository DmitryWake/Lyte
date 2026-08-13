package com.nikolaevskii.lyte.core.design.component.session

/**
 * Строка-ориентир в фокус-карточке подхода: «Цель — 10×62,5 кг», «В прошлый раз — 10×60 кг».
 *
 * Подписи принадлежат дизайн-системе, а не вызывающей стороне: это тот же словарь, которым говорит
 * спокойная строка будущего подхода («цель …»), и разъезжаться этим двум формулировкам нельзя.
 * Вызывающая сторона решает, какие ориентиры показать и в каком порядке, — но не как их назвать.
 */
sealed interface LyteTrackSetReference {

    val value: String

    /** Цель подхода по плану программы. */
    data class Target(override val value: String) : LyteTrackSetReference

    /** Что было в этом подходе на прошлой тренировке. */
    data class LastTime(override val value: String) : LyteTrackSetReference
}
