package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone

private val ExerciseSetListGutter = 20.dp
private val ExerciseSetListGap = 6.dp
private val ExerciseSetListMaskHeight = 20.dp
private val ExerciseSetListEnterOffset = 8.dp

/**
 * Насколько низ фокус-карточки поднят над низом списка. Один и тот же зазор при одном подходе и при
 * восьми — палец попадает в одно и то же место.
 */
private val ExerciseSetListAnchor = 104.dp

/** Текущего подхода в списке нет: все подходы упражнения уже закрыты. */
private const val NoFocusIndex = -1

private val PreviewListWidth = 390.dp
private val PreviewListHeight = 560.dp
private const val PreviewTargetReps = 10
private const val PreviewWeight = 62.5

/** Индекс текущего подхода и высота «головы» списка, опубликованные одним измерением. */
private data class SetListAnchor(val focusIndex: Int, val headHeightPx: Int)

/**
 * Список подходов упражнения — композиция экрана трекинга: все подходы по порядку, текущий —
 * фокус-карточка, закреплённая у нижнего края области. В [sets] ожидается **не больше одного**
 * [LyteTrackSetState.Focus]; всё до него включительно образует «голову» списка, остальное — хвост.
 *
 * Зачем закреплять. Фокус-карточка занимает ~200dp, спокойная строка — 36dp, поэтому после шести-семи
 * подходов список выше доступного места. В обычном скроллере уезжает именно карточка — единственный
 * элемент, которого человек касается, — а этого посреди подхода происходить не должно.
 *
 * В вебе это `position: sticky; bottom: 0`. В Compose sticky снизу нет, поэтому переносится
 * **поведение**, а не приём: один скроллер, карточка — обычный элемент, а её положение доводится
 * программной прокруткой. Пока список короткий, карточку держит на якоре верхняя распорка
 * `max(0, высота области − 104dp − высота головы)` и прокрутка не нужна вовсе; дальше её место
 * занимает прокрутка. Распорка считается **в том же measure-проходе**, что и голова: если брать
 * высоту из состояния прошлого layout, между закрытием подхода и рекомпозицией остаётся кадр, в
 * котором карточка стоит на строку ниже якоря и потом скачком возвращается.
 *
 * Нижняя распорка ровно в [ExerciseSetListAnchor] — не косметика, а условие достижимости якоря:
 * без неё на последнем подходе карточке некуда доехать.
 *
 * Компоненту нужна **ограниченная по высоте** область (`weight(1f)`, `height(...)`, `fillMaxSize()`):
 * от неё считается якорь. В неограниченной высоте компонент честно вырождается в обычную колонку.
 *
 * Горизонтальные поля (20dp) держит сам список: маска верхнего края рисуется в offscreen-слое,
 * который клипует строго по границам, и без полей срезал бы боковую тень фокус-карточки.
 *
 * Альтернативой была горизонтальная карусель плашек — она отклонена дизайном: просит боковой свайп у
 * человека, стоящего под штангой, и не показывает факт и цель в одном взгляде.
 */
@Composable
fun LyteExerciseSetList(
    sets: List<LyteTrackSetState>,
    onRepsChange: (Int) -> Unit,
    onWeightChange: (Double) -> Unit,
    repsStep: Int = 1,
    weightStep: Double = 2.5,
    focusContent: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    scrollState: ScrollState = rememberScrollState(),
    modifier: Modifier = Modifier,
) {
    val motion = LyteTheme.motion
    val focusIndex = sets.indexOfFirst { state -> state is LyteTrackSetState.Focus }
    val headLastIndex = if (focusIndex >= 0) focusIndex else sets.lastIndex
    val tail = sets.drop(headLastIndex + 1)

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val viewportHeightPx = if (constraints.hasBoundedHeight) constraints.maxHeight else 0
        val anchorPx = with(density) { ExerciseSetListAnchor.roundToPx() }
        val enterOffsetPx = with(density) { ExerciseSetListEnterOffset.toPx() }
        val maskHeightPx = with(density) { ExerciseSetListMaskHeight.toPx() }
        var anchorState by remember { mutableStateOf(SetListAnchor(focusIndex = NoFocusIndex, headHeightPx = 0)) }
        var settledFocusIndex by remember { mutableIntStateOf(NoFocusIndex) }
        val maskBrush = remember(maskHeightPx) {
            Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black), startY = 0f, endY = maskHeightPx)
        }

        LaunchedEffect(anchorState, viewportHeightPx) {
            val headHeightPx = anchorState.headHeightPx
            if (headHeightPx == 0 || viewportHeightPx == 0) {
                return@LaunchedEffect
            }
            // Клампом по maxValue не занимаемся: он может быть измерен до перерасчёта, а scrollTo
            // зажимает сам. Верхняя распорка уже вытолкнула голову на якорь, если список короткий, —
            // тогда цель нулевая.
            val target = (headHeightPx - (viewportHeightPx - anchorPx)).coerceAtLeast(0)
            // Анимируется только смена подхода. Первый проход и изменение размеров области
            // (клавиатура, поворот) доводятся мгновенно: там анимация читается как лаг, а не как связь.
            val isSetChange = settledFocusIndex != NoFocusIndex && settledFocusIndex != anchorState.focusIndex
            settledFocusIndex = anchorState.focusIndex
            if (isSetChange) {
                scrollState.animateScrollTo(
                    value = target,
                    animationSpec = tween(durationMillis = motion.durationMedium, easing = motion.easingEmphasized),
                )
            } else {
                scrollState.scrollTo(target)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(brush = maskBrush, blendMode = BlendMode.DstIn)
                }
                .verticalScroll(scrollState)
                .padding(horizontal = ExerciseSetListGutter),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ExerciseSetListGap),
                modifier = Modifier.layout { measurable, headConstraints ->
                    val placeable = measurable.measure(headConstraints)
                    val topPadding = (viewportHeightPx - anchorPx - placeable.height).coerceAtLeast(0)
                    anchorState = SetListAnchor(focusIndex = focusIndex, headHeightPx = placeable.height)
                    layout(placeable.width, placeable.height + topPadding) {
                        placeable.place(x = 0, y = topPadding)
                    }
                },
            ) {
                sets.take(headLastIndex + 1).forEachIndexed { index, state ->
                    key(index) {
                        AppearingSetRow(offsetPx = enterOffsetPx, isEnabled = state is LyteTrackSetState.Quiet) {
                            LyteTrackSetRow(
                                number = index + 1,
                                state = state,
                                onRepsChange = onRepsChange,
                                onWeightChange = onWeightChange,
                                repsStep = repsStep,
                                weightStep = weightStep,
                                focusContent = focusContent.takeIf { state is LyteTrackSetState.Focus },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
            if (tail.isNotEmpty() || footer != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(ExerciseSetListGap),
                    modifier = Modifier.padding(top = ExerciseSetListGap),
                ) {
                    tail.forEachIndexed { index, state ->
                        val number = headLastIndex + index + 2
                        key(number) {
                            LyteTrackSetRow(
                                number = number,
                                state = state,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    footer?.invoke()
                }
            }
            Spacer(modifier = Modifier.height(ExerciseSetListAnchor))
        }
    }
}

/**
 * Выполненная строка не подменяет фокус-карточку рывком: она проявляется и подъезжает на
 * [offsetPx]. Анимация запускается на первой композиции строки — то есть ровно в тот момент, когда
 * подход закрыт и карточка уехала на следующий.
 */
@Composable
private fun AppearingSetRow(offsetPx: Float, isEnabled: Boolean, content: @Composable () -> Unit) {
    if (!isEnabled) {
        content()
    } else {
        val motion = LyteTheme.motion
        val progress = remember { Animatable(initialValue = 0f) }
        LaunchedEffect(Unit) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = motion.durationMedium, easing = motion.easingEmphasized),
            )
        }
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * offsetPx
            },
        ) {
            content()
        }
    }
}

