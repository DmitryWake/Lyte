package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.ui.Modifier

/**
 * Вариант карточки упражнения — задаёт ведущий элемент строки и набор действий, зависящие от контекста.
 * Вынесен в sealed-тип, чтобы исключить бессмысленные сочетания (номер-бейдж с кнопками действий или
 * drag-хэндл в read-only превью): [LyteExerciseCard] не принимает эти параметры по отдельности.
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
     * Read-only превью программы (спека 4.2): слева круглый бейдж с порядковым номером [index]
     * (позиция упражнения в программе), без действий и переупорядочивания.
     */
    data class Preview(val index: Int) : LyteExerciseCardVariant
}
