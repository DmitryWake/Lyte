package com.nikolaevskii.lyte.core.design.component.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrack
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrackMode
import com.nikolaevskii.lyte.core.design.component.progress.lytePlanTrackWidth
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_edit_sets
import com.nikolaevskii.lyte.core.design.generated.resources.a11y_remove_from_program
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.design.theme.lytePressScale
import org.jetbrains.compose.resources.stringResource

private val ExerciseCardPaddingStart = 8.dp
private val ExerciseCardPadding = 12.dp
private val ExerciseCardGap = 10.dp
private val ExerciseCardHandleSize = 18.dp
private val ExerciseCardMarkSize = 38.dp
private val ExerciseCardPlanSpacing = 6.dp
private val ExerciseCardPlanGap = 8.dp

/**
 * Место под трек плана. Резервируется целиком, даже когда трек занимает меньше: так подпись
 * «N подходов» у всех карточек списка начинается с одного места.
 */
private val ExerciseCardPlanSlotWidth = 76.dp

/** Сегмент не растёт дальше — при одном-трёх подходах трек выглядит так же, как до расширения слота. */
private val ExerciseCardPlanSegmentMaxWidth = 16.dp

/**
 * Порог, ниже которого трек не рисуется совсем (см. [lytePlanTrackWidth]). Взят от высоты сегмента
 * (5dp): при ширине меньше ~1.8 высоты пилюля превращается в круг, и ряд начинает читаться как
 * индикатор загрузки, а не как «сколько подходов». В слоте 76dp это шесть подходов; семь пилюль
 * в такую ширину не влезают ни при каком зазоре, поэтому дальше остаётся одна подпись.
 */
private val ExerciseCardPlanSegmentMinWidth = 9.dp
private const val ExerciseCardTitleMaxLines = 2

private val ExerciseCardSpecimenGap = 10.dp
private val ExerciseCardSpecimenPadding = 16.dp

/**
 * Карточка упражнения в программе: редактор (3.2) или read-only превью (4.2) — см. [variant].
 *
 * В v1 карточка рисовала каждый плановый подход отдельной пилюлей («10×60 кг» ×4), и одно упражнение
 * превращалось в пять конкурирующих текстовых блоков. В v2 тот же план читается [LyteProgressTrack]
 * в режиме `Plan` плюс одна подпись-счётчик; точные числа живут на касание глубже, в редакторе
 * подходов.
 *
 * Трек здесь — **план**, поэтому все сегменты одного цвета: он отвечает на «сколько подходов», а не
 * «сколько сделано». Прогресс — свойство сессии, и наполовину закрашенный трек в списке упражнений
 * программы читался бы как ошибка.
 *
 * [setCount] задаёт число сегментов, [setsLabel] — готовую подпись к ним («3 подхода»): число в
 * подписи и [setCount] обязаны быть про одно и то же, склейку с единицами делает вызывающая сторона.
 *
 * Трека нет при [setCount] = 0 и при плотном плане (в текущем слоте — от семи подходов): сегменты
 * становятся круглыми, и ряд читается индикатором загрузки, а не планом. Тогда остаётся одна подпись
 * — число подходов она несёт и так, а список карточек не мешает пилюли с точками. Арифметика и
 * обоснование — в [lytePlanTrackWidth].
 *
 * [onClick] делает кликабельной только колонку контента: маркер, drag-хэндл и кнопки действий из
 * тапа исключены, иначе перетаскивание и «удалить» конкурировали бы с переходом. Нажатие при этом
 * сжимает **всю** карточку: масштабировать одну колонку, оставив маркер и трек на месте, выглядело
 * бы поломкой вёрстки.
 */
