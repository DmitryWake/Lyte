package com.nikolaevskii.lyte.core.design.component.session

/** Визуальный статус карточки [LyteExerciseStrip]: выделенная текущая, завершённая (галочка), обычная. */
enum class LyteExerciseStripStatus { Done, Current, Todo }

/**
 * Модель карточки [LyteExerciseStrip] — только роли раскладки: [title] (имя, полоса не обрезает —
 * передавайте уже укороченное), [subtitle] (напр. «3/3») и визуальный [status]. Тексты — от
 * вызывающей стороны.
 */
data class LyteExerciseStripItem(
    val title: String,
    val subtitle: String,
    val status: LyteExerciseStripStatus,
)
