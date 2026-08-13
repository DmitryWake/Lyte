package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import kotlin.math.roundToInt

/** Низ фокус-карточки всегда на этом расстоянии от низа области — палец попадает в одно место. */
private val ExerciseSetListAnchorInset = 104.dp

private val ExerciseSetListGap = 6.dp
private val ExerciseSetListCurrentMargin = 6.dp
private val ExerciseSetListTopMaskHeight = 20.dp
private val ExerciseSetListEnterShift = 8.dp

private const val ExerciseSetListUnmeasured = -1
private const val ExerciseSetListNoIndex = -1

private val ExerciseSetListPreviewHeight = 520.dp
private val ExerciseSetListPreviewPadding = 20.dp

/**
 * Список всех подходов активного упражнения: спокойные строки по порядку и фокус-карточка текущего
 * подхода среди них. Композиция экрана тренировки целиком.
 *
 * Решает проблему обычного списка: фокус-карточка — единственный элемент, которого касаются посреди
 * подхода, и она не должна уезжать за экран. В вебе это `position: sticky; bottom: 0`, в Compose
 * такого нет, поэтому переносится **поведение**: список один, карточка — обычный элемент, а её
 * позиция доводится программной прокруткой так, чтобы низ карточки был в [ExerciseSetListAnchorInset]
 * от низа области. Выполненные подходы уходят выше и остаются в одном движении пальца, будущие —
 * ниже и в настоящем порядке.
 *
 * Первая установка позиции — мгновенная, смена подхода — с анимацией: иначе список «подъезжал» бы
 * при каждом появлении экрана. Отбивка сверху — маска-градиент [ExerciseSetListTopMaskHeight],
 * поэтому строки не обрезаются краем, а растворяются в нём.
 *
 * Список не ленивый (`Column` + `verticalScroll`): подходов в упражнении единицы, зато позиции всех
 * элементов известны сразу — на них и держится якорь.
 *
 * Требует **ограниченную по высоте** область (`Modifier.weight(1f)` в колонке экрана или явная
 * высота): якорь считается от её нижнего края. В неограниченной по высоте области список работает
 * как обычная колонка, без прокрутки и якоря.
 *
 * [currentContent] — слот под заметку или чип внизу фокус-карточки; [footer] — хвост списка
 * (например, `LyteOverline` «Последний подход»), он скроллится вместе с подходами.
 */
@Composable
fun LyteExerciseSetList(
    sets: List<LyteTrackSetState>,
    onRepsChange: (Int) -> Unit = {},
    onWeightChange: (Double) -> Unit = {},
    currentContent: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val motion = LyteTheme.motion
    val scrollState = rememberScrollState()
    val currentIndex = sets.indexOfFirst { it is LyteTrackSetState.Current }
    var currentBottom by remember { mutableIntStateOf(ExerciseSetListUnmeasured) }
    var anchoredIndex by remember { mutableIntStateOf(ExerciseSetListNoIndex) }

    BoxWithConstraints(modifier = modifier) {
        val viewportHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else 0
        val anchorInset = with(LocalDensity.current) { ExerciseSetListAnchorInset.roundToPx() }
        val topSpacerHeight = with(LocalDensity.current) { viewportHeight.toDp() }

        // Ключ включает измеренный низ карточки: она растёт, когда появляется заметка, и якорь
        // обязан поехать следом.
        LaunchedEffect(currentIndex, currentBottom, viewportHeight) {
            if (currentIndex < 0 || currentBottom < 0 || viewportHeight <= 0) {
                return@LaunchedEffect
            }
            val target = (currentBottom - viewportHeight + anchorInset).coerceIn(0, scrollState.maxValue)
            val jumped = anchoredIndex != ExerciseSetListNoIndex && anchoredIndex != currentIndex
            anchoredIndex = currentIndex
            if (jumped) {
                scrollState.animateScrollTo(
                    value = target,
                    animationSpec = tween(
                        durationMillis = motion.durationMedium,
                        easing = motion.easingDecelerate,
                    ),
                )
            } else {
                scrollState.scrollTo(target)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(ExerciseSetListGap),
            modifier = Modifier
                .fillMaxSize()
                .topEdgeMask(ExerciseSetListTopMaskHeight)
                .verticalScroll(scrollState),
        ) {
            // Пустое место над первым подходом: без него карточку первого подхода нечем опустить
            // к якорю — прокручивать было бы нечего.
            Spacer(modifier = Modifier.height(topSpacerHeight))
            sets.forEachIndexed { index, state ->
                when (state) {
                    is LyteTrackSetState.Current -> Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                currentBottom = coordinates.positionInParent().y.roundToInt() +
                                    coordinates.size.height
                            }
                            .fillMaxWidth()
                            // Запас под тень карточки: сосед снизу нарисовался бы поверх неё.
                            .padding(vertical = ExerciseSetListCurrentMargin),
                    ) {
                        LyteTrackSetRow(
                            number = index + 1,
                            state = state,
                            onRepsChange = onRepsChange,
                            onWeightChange = onWeightChange,
                            content = currentContent,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    is LyteTrackSetState.Resting -> LyteTrackSetRow(
                        number = index + 1,
                        state = state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (index < currentIndex) Modifier.enterFromBelow() else Modifier),
                    )
                }
            }
            footer?.invoke()
            Spacer(modifier = Modifier.height(ExerciseSetListAnchorInset))
        }
    }
}