private fun previewSets(setCount: Int, focusNumber: Int): List<LyteTrackSetState> =
    List(setCount) { index ->
        val number = index + 1
        when {
            number < focusNumber -> LyteTrackSetState.Quiet(
                tone = previewTone(number),
                value = LyteSetValue(reps = previewReps(number), weight = PreviewWeight),
                note = if (number == 2) "Локти разъезжались, следующий подход в том же весе" else null,
            )

            number == focusNumber -> LyteTrackSetState.Focus(
                setCount = setCount,
                reps = PreviewTargetReps,
                weight = PreviewWeight,
                references = listOf(
                    LyteTrackSetReference.Target(value = "10×62.5 кг"),
                    LyteTrackSetReference.LastTime(value = "10×60 кг"),
                ),
            )

            else -> LyteTrackSetState.Quiet(
                tone = LyteProgressTone.Todo,
                value = LyteSetValue(reps = PreviewTargetReps, weight = PreviewWeight),
            )
        }
    }

private fun previewTone(number: Int): LyteProgressTone = when (number % 4) {
    0 -> LyteProgressTone.Skipped
    1 -> LyteProgressTone.Met
    2 -> LyteProgressTone.Negative
    else -> LyteProgressTone.Positive
}

private fun previewReps(number: Int): Int = when (previewTone(number)) {
    LyteProgressTone.Positive -> PreviewTargetReps + 2
    LyteProgressTone.Negative -> PreviewTargetReps - 2
    else -> PreviewTargetReps
}

@Composable
private fun PreviewSetList(sets: List<LyteTrackSetState>, footer: (@Composable () -> Unit)? = null) {
    LyteTheme {
        Box(
            modifier = Modifier
                .size(width = PreviewListWidth, height = PreviewListHeight)
                .background(MaterialTheme.colorScheme.surface),
        ) {
            LyteExerciseSetList(
                sets = sets,
                onRepsChange = {},
                onWeightChange = {},
                footer = footer,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Preview
@Composable
private fun LyteExerciseSetListPreview() {
    PreviewSetList(sets = previewSets(setCount = 3, focusNumber = 2))
}

@Preview
@Composable
private fun LyteExerciseSetListFirstSetPreview() {
    PreviewSetList(sets = previewSets(setCount = 8, focusNumber = 1))
}

@Preview
@Composable
private fun LyteExerciseSetListMiddleSetPreview() {
    PreviewSetList(sets = previewSets(setCount = 8, focusNumber = 5))
}

@Preview
@Composable
private fun LyteExerciseSetListLastSetPreview() {
    PreviewSetList(
        sets = previewSets(setCount = 8, focusNumber = 8),
        footer = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            ) {
                LyteOverline(text = "Последний подход")
            }
        },
    )
}

@Preview
@Composable
private fun LyteExerciseSetListAllDonePreview() {
    PreviewSetList(sets = previewSets(setCount = 5, focusNumber = 6))
}

@Preview
@Composable
private fun LyteExerciseSetListBodyweightPreview() {
    PreviewSetList(
        sets = listOf(
            LyteTrackSetState.Quiet(tone = LyteProgressTone.Met, value = LyteSetValue(reps = 12)),
            LyteTrackSetState.Quiet(tone = LyteProgressTone.Skipped),
            LyteTrackSetState.Focus(
                setCount = 4,
                reps = 12,
                references = listOf(LyteTrackSetReference.Target(value = "12 повт")),
            ),
            LyteTrackSetState.Quiet(tone = LyteProgressTone.Todo, value = LyteSetValue(reps = 12)),
        ),
    )
}
