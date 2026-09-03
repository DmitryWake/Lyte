package com.nikolaevskii.lyte.feature.onboarding.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.brand.LyteWordmark
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.Res
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_description
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_skip
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_start_tour
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_tagline
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingIntent
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingUiState
import com.nikolaevskii.lyte.feature.onboarding.presentation.model.mvi.OnboardingUiState.OnboardingContent
import com.nikolaevskii.lyte.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val WordmarkFontSize = 44.sp
private val ContentPaddingHorizontal = 32.dp
private val TaglineSpacing = 12.dp
private val DescriptionSpacing = 20.dp
private val ActionsGap = 8.dp

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingContent(state = state, onIntent = viewModel::onIntent)
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun OnboardingContent(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Системное «назад» — часть обучения, а не выход мимо него: без перехвата пользователь ушёл бы
    // с экрана, не записав флаг, и приветствие встретило бы его снова на следующем запуске.
    BackHandler(enabled = !state.isLeaving) {
        onIntent(OnboardingIntent.OnBackPressed)
    }
    Scaffold(modifier = modifier) { padding ->
        when (state.content) {
            OnboardingContent.Welcome -> OnboardingWelcome(
                isLeaving = state.isLeaving,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            is OnboardingContent.Tour -> Unit
        }
    }
}

@Composable
private fun OnboardingWelcome(
    isLeaving: Boolean,
    onIntent: (OnboardingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = ContentPaddingHorizontal),
    ) {
        LyteWordmark(fontSize = WordmarkFontSize)
        Spacer(modifier = Modifier.height(TaglineSpacing))
        Text(
            text = stringResource(Res.string.onboarding_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(DescriptionSpacing))
        Text(
            text = stringResource(Res.string.onboarding_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(LyteTheme.spacing.s10))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ActionsGap),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Обе кнопки гасятся на время записи флага: она ведёт к переходу, и второй тап был бы
            // мёртвым — экран на него уже не отвечает.
            LyteButton(
                text = stringResource(Res.string.onboarding_start_tour),
                onClick = { onIntent(OnboardingIntent.OnStartTourClicked) },
                size = LyteButtonSize.Large,
                enabled = !isLeaving,
                fullWidth = true,
            )
            LyteButton(
                text = stringResource(Res.string.onboarding_skip),
                onClick = { onIntent(OnboardingIntent.OnSkipClicked) },
                variant = LyteButtonVariant.Text,
                enabled = !isLeaving,
                fullWidth = true,
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingWelcomePreview() {
    LyteTheme {
        OnboardingContent(state = OnboardingUiState(), onIntent = {})
    }
}

@Preview
@Composable
private fun OnboardingWelcomeLeavingPreview() {
    LyteTheme {
        OnboardingContent(state = OnboardingUiState(isLeaving = true), onIntent = {})
    }
}