/**
 * Появление только что выполненной строки: проявление со сдвигом снизу. Двигает содержимое слоем,
 * а не размером, поэтому высота списка не пляшет и якорь карточки не дрожит.
 */
@Composable
private fun Modifier.enterFromBelow(): Modifier {
    val motion = LyteTheme.motion
    val shift = with(LocalDensity.current) { ExerciseSetListEnterShift.toPx() }
    var entered by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = motion.durationMedium, easing = motion.easingEmphasized),
        label = "trackSetRowEnter",
    )
    LaunchedEffect(Unit) { entered = true }
    return graphicsLayer {
        alpha = progress
        translationY = (1f - progress) * shift
    }
}

/** Эквивалент CSS-маски верхнего края: строки не обрезаются границей списка, а растворяются в ней. */
private fun Modifier.topEdgeMask(height: Dp): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startY = 0f,
                endY = height.toPx(),
            ),
            blendMode = BlendMode.DstIn,
        )
    }

@Composable
private fun SetListPreviewSurface(sets: List<LyteTrackSetState>, footer: (@Composable () -> Unit)? = null) {
    LyteTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            LyteExerciseSetList(
                sets = sets,
                footer = footer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ExerciseSetListPreviewHeight)
                    .padding(horizontal = ExerciseSetListPreviewPadding),
            )
        }
    }
}

private fun previewLongSets(currentIndex: Int): List<LyteTrackSetState> = List(size = 8) { index ->
    when {
        index == currentIndex -> LyteTrackSetState.Current(
            total = 8,
            reps = 10,
            weight = 62.5,
            target = "10×62.5 кг",
            last = "10×60 кг",
        )

        index > currentIndex -> LyteTrackSetState.Resting(
            tone = LyteProgressTone.Todo,
            target = "10×62.5 кг",
        )

        index % 3 == 0 -> LyteTrackSetState.Resting(tone = LyteProgressTone.Positive, reps = 12, weight = 62.5)
        index % 3 == 1 -> LyteTrackSetState.Resting(tone = LyteProgressTone.Met, reps = 10, weight = 62.5)
        else -> LyteTrackSetState.Resting(tone = LyteProgressTone.Skipped)
    }
}

@Preview
@Composable
private fun LyteExerciseSetListShortPreview() {
    SetListPreviewSurface(
        sets = listOf(
            LyteTrackSetState.Resting(tone = LyteProgressTone.Met, reps = 10, weight = 60.0),
            LyteTrackSetState.Current(total = 3, reps = 10, weight = 62.5, target = "10×62.5 кг", last = "10×60 кг"),
            LyteTrackSetState.Resting(tone = LyteProgressTone.Todo, target = "10×62.5 кг"),
        ),
    )
}

@Preview
@Composable
private fun LyteExerciseSetListFirstPreview() {
    SetListPreviewSurface(sets = previewLongSets(currentIndex = 0))
}

@Preview
@Composable
private fun LyteExerciseSetListMiddlePreview() {
    SetListPreviewSurface(
        sets = previewLongSets(currentIndex = 4).mapIndexed { index, state ->
            if (index == 2 && state is LyteTrackSetState.Resting) {
                state.copy(note = "Пояс затянул туго — на следующем подходе ослабить и добавить 2.5 кг")
            } else {
                state
            }
        },
    )
}

@Preview
@Composable
private fun LyteExerciseSetListLastPreview() {
    SetListPreviewSurface(
        sets = previewLongSets(currentIndex = 7),
        footer = {
            Box(modifier = Modifier.fillMaxWidth().padding(top = ExerciseSetListGap)) {
                LyteOverline(text = "Последний подход")
            }
        },
    )
}
