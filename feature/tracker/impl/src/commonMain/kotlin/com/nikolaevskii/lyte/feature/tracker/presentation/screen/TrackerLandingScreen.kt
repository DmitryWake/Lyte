package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.brand.LyteWordmark
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.navigation.LyteBottomNavigationBarHeight
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.tracker_landing_hint
import com.nikolaevskii.lyte.feature.tracker.generated.resources.tracker_landing_start
import com.nikolaevskii.lyte.feature.tracker.generated.resources.tracker_landing_title
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ProgramPickerUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.TrackerLandingUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.TrackerLandingViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val WordmarkFontSize = 40.sp
private val WordmarkPaddingTop = 20.dp
private val WordmarkPaddingHorizontal = 24.dp

private val IconBadgeSize = 120.dp
private val IconSize = 52.dp
private val IconBadgeSpacing = 30.dp
private val TitleLetterSpacing = (-0.3).sp
private val TitleHintSpacing = 8.dp
private val HintMaxWidth = 280.dp
private val HintActionSpacing = 32.dp
private val ContentPaddingHorizontal = 32.dp

@Composable
fun TrackerLandingScreen(
    viewModel: TrackerLandingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TrackerLandingContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun TrackerLandingContent(
    state: TrackerLandingUiState,
    onIntent: (TrackerLandingIntent) -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Пока гейт проверяет активную сессию, экран пуст: найдётся сессия — уйдём на её маршрут
            // без вспышки «Нет активной сессии»; локальный запрос быстрый, спиннер бы только мигал.
            if (state is TrackerLandingUiState.NoActiveSession) {
                LyteWordmark(
                    fontSize = WordmarkFontSize,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = WordmarkPaddingTop, start = WordmarkPaddingHorizontal, end = WordmarkPaddingHorizontal),
                )

                NoActiveSessionContent(
                    onIntent = onIntent,
                    // Корень вкладки показывается вместе с плавающим доком, а тот не резервирует место
                    // через Scaffold.bottomBar (см. App() в :shared) — центрируем контент над ним.
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = LyteBottomNavigationBarHeight),
                )

                // Шторка выбора программы (кадры program-pick / program-pick-empty) — оверлей
                // лендинга, а не отдельный маршрут: в стеке вкладки её нет.
                state.picker?.let { picker ->
                    ProgramPickerSheet(picker = picker, onIntent = onIntent)
                }
            }
        }
    }
}

@Composable
private fun NoActiveSessionContent(
    onIntent: (TrackerLandingIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = ContentPaddingHorizontal),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(IconBadgeSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = LyteIcons.Dumbbell,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(IconSize),
                )
            }
        }
        Spacer(modifier = Modifier.height(IconBadgeSpacing))
        Text(
            text = stringResource(Res.string.tracker_landing_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = TitleLetterSpacing,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(TitleHintSpacing))
        Text(
            text = stringResource(Res.string.tracker_landing_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = HintMaxWidth),
        )
        Spacer(modifier = Modifier.height(HintActionSpacing))
        LyteButton(
            text = stringResource(Res.string.tracker_landing_start),
            onClick = { onIntent(TrackerLandingIntent.OnStartClicked) },
            icon = LyteIcons.Play,
        )
    }
}

@Composable
@Preview
private fun TrackerLandingContentPreview() {
    LyteTheme {
        TrackerLandingContent(
            state = TrackerLandingUiState.NoActiveSession(),
            onIntent = {},
        )
    }
}

/**
 * Гейт активной сессии: пока идёт проверка БД, экран намеренно пуст — ни вордмарка, ни спиннера.
 * Кадр фиксирует именно эту пустоту: если она когда-нибудь перестанет быть пустой, это будет видно.
 */
@Composable
@Preview
private fun TrackerLandingContentCheckingSessionPreview() {
    LyteTheme {
        TrackerLandingContent(
            state = TrackerLandingUiState.CheckingSession,
            onIntent = {},
        )
    }
}
