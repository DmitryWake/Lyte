package com.nikolaevskii.lyte.feature.onboarding.presentation.model

/**
 * Шаги тура (кадры 0.1–0.4). Список пуст: реплики экранов и тексты реплик — RD-31, и до неё тур
 * состоит из одного приветствия.
 *
 * Пустой список — не заглушка, а рабочее состояние: `OnboardingUiState.Tour` по нему недостижим,
 * «Показать как это работает» ведёт туда же, куда «Пропустить», и флаг пишется одинаково. Так
 * обучение целиком проверяемо уже сейчас, а RD-31 добавляет данные, не трогая переходы.
 */
data class OnboardingStep(
    val id: String,
) {

    companion object {

        val ALL: List<OnboardingStep> = emptyList()
    }
}
