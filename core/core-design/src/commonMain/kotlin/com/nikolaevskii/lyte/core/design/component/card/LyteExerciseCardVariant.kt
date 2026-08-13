package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.ui.Modifier

/**
 * Вариант карточки упражнения — задаёт, есть ли у строки drag-хэндл и кнопки действий. Вынесен в
 * sealed-тип, чтобы исключить бессмысленное сочетание «read-only карточка с кнопкой удаления»:
 * [LyteExerciseCard] не принимает эти параметры по отдельности.
 *
 * Двух вариантов ровно столько, сколько мест у карточки в v2: редактор программы (3.2) и read-only
 * превью программы перед стартом (4.2).
 */
sealed interface LyteExerciseCardVariant {

    /**
     * Редактор программы: слева drag-хэндл для переупорядочивания, справа действия «редактировать
     * подходы» ([onEdit]) и «убрать из программы» ([onRemove]) — любое действие можно опустить.
     *
     * Жест сам компонент не реализует: [dragHandleModifier] — точка подключения
     * `pointerInput`/`detectDragGestures` вызывающей стороны, привязанная к хэндлу (а не ко всей
     * строке), чтобы drag не конфликтовал со скроллом списка.
     */
    data class Editor(
        val onEdit: (() -> Unit)? = null,
        val onRemove: (() -> Unit)? = null,
        val dragHandleModifier: Modifier = Modifier,
    ) : LyteExerciseCardVariant

    /**
     * Read-only превью программы (спека 4.2): ни хэндла, ни действий. Порядкового номера у
     * упражнения в v2 нет — карточку ведёт маркер, а порядок читается по самому списку.
     */
    data object ReadOnly : LyteExerciseCardVariant
}
