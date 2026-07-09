package com.nikolaevskii.lyte.feature.splash.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.theme.lyteWordmarkFontFamily
import com.nikolaevskii.lyte.feature.splash.presentation.constant.SplashConstant.SPLASH_REVEAL_DURATION_MS
import com.nikolaevskii.lyte.feature.splash.generated.resources.Res
import com.nikolaevskii.lyte.feature.splash.generated.resources.splash_error_message
import com.nikolaevskii.lyte.feature.splash.generated.resources.splash_retry
import com.nikolaevskii.lyte.feature.splash.generated.resources.splash_wordmark
import com.nikolaevskii.lyte.feature.splash.generated.resources.splash_wordmark_accent
import com.nikolaevskii.lyte.feature.splash.presentation.model.SplashPhaseUiModel
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashIntent
import com.nikolaevskii.lyte.feature.splash.presentation.model.mvi.SplashUiState
import com.nikolaevskii.lyte.feature.splash.presentation.viewmodel.SplashViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val WordmarkFontSize = 40.sp
private val WordmarkLineHeight = 46.sp
private val WordmarkLetterSpacing = (-0.5).sp
private val BlinkingDotSize = 180.dp
private val RevealedDotSize = 14.dp
private const val BlinkAnimationDurationMs = 900
private const val HiddenAlpha = 0f
private const val FullyOpaqueAlpha = 1f
private const val MinBlinkAlpha = 0.35f
private const val SplashRevealTransitionLabel = "splashReveal"
private const val DotSizeAnimationLabel = "dotSize"
private const val TextAlphaAnimationLabel = "textAlpha"
private const val DotBlinkTransitionLabel = "dotBlink"
private const val BlinkAlphaAnimationLabel = "blinkAlpha"

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
                .padding(paddingValues)
                .padding(horizontal = LyteTheme.spacing.s8),
            contentAlignment = Alignment.Center,
        ) {
            if (state.isError) {
                SplashErrorContent(onIntent = onIntent)
            } else {
                SplashRevealContent(phase = state.phase)
            }
        }
    }
}

/** Точка пульсирует, пока идут стартовые процессы, затем уменьшается и рядом выезжает текст «Lyte». */
@Composable
private fun SplashRevealContent(phase: SplashPhaseUiModel) {
    val transition = updateTransition(targetState = phase, label = SplashRevealTransitionLabel)
    val revealAnimationSpec = tween<Dp>(SPLASH_REVEAL_DURATION_MS.toInt())
    val dotSize by transition.animateDp(
        transitionSpec = { revealAnimationSpec },
        label = DotSizeAnimationLabel,
    ) { animatedPhase -> if (animatedPhase == SplashPhaseUiModel.Blinking) BlinkingDotSize else RevealedDotSize }
    val textAlpha by transition.animateFloat(
        transitionSpec = { tween(SPLASH_REVEAL_DURATION_MS.toInt()) },
        label = TextAlphaAnimationLabel,
    ) { animatedPhase -> if (animatedPhase == SplashPhaseUiModel.Blinking) HiddenAlpha else FullyOpaqueAlpha }

    val dotAlpha = if (phase == SplashPhaseUiModel.Blinking) {
        val infiniteTransition = rememberInfiniteTransition(label = DotBlinkTransitionLabel)
        val blinkAlpha by infiniteTransition.animateFloat(
            initialValue = MinBlinkAlpha,
            targetValue = FullyOpaqueAlpha,
            animationSpec = infiniteRepeatable(
                animation = tween(BlinkAnimationDurationMs, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = BlinkAlphaAnimationLabel,
        )
        blinkAlpha
    } else {
        FullyOpaqueAlpha
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.animateContentSize(animationSpec = tween(SPLASH_REVEAL_DURATION_MS.toInt())),
    ) {
        if (phase == SplashPhaseUiModel.Revealing) {
            Text(
                text = stringResource(Res.string.splash_wordmark),
                modifier = Modifier.alpha(textAlpha),
                fontFamily = lyteWordmarkFontFamily(),
                fontSize = WordmarkFontSize,
                lineHeight = WordmarkLineHeight,
                letterSpacing = WordmarkLetterSpacing,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dotAlpha)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
        )
    }
}

@Composable
private fun SplashErrorContent(onIntent: (SplashIntent) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(Res.string.splash_wordmark),
                fontFamily = lyteWordmarkFontFamily(),
                fontSize = WordmarkFontSize,
                lineHeight = WordmarkLineHeight,
                letterSpacing = WordmarkLetterSpacing,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.splash_wordmark_accent),
                fontFamily = lyteWordmarkFontFamily(),
                fontSize = WordmarkFontSize,
                lineHeight = WordmarkLineHeight,
                letterSpacing = WordmarkLetterSpacing,
                color = MaterialTheme.colorScheme.primary,
            )
        }

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
private fun SplashContentBlinkingPreview() {
    LyteTheme {
        SplashContent(
            state = SplashUiState(phase = SplashPhaseUiModel.Blinking),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun SplashContentRevealingPreview() {
    LyteTheme {
        SplashContent(
            state = SplashUiState(phase = SplashPhaseUiModel.Revealing),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun SplashContentErrorPreview() {
    LyteTheme {
        SplashContent(
            state = SplashUiState(isError = true),
            onIntent = {},
        )
    }
}
