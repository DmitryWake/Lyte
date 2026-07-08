package com.nikolaevskii.lyte.core.design.component.session

/** Визуальный тон плашки [LyteSetOverview]: заливка + выделение текущей (шире, с тенью). */
enum class LyteSetOverviewTone { Current, Hit, Exceed, Miss, Skip, Todo }

/**
 * Модель одной плашки [LyteSetOverview] — только роли раскладки, без доменных данных:
 * [caption] (верхняя капс-подпись), [value] (крупное значение) и визуальный [tone].
 * Оба текста полностью формирует вызывающая сторона.
 */
data class LyteSetOverviewItem(
    val caption: String,
    val value: String,
    val tone: LyteSetOverviewTone,
)
