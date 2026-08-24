package com.nikolaevskii.lyte.feature.splash.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.brand.LyteWordmark
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_EXIT_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.generated.resources.Res
import com.nikolaevskii.lyte.feature.splash.generated.resources.splash_error_message
import com.nikolaevskii.lyte.feature.splash.generated.resources.splash_retry
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashIntent
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashUiState
import com.nikolaevskii.lyte.feature.splash.presentation.viewmodel.SplashViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val ErrorWordmarkFontSize = 40.sp

// Кегль вордмарка на сплэше — адаптивный: держим долю ширины экрана (а не фиксированный sp,
// который на разных экранах выглядит по-разному). Отношение advance("Lyte.") / кегль в Inter Tight
// Bold = 2.31 (измерено по шрифту), поэтому кегль = ширина * доля / 2.31, зажатый в [min; max].
private const val WordmarkWidthFraction = 0.40f
private const val WordmarkAdvanceToSizeRatio = 2.31f
private val MinWordmarkFontSize = 48.dp
private val MaxWordmarkFontSize = 84.dp

private const val BreathHalfCycleMs = 1000
private const val HiddenAlpha = 0f
private const val FullAlpha = 1f
private const val MinBreathAlpha = 0.32f
private const val ExitEndScale = 1.06f

private const val DotBreathTransitionLabel = "splashDotBreath"
private const val DotBreathAlphaLabel = "splashDotBreathAlpha"

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SplashContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun SplashContent(
    state: SplashUiState,
    onIntent: (SplashIntent) -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center,
        ) {
            if (state is SplashUiState.Error) {
                SplashErrorContent(
                    onIntent = onIntent,
                    modifier = Modifier.padding(horizontal = LyteTheme.spacing.s8),
                )
            } else {
                SplashBrandContent(isExiting = state is SplashUiState.Exiting)
            }
        }
    }
}

/**
 * Единый жест: вордмарк «Lyte.» присутствует сразу (без entrance-fade — иначе на холодном старте
 * между системным сплэшем и Compose мелькал бы пустой сурфейс); лаймовая точка мягко «дышит», пока
 * идут стартовые процессы; на выходе весь вордмарк растворяется с лёгким подъёмом (lift), под это
 * уходит навигация. Отдельной «крупной точки» больше нет — марка совпадает с иконкой приложения.
 */
@Composable
private fun SplashBrandContent(isExiting: Boolean) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current
        val fontSize = remember(maxWidth, density) {
            val raw = maxWidth.value * WordmarkWidthFraction / WordmarkAdvanceToSizeRatio
            val clamped = raw.coerceIn(MinWordmarkFontSize.value, MaxWordmarkFontSize.value)
            with(density) { clamped.dp.toSp() }
        }

        val exit = remember { Animatable(HiddenAlpha) }
        LaunchedEffect(isExiting) {
            if (isExiting) {
                exit.animateTo(
                    targetValue = FullAlpha,
                    animationSpec = tween(SPLASH_EXIT_DURATION_MS.toInt(), easing = FastOutLinearInEasing),
                )
            }
        }

        // Точка «дышит» только на фазе ожидания; на выходе она полная, а весь вордмарк растворяется.
        val dotAlpha = if (!isExiting) {
            val transition = rememberInfiniteTransition(label = DotBreathTransitionLabel)
            val breath by transition.animateFloat(
                initialValue = FullAlpha,
                targetValue = MinBreathAlpha,
                animationSpec = infiniteRepeatable(
                    animation = tween(BreathHalfCycleMs, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = DotBreathAlphaLabel,
            )
            breath
        } else {
            FullAlpha
        }

        val exitScale = lerp(FullAlpha, ExitEndScale, exit.value)

        LyteWordmark(
            fontSize = fontSize,
            dotAlpha = dotAlpha,
            modifier = Modifier.graphicsLayer {
                alpha = FullAlpha - exit.value
                scaleX = exitScale
                scaleY = exitScale
            },
        )
    }
}

@Composable
private fun SplashErrorContent(
    onIntent: (SplashIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        LyteWordmark(fontSize = ErrorWordmarkFontSize, dotAlpha = FullAlpha)

        Spacer(modifier = Modifier.height(LyteTheme.spacing.s8))

        Text(
            text = stringResource(Res.string.splash_error_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(LyteTheme.spacing.s4))

        LyteButton(
            text = stringResource(Res.string.splash_retry),
            onClick = { onIntent(SplashIntent.Retry) },
        )
    }
}

@Composable
@Preview
private fun SplashContentLoadingPreview() {
    LyteTheme {
        SplashContent(
            state = SplashUiState.Loading,
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun SplashContentExitingPreview() {
    LyteTheme {
        SplashContent(
            state = SplashUiState.Exiting,
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun SplashContentErrorPreview() {
    LyteTheme {
        SplashContent(
            state = SplashUiState.Error,
            onIntent = {},
        )
    }
}
