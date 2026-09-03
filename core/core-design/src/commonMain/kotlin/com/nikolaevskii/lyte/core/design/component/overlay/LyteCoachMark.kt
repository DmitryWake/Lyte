package com.nikolaevskii.lyte.core.design.component.overlay

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.coach_mark_done
import com.nikolaevskii.lyte.core.design.generated.resources.coach_mark_next
import com.nikolaevskii.lyte.core.design.generated.resources.coach_mark_skip
import org.jetbrains.compose.resources.stringResource
import kotlin.math.min

/** Воздух вокруг подсвечиваемого элемента: вырез шире цели на эту величину с каждой стороны. */
private val CoachMarkCutoutPadding = 8.dp

/** Потолок скругления выреза; у низких целей радиус ограничен половиной высоты — получается пилюля. */
private val CoachMarkCutoutMaxCornerRadius = 28.dp
private val CoachMarkCalloutMaxWidth = 300.dp
private val CoachMarkCalloutMargin = 12.dp
private val CoachMarkCalloutGap = 14.dp
private val CoachMarkCalloutPadding = 16.dp
private val CoachMarkTitleGap = 6.dp
private val CoachMarkActionsTopPadding = 14.dp

/**
 * Зазор «Пропустить» ↔ основная кнопка. Меньше макетных 14dp намеренно: в макете это голые
 * текстовые спаны, а здесь обе — [LyteButton], и их собственные горизонтальные паддинги (20dp)
 * уже дают воздух между подписями.
 */
private val CoachMarkActionsGap = 4.dp
private val CoachMarkStepDotSize = 6.dp
private val CoachMarkStepDotActiveWidth = 18.dp
private val CoachMarkStepDotGap = 6.dp

/** Плотность скрима из макета (`rgba(13,16,10,0.64)`) поверх роли `colorScheme.scrim`. */
private const val CoachMarkScrimAlpha = 0.64f

/**
 * Подсказка обучения: затемнение всего экрана с вырезом вокруг одного элемента плюс каллаут с
 * текстом, точками-пейджером и действиями «Пропустить» / «Далее».
 *
 * **Координаты выреза приходят параметром — компонент ничего не измеряет.** [targetBounds] задаётся
 * в системе координат самого коуч-марка, поэтому его кладут в тот же `Box`, что и подсвечиваемый
 * элемент: тогда начала координат совпадают. Замера нет намеренно (решение 8 роадмапа
 * `design-v2`): подсвечиваются статичные реплики экранов, у которых прямоугольник известен на этапе
 * вёрстки, а контракт якорей поверх живых экранов ломался бы молча при их рефакторинге.
 *
 * **Все касания в границах компонента гасятся** — включая касание внутри выреза: подсвеченный
 * элемент под скримом нерабочий, и «подсветили кнопку, а она не нажимается» лучше, чем случайный
 * тап по реплике. Управление туром идёт только через [onNext] и [onSkip].
 *
 * На последнем шаге ([stepIndex] `>=` [stepCount]` - 1`) основная кнопка подписана «Понятно», а
 * «Пропустить» не показывается: пропускать уже нечего. Правило живёт здесь, а не у вызывающего, —
 * номер шага компонент и так знает, он нужен ему для точек.
 *
 * Видимостью управляет вызывающая сторона самим фактом композиции (`if (tourVisible) { … }`), как
 * у [LyteDialog][com.nikolaevskii.lyte.core.design.component.feedback.LyteDialog] и
 * [LyteBottomSheet]: флага видимости у компонента нет.
 */
