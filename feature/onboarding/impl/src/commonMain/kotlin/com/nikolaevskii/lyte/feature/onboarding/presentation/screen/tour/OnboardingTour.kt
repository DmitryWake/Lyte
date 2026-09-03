package com.nikolaevskii.lyte.feature.onboarding.presentation.screen.tour

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.component.overlay.LyteCoachMark
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.Res
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_coach_commit
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_coach_history
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_coach_start
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_coach_stepper
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.OnboardingStep
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingIntent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Ширина подсветки: реплики занимают всю ширину за вычетом полей, вырез повторяет её. */
private val HighlightWidthInset = ReplicaPaddingHorizontal

/** Высота карточки истории вместе с её месячным заголовком — подсвечивается пара целиком. */
private val HistoryHighlightHeight = ReplicaHistoryCardHeight

/**
 * Один шаг тура: статичная реплика экрана плюс коуч-марк поверх неё.
 *
 * [BoxWithConstraints] нужен ровно затем, чтобы знать высоту экрана: у шага 0.1 реплика
 * центрируется, поэтому её кнопка стоит не на фиксированном отступе сверху, а относительно центра.
 * Остальные три реплики выкладываются сверху вниз, и их прямоугольники — арифметика по константам
 * из `TourReplicas`, а не замер: подсветка не может разъехаться с тем, что нарисовано.
 */
@Composable
internal fun OnboardingTour(
    step: OnboardingStep,
    isLeaving: Boolean,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val highlightWidth = maxWidth - HighlightWidthInset * 2
        val containerHeight = maxHeight
        Box(modifier = Modifier.fillMaxSize()) {
            when (step) {
                OnboardingStep.Start -> StartReplica()
                OnboardingStep.Stepper, OnboardingStep.Commit -> SetReplica()
                OnboardingStep.History -> HistoryReplica()
            }
            LyteCoachMark(
                targetBounds = step.highlightBounds(
                    containerHeight = containerHeight,
                    highlightWidth = highlightWidth,
                ),
                text = stringResource(step.coachText()),
                stepIndex = OnboardingStep.ALL.indexOf(step),
                stepCount = OnboardingStep.ALL.size,
                // Пока идёт запись флага, тур перестаёт реагировать: иначе двойной тап по «Понятно»
                // на последнем шаге отправил бы вторую команду навигации.
                onNext = { if (!isLeaving) onIntent(OnboardingIntent.OnNextClicked) },
                onSkip = { if (!isLeaving) onIntent(OnboardingIntent.OnSkipClicked) },
            )
        }
    }
}

private fun OnboardingStep.coachText(): StringResource = when (this) {
    OnboardingStep.Start -> Res.string.onboarding_coach_start
    OnboardingStep.Stepper -> Res.string.onboarding_coach_stepper
    OnboardingStep.Commit -> Res.string.onboarding_coach_commit
    OnboardingStep.History -> Res.string.onboarding_coach_history
}

/**
 * Прямоугольник подсветки шага. Считается по тем же константам, которыми выложена реплика, — второй
 * набор чисел разъехался бы с первым молча, а заметен был бы только глазами на кадре.
 */
private fun OnboardingStep.highlightBounds(
    containerHeight: androidx.compose.ui.unit.Dp,
    highlightWidth: androidx.compose.ui.unit.Dp,
): DpRect = when (this) {
    OnboardingStep.Start -> DpRect(
        left = ReplicaPaddingHorizontal,
        top = startReplicaButtonTop(containerHeight),
        right = ReplicaPaddingHorizontal + highlightWidth,
        bottom = startReplicaButtonTop(containerHeight) + ReplicaTextActionHeight,
    )

    // Степперы — внутри карточки подхода, поэтому подсвечивается её нижняя половина.
    OnboardingStep.Stepper -> DpRect(
        left = ReplicaPaddingHorizontal,
        top = ReplicaTopGap + ReplicaSetRowHeight / 2,
        right = ReplicaPaddingHorizontal + highlightWidth,
        bottom = ReplicaTopGap + ReplicaSetRowHeight,
    )

    // Обе кнопки под карточкой — одним вырезом: шаг про выбор между ними, а не про каждую отдельно.
    OnboardingStep.Commit -> DpRect(
        left = ReplicaPaddingHorizontal,
        top = ReplicaTopGap + ReplicaSetRowHeight + ReplicaActionsGap,
        right = ReplicaPaddingHorizontal + highlightWidth,
        bottom = ReplicaTopGap + ReplicaSetRowHeight + ReplicaActionsGap * 2 +
            ReplicaPrimaryActionHeight + ReplicaTextActionHeight,
    )

    OnboardingStep.History -> DpRect(
        left = ReplicaPaddingHorizontal,
        top = ReplicaTopGap + ReplicaHistoryOverlineHeight,
        right = ReplicaPaddingHorizontal + highlightWidth,
        bottom = ReplicaTopGap + ReplicaHistoryOverlineHeight + HistoryHighlightHeight,
    )
}
