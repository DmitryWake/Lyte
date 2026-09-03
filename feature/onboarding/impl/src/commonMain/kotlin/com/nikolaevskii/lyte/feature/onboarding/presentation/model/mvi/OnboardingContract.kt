package com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi

import com.nikolaevskii.lyte.core.mvi.UiIntent
import com.nikolaevskii.lyte.core.mvi.UiState
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.OnboardingStep

/**
 * Обучение (кадры 0.0–0.4). Что рисовать, решает [content]; [isLeaving] — сквозной, потому что
 * выйти можно из **обоих** армов, и внутри одного из них guard от повторного тапа был бы
 * недостижим для другого. Тот же довод, по которому `isMutating` живёт снаружи армов активной
 * сессии.
 *
 * `Error`-арма нет намеренно. Единственная операция, способная упасть, — запись флага «обучение
 * пройдено», и её провал не должен запирать вход в приложение: пользователь уходит в трекер, а
 * худшее последствие — обучение покажется ещё раз. Арм для этого пришлось бы рисовать, покрывать
 * превью и объяснять пользователю ошибку, на которую он всё равно не может ответить.
 */
data class OnboardingUiState(
    val content: OnboardingContent = OnboardingContent.Welcome,
    val isLeaving: Boolean = false,
) : UiState {

    sealed interface OnboardingContent {

        /** Кадр 0.0: что это за приложение и два выхода — посмотреть тур или пропустить. */
        data object Welcome : OnboardingContent

        /** Кадр тура; [step] — индекс в [OnboardingStep.ALL]. */
        data class Tour(val step: Int) : OnboardingContent {

            val current: OnboardingStep get() = OnboardingStep.ALL[step]

            val isFirst: Boolean get() = step == 0

            val isLast: Boolean get() = step == OnboardingStep.ALL.lastIndex
        }
    }
}

sealed interface OnboardingIntent : UiIntent {

    /** «Показать как это работает» на приветствии. */
    data object OnStartTourClicked : OnboardingIntent

    /** «Далее»: следующий шаг, а с последнего — выход. */
    data object OnNextClicked : OnboardingIntent

    /** Системное «назад»: предыдущий шаг, а с первого — выход. */
    data object OnBackPressed : OnboardingIntent

    /** «Пропустить» — с любого кадра, включая приветствие. */
    data object OnSkipClicked : OnboardingIntent
}