@Composable
fun LyteCoachMark(
    targetBounds: DpRect,
    text: String,
    stepIndex: Int,
    stepCount: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
) {
    val motion = LyteTheme.motion
    val density = LocalDensity.current
    val scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = CoachMarkScrimAlpha)
    // Вырез и каллаут переезжают между шагами одним движением — отсюда общий spec.
    val cutout = animateRectAsState(
        targetValue = density.cutoutRect(targetBounds),
        animationSpec = tween(
            durationMillis = motion.durationLong,
            easing = motion.easingEmphasized,
        ),
    )
    val scrimPath = remember { Path() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .blockAllPointerInput(),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Чтение анимированного значения отложено в draw: кадр анимации не рекомпозирует дерево.
            drawScrimWithCutout(cutout = cutout.value, color = scrimColor, path = scrimPath)
        }
        Layout(
            content = {
                CoachMarkCallout(
                    text = text,
                    title = title,
                    stepIndex = stepIndex,
                    stepCount = stepCount,
                    onNext = onNext,
                    onSkip = onSkip,
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { measurables, constraints ->
            val margin = CoachMarkCalloutMargin.roundToPx()
            val gap = CoachMarkCalloutGap.roundToPx()
            val calloutWidth = min(
                CoachMarkCalloutMaxWidth.roundToPx(),
                constraints.maxWidth - margin * 2,
            ).coerceAtLeast(0)
            val callout = measurables.first().measure(
                Constraints(
                    minWidth = calloutWidth,
                    maxWidth = calloutWidth,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight,
                ),
            )
            val offset = lyteCoachMarkCalloutOffset(
                // Чтение анимированного значения отложено в measure: кадр анимации не рекомпозирует
                // дерево, а только переставляет каллаут.
                cutout = cutout.value,
                calloutSize = IntSize(width = callout.width, height = callout.height),
                containerSize = IntSize(width = constraints.maxWidth, height = constraints.maxHeight),
                gap = gap,
                margin = margin,
            )
            layout(width = constraints.maxWidth, height = constraints.maxHeight) {
                callout.place(offset)
            }
        }
    }
}

@Composable
private fun CoachMarkCallout(
    text: String,
    title: String?,
    stepIndex: Int,
    stepCount: Int,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lastStep = stepIndex >= stepCount - 1
    val nextLabel = if (lastStep) {
        stringResource(Res.string.coach_mark_done)
    } else {
        stringResource(Res.string.coach_mark_next)
    }
    Surface(
        shape = LyteTheme.extendedShapes.largeIncreased,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level3,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(CoachMarkCalloutPadding)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(CoachMarkTitleGap))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = CoachMarkActionsTopPadding),
            ) {
                CoachMarkStepDots(stepIndex = stepIndex, stepCount = stepCount)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(CoachMarkActionsGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!lastStep) {
                        LyteButton(
                            text = stringResource(Res.string.coach_mark_skip),
                            onClick = onSkip,
                            variant = LyteButtonVariant.Text,
                            size = LyteButtonSize.Small,
                        )
                    }
                    LyteButton(text = nextLabel, onClick = onNext, size = LyteButtonSize.Small)
                }
            }
        }
    }
}

/**
 * Пейджер тура: по точке на шаг, текущая — вытянутая пилюля. Не кликается — шаг переключают
 * кнопками, иначе тур получил бы второй, невидимый для новичка способ навигации.
 *
 * Не переиспользует [LyteSetDots][com.nikolaevskii.lyte.core.design.component.session.LyteSetDots]:
 * тот кодирует исход подхода (попал / промазал / пропустил) и меряется в других размерах — общего
 * здесь только слово «точки».
 */
@Composable
private fun CoachMarkStepDots(
    stepIndex: Int,
    stepCount: Int,
    modifier: Modifier = Modifier,
) {
    val motion = LyteTheme.motion
    Row(
        horizontalArrangement = Arrangement.spacedBy(CoachMarkStepDotGap),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        repeat(stepCount) { index ->
            val active = index == stepIndex
            val dotWidth by animateDpAsState(
                targetValue = if (active) CoachMarkStepDotActiveWidth else CoachMarkStepDotSize,
                animationSpec = tween(
                    durationMillis = motion.durationLong,
                    easing = motion.easingEmphasized,
                ),
            )
            val dotColor = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(CoachMarkStepDotSize)
                    .clip(LyteTheme.extendedShapes.full)
                    .background(dotColor),
            )
        }
    }
}

private fun Density.cutoutRect(targetBounds: DpRect): Rect = Rect(
    left = targetBounds.left.toPx(),
    top = targetBounds.top.toPx(),
    right = targetBounds.right.toPx(),
    bottom = targetBounds.bottom.toPx(),
).inflate(CoachMarkCutoutPadding.toPx())

/**
 * Скрим одним слоем с дыркой: заливка во весь размер и вырез в том же пути с [PathFillType.EvenOdd].
 * Не два прямоугольника и не `BlendMode.Clear` — второе потребовало бы отдельного offscreen-слоя.
 */
