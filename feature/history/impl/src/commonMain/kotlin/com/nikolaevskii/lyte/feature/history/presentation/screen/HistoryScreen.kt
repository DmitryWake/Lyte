package com.nikolaevskii.lyte.feature.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.card.LyteSessionCard
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.navigation.LyteBottomNavigationBarHeight
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.feature.history.generated.resources.Res
import com.nikolaevskii.lyte.feature.history.generated.resources.history_date
import com.nikolaevskii.lyte.feature.history.generated.resources.history_empty_hint
import com.nikolaevskii.lyte.feature.history.generated.resources.history_empty_message
import com.nikolaevskii.lyte.feature.history.generated.resources.history_error
import com.nikolaevskii.lyte.feature.history.generated.resources.history_month_names
import com.nikolaevskii.lyte.feature.history.generated.resources.history_month_names_short
import com.nikolaevskii.lyte.feature.history.generated.resources.history_session_summary
import com.nikolaevskii.lyte.feature.history.generated.resources.history_title
import com.nikolaevskii.lyte.feature.history.presentation.model.HistoryMonthGroupUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.HistorySessionUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistoryUiState
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistoryViewModel
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Экран пересоздаётся при возврате на вкладку — перечитываем список, чтобы подхватить сессии,
    // завершённые в трекере, пока История была не видна (аналогично WorkoutListScreen).
    LaunchedEffect(Unit) {
        viewModel.onIntent(HistoryIntent.OnScreenShown)
    }

    HistoryContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun HistoryContent(
    state: HistoryUiState,
    onIntent: (HistoryIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            LyteTopBar(title = stringResource(Res.string.history_title), size = LyteTopBarSize.Large)
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                HistoryUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is HistoryUiState.Error ->
                    Text(
                        text = state.message ?: stringResource(Res.string.history_error),
                        modifier = Modifier.align(Alignment.Center),
                    )

                HistoryUiState.Empty ->
                    LyteEmptyState(
                        message = stringResource(Res.string.history_empty_message),
                        icon = LyteIcons.History,
                        hint = stringResource(Res.string.history_empty_hint),
                        modifier = Modifier.align(Alignment.Center),
                    )

                is HistoryUiState.Content -> HistorySessionList(groups = state.groups, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun HistorySessionList(
    groups: List<HistoryMonthGroupUiModel>,
    onIntent: (HistoryIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val monthNames = stringArrayResource(Res.array.history_month_names)
    val monthNamesShort = stringArrayResource(Res.array.history_month_names_short)
    LazyColumn(
        // Список — корень вкладки «История», показывается вместе с плавающим bottom-доком: тот не
        // резервирует место через Scaffold.bottomBar (см. App() в :shared), поэтому низ списка сам
        // добавляет LyteBottomNavigationBarHeight, иначе последняя карточка пряталась бы под доком.
        contentPadding = PaddingValues(
            start = LyteTheme.spacing.s5,
            end = LyteTheme.spacing.s5,
            top = LyteTheme.spacing.s1,
            bottom = LyteBottomNavigationBarHeight,
        ),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s3),
        modifier = modifier.fillMaxSize(),
    ) {
        groups.forEachIndexed { groupIndex, group ->
            item(key = "header-${group.year}-${group.monthNumber}") {
                LyteOverline(
                    text = monthNames[group.monthNumber - 1],
                    // Заголовкам месяцев, кроме первого, — чуть больше воздуха сверху (как в дизайне).
                    modifier = if (groupIndex == 0) Modifier else Modifier.padding(top = LyteTheme.spacing.s2),
                )
            }
            items(items = group.sessions, key = { session -> session.id }) { session ->
                LyteSessionCard(
                    title = session.programName,
                    trailingLabel = stringResource(
                        Res.string.history_date,
                        session.dayOfMonth,
                        monthNamesShort[session.monthNumber - 1],
                    ),
                    subtitle = stringResource(
                        Res.string.history_session_summary,
                        session.durationMinutes,
                        session.completedSetCount,
                        session.totalSetCount,
                    ),
                    onClick = { onIntent(HistoryIntent.OnSessionClicked(session.id)) },
                )
            }
        }
    }
}

@Composable
@Preview
private fun HistoryContentPreview() {
    LyteTheme {
        HistoryContent(
            state = HistoryUiState.Content(
                groups = listOf(
                    HistoryMonthGroupUiModel(
                        year = 2026,
                        monthNumber = 7,
                        sessions = listOf(
                            HistorySessionUiModel(
                                id = "1",
                                programName = "Push Day",
                                year = 2026,
                                monthNumber = 7,
                                dayOfMonth = 2,
                                durationMinutes = 52,
                                completedSetCount = 15,
                                totalSetCount = 16,
                            ),
                        ),
                    ),
                    HistoryMonthGroupUiModel(
                        year = 2026,
                        monthNumber = 6,
                        sessions = listOf(
                            HistorySessionUiModel(
                                id = "2",
                                programName = "Pull Day",
                                year = 2026,
                                monthNumber = 6,
                                dayOfMonth = 30,
                                durationMinutes = 58,
                                completedSetCount = 17,
                                totalSetCount = 17,
                            ),
                            HistorySessionUiModel(
                                id = "3",
                                programName = "Leg Day",
                                year = 2026,
                                monthNumber = 6,
                                dayOfMonth = 28,
                                durationMinutes = 61,
                                completedSetCount = 13,
                                totalSetCount = 14,
                            ),
                        ),
                    ),
                ),
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun HistoryContentEmptyPreview() {
    LyteTheme {
        HistoryContent(state = HistoryUiState.Empty, onIntent = {})
    }
}

@Composable
@Preview
private fun HistoryContentLoadingPreview() {
    LyteTheme {
        HistoryContent(state = HistoryUiState.Loading, onIntent = {})
    }
}

@Composable
@Preview
private fun HistoryContentErrorPreview() {
    LyteTheme {
        HistoryContent(state = HistoryUiState.Error(message = null), onIntent = {})
    }
}