@Composable
fun LyteExerciseCard(
    title: String,
    accent: LyteAccent,
    glyph: LyteExerciseGlyph,
    variant: LyteExerciseCardVariant,
    setCount: Int,
    setsLabel: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = modifier.lytePressScale(interactionSource, enabled = onClick != null),
        shape = LyteTheme.extendedShapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level1,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ExerciseCardGap),
            modifier = Modifier.padding(
                start = ExerciseCardPaddingStart,
                top = ExerciseCardPadding,
                end = ExerciseCardPadding,
                bottom = ExerciseCardPadding,
            ),
        ) {
            if (variant is LyteExerciseCardVariant.Editor) {
                Icon(
                    imageVector = LyteIcons.GripVertical,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = variant.dragHandleModifier.size(ExerciseCardHandleSize),
                )
            }
            LyteExerciseMark(accent = accent, glyph = glyph, size = ExerciseCardMarkSize)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickableContent(onClick = onClick, interactionSource = interactionSource),
            ) {
                Text(
                    text = title,
                    style = rowTitleStrongStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = ExerciseCardTitleMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ExerciseCardPlanGap),
                    modifier = Modifier.padding(top = ExerciseCardPlanSpacing),
                ) {
                    val trackWidth = lytePlanTrackWidth(
                        setCount = setCount,
                        slotWidth = ExerciseCardPlanSlotWidth,
                        minSegmentWidth = ExerciseCardPlanSegmentMinWidth,
                        maxSegmentWidth = ExerciseCardPlanSegmentMaxWidth,
                    )
                    if (trackWidth != null) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.width(ExerciseCardPlanSlotWidth),
                        ) {
                            LyteProgressTrack(
                                mode = LyteProgressTrackMode.Plan(total = setCount, accent = accent),
                                modifier = Modifier.width(trackWidth),
                            )
                        }
                    }
                    Text(
                        text = setsLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (variant is LyteExerciseCardVariant.Editor) {
                variant.onEdit?.let { onEdit ->
                    LyteIconButton(
                        icon = LyteIcons.Edit,
                        contentDescription = stringResource(Res.string.a11y_edit_sets),
                        onClick = onEdit,
                    )
                }
                variant.onRemove?.let { onRemove ->
                    LyteIconButton(
                        icon = LyteIcons.Delete,
                        contentDescription = stringResource(Res.string.a11y_remove_from_program),
                        onClick = onRemove,
                    )
                }
            }
        }
    }
}

/**
 * Тап без рипла: отклик карточки — сжатие всей поверхности ([lytePressScale] на `Surface`), поэтому
 * [interactionSource] приходит снаружи — он один на клик и на анимацию нажатия.
 */
private fun Modifier.clickableContent(
    onClick: (() -> Unit)?,
    interactionSource: MutableInteractionSource,
): Modifier = if (onClick == null) {
    this
} else {
    clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
}

@Preview
@Composable
private fun LyteExerciseCardPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(ExerciseCardSpecimenGap),
            modifier = Modifier.padding(ExerciseCardSpecimenPadding),
        ) {
            LyteExerciseCard(
                title = "Жим лёжа",
                accent = LyteAccent.Indigo,
                glyph = LyteExerciseGlyph.BenchPress,
                variant = LyteExerciseCardVariant.Editor(onEdit = {}, onRemove = {}),
                setCount = 3,
                setsLabel = "3 подхода",
                onClick = {},
            )
            // Самый плотный трек, который ещё рисуется: сегменты остаются пилюлями.
            LyteExerciseCard(
                title = "Подтягивания широким хватом до касания грудью",
                accent = LyteAccent.Coral,
                glyph = LyteExerciseGlyph.PullUp,
                variant = LyteExerciseCardVariant.Editor(onRemove = {}),
                setCount = 6,
                setsLabel = "6 подходов",
                onClick = {},
            )
            LyteExerciseCard(
                title = "Приседания со штангой",
                accent = LyteAccent.Lime,
                glyph = LyteExerciseGlyph.Squat,
                variant = LyteExerciseCardVariant.ReadOnly,
                setCount = 4,
                setsLabel = "4 подхода",
                onClick = {},
            )
            // Плотный план: восемь пилюль в слот не влезают, поэтому трека нет — только подпись.
            LyteExerciseCard(
                title = "Подъём на носки",
                accent = LyteAccent.Amber,
                glyph = LyteExerciseGlyph.Machine,
                variant = LyteExerciseCardVariant.Editor(onEdit = {}, onRemove = {}),
                setCount = 8,
                setsLabel = "8 подходов",
                onClick = {},
            )
            // Упражнение без подходов: трека нет, остаётся подпись.
            LyteExerciseCard(
                title = "Растяжка",
                accent = LyteAccent.Teal,
                glyph = LyteExerciseGlyph.Stretch,
                variant = LyteExerciseCardVariant.ReadOnly,
                setCount = 0,
                setsLabel = "Без подходов",
            )
        }
    }
}