private fun DrawScope.drawScrimWithCutout(cutout: Rect, color: Color, path: Path) {
    val cornerRadius = min(CoachMarkCutoutMaxCornerRadius.toPx(), cutout.height / 2)
    path.rewind()
    path.fillType = PathFillType.EvenOdd
    path.addRect(Rect(offset = Offset.Zero, size = size))
    path.addRoundRect(RoundRect(rect = cutout, cornerRadius = CornerRadius(cornerRadius)))
    drawPath(path = path, color = color)
}

/**
 * Гасит любое касание в границах компонента. Гашение в основном (не [Initial][
 * androidx.compose.ui.input.pointer.PointerEventPass.Initial]) проходе и на корне оверлея:
 * кнопки каллаута лежат глубже и получают событие раньше, поэтому продолжают работать, а всё,
 * что под оверлеем, видит уже поглощённое изменение.
 */
private fun Modifier.blockAllPointerInput(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            awaitPointerEvent().changes.forEach { it.consume() }
        }
    }
}

private val PreviewTargetTop = DpRect(left = 20.dp, top = 96.dp, right = 220.dp, bottom = 152.dp)

/** Цель у нижнего края: каллаут под неё не помещается и уходит наверх. */
private val PreviewTargetBottom = DpRect(left = 20.dp, top = 700.dp, right = 373.dp, bottom = 760.dp)

/** Фон превью: подсвечиваемый элемент ровно в границах [target], чтобы вырез лёг на него. */
@Composable
private fun CoachMarkPreviewFrame(target: DpRect, content: @Composable () -> Unit) {
    LyteTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = target.left, y = target.top)
                    .size(width = target.width, height = target.height)
                    .clip(LyteTheme.extendedShapes.largeIncreased)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )
            content()
        }
    }
}

@Preview
@Composable
private fun LyteCoachMarkPreview() {
    CoachMarkPreviewFrame(target = PreviewTargetTop) {
        LyteCoachMark(
            targetBounds = PreviewTargetTop,
            text = "Нажмите «Начать» — выберите программу и запустите тренировку.",
            stepIndex = 0,
            stepCount = 4,
            onNext = {},
            onSkip = {},
        )
    }
}

@Preview
@Composable
private fun LyteCoachMarkAboveTargetPreview() {
    CoachMarkPreviewFrame(target = PreviewTargetBottom) {
        LyteCoachMark(
            targetBounds = PreviewTargetBottom,
            text = "«Готово» — подход записан, дальше следующий. «Пропустить» — если решили его не делать.",
            stepIndex = 2,
            stepCount = 4,
            onNext = {},
            onSkip = {},
        )
    }
}

@Preview
@Composable
private fun LyteCoachMarkLastStepPreview() {
    CoachMarkPreviewFrame(target = PreviewTargetTop) {
        LyteCoachMark(
            targetBounds = PreviewTargetTop,
            text = "Каждая тренировка сохраняется здесь — открывайте, чтобы увидеть подробные результаты.",
            stepIndex = 3,
            stepCount = 4,
            onNext = {},
            onSkip = {},
        )
    }
}

@Preview
@Composable
private fun LyteCoachMarkWithTitlePreview() {
    CoachMarkPreviewFrame(target = PreviewTargetTop) {
        LyteCoachMark(
            targetBounds = PreviewTargetTop,
            text = "Меняйте повторения и вес: нажимайте на ± или на само число.",
            stepIndex = 1,
            stepCount = 4,
            onNext = {},
            onSkip = {},
            title = "Степперы",
        )
    }
}

@Preview
@Composable
private fun LyteCoachMarkLongTextPreview() {
    CoachMarkPreviewFrame(target = PreviewTargetTop) {
        LyteCoachMark(
            targetBounds = PreviewTargetTop,
            text = "Меняйте повторения и вес: нажимайте на ± или на само число, чтобы ввести точное " +
                "значение. Записанный подход попадёт в историю сразу, а цель на следующую " +
                "тренировку подтянется под факт, когда вы завершите эту.",
            stepIndex = 1,
            stepCount = 4,
            onNext = {},
            onSkip = {},
        )
    }
}
