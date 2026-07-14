package com.nikolaevskii.lyte.feature.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.badge.LyteBadge
import com.nikolaevskii.lyte.core.design.component.badge.LyteBadgeSize
import com.nikolaevskii.lyte.core.design.component.badge.LyteBadgeTone
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDiffRow
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDiffTone
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import com.nikolaevskii.lyte.feature.history.generated.resources.Res
import com.nikolaevskii.lyte.feature.history.generated.resources.history_bodyweight
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_error
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_meta
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_sets
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_title
import com.nikolaevskii.lyte.feature.history.generated.resources.history_duration
import com.nikolaevskii.lyte.feature.history.generated.resources.history_month_names_genitive
import com.nikolaevskii.lyte.feature.history.presentation.model.HistoryDiffRowUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.HistoryExerciseGroupUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.HistorySessionDetailsUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.HistorySetValueUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsUiState
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistorySessionDetailsViewModel
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HistorySessionDetailsScreen(
    sessionId: String,
    viewModel: HistorySessionDetailsViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HistorySessionDetailsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun HistorySessionDetailsContent(
    state: HistorySessionDetailsUiState,
    onIntent: (HistorySessionDetailsIntent) -> Unit,
) {
    val fallbackTitle = stringResource(Res.string.history_details_title)
    val title = (state as? HistorySessionDetailsUiState.Content)?.details?.programName ?: fallbackTitle
    Scaffold(
        topBar = {
            LyteTopBar(
                title = title,
                size = LyteTopBarSize.Large,
                onBack = { onIntent(HistorySessionDetailsIntent.OnBackClicked) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (state) {
                HistorySessionDetailsUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is HistorySessionDetailsUiState.Error ->
                    Text(
                        text = state.message ?: stringResource(Res.string.history_details_error),
                        modifier = Modifier.align(Alignment.Center),
                    )

                is HistorySessionDetailsUiState.Content ->
                    HistorySessionDetailsList(details = state.details)
            }
        }
    }
}

@Composable
private fun HistorySessionDetailsList(
    details: HistorySessionDetailsUiModel,
    modifier: Modifier = Modifier,
) {
    val genitiveMonths = stringArrayResource(Res.array.history_month_names_genitive)
    LazyColumn(
        contentPadding = PaddingValues(
            start = LyteTheme.spacing.s5,
            end = LyteTheme.spacing.s5,
            top = LyteTheme.spacing.s1,
            bottom = LyteTheme.spacing.s6,
        ),
        verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s2),
        modifier = modifier.fillMaxSize(),
    ) {
        item(key = "meta") {
            HistorySessionMetaRow(details = details, monthLabel = genitiveMonths[details.monthNumber - 1])
        }
        details.exercises.forEach { group ->
            item(key = "title-${group.exerciseId}") {
                Text(
                    text = group.exerciseName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = LyteTheme.spacing.s3),
                )
            }
            items(items = group.rows, key = { row -> row.id }) { row ->
                LyteDiffRow(
                    index = row.index,
                    tone = row.tone,
                    target = row.target.notation(),
                    actual = row.actual?.notation(),
                    note = row.note,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun HistorySessionMetaRow(
    details: HistorySessionDetailsUiModel,
    monthLabel: String,
    modifier: Modifier = Modifier,
) {
    val startTime = "${details.startHour}:${details.startMinute.toString().padStart(2, '0')}"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s2),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = LyteTheme.spacing.s2),
    ) {
        Text(
            text = stringResource(Res.string.history_details_meta, details.dayOfMonth, monthLabel, startTime),
            style = MaterialTheme.typography.bodySmall.withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        LyteBadge(
            text = stringResource(Res.string.history_duration, details.durationMinutes),
            tone = LyteBadgeTone.Neutral,
            size = LyteBadgeSize.Medium,
        )
        LyteBadge(
            text = stringResource(Res.string.history_details_sets, details.completedSetCount, details.totalSetCount),
            tone = LyteBadgeTone.Neutral,
            size = LyteBadgeSize.Medium,
        )
    }
}

@Composable
private fun HistorySetValueUiModel.notation(): String = when (this) {
    // «8×80» — LyteDiffRow сам добавит единицы «повт»/«кг».
    is HistorySetValueUiModel.Weighted -> "$reps×$weight"
    // Свой вес: единицу добавляем сами, LyteDiffRow отрендерит строку как есть.
    is HistorySetValueUiModel.Bodyweight -> stringResource(Res.string.history_bodyweight, reps)
}

@Composable
@Preview
private fun HistorySessionDetailsContentPreview() {
    LyteTheme {
        HistorySessionDetailsContent(
            state = HistorySessionDetailsUiState.Content(
                details = HistorySessionDetailsUiModel(
                    programName = "Push Day",
                    year = 2026,
                    monthNumber = 7,
                    dayOfMonth = 2,
                    startHour = 18,
                    startMinute = 24,
                    durationMinutes = 52,
                    completedSetCount = 15,
                    totalSetCount = 16,
                    exercises = listOf(
                        HistoryExerciseGroupUiModel(
                            exerciseId = "e1",
                            exerciseName = "Жим лёжа",
                            rows = listOf(
                                diffRow("s1", 1, LyteDiffTone.Met, weighted(8, "80"), weighted(8, "80")),
                                diffRow("s2", 2, LyteDiffTone.Positive, weighted(8, "80"), weighted(9, "80")),
                                diffRow("s3", 3, LyteDiffTone.Negative, weighted(8, "80"), weighted(6, "80"), note = "тяжело"),
                                diffRow("s4", 4, LyteDiffTone.Skipped, weighted(8, "80"), actual = null),
                            ),
                        ),
                        HistoryExerciseGroupUiModel(
                            exerciseId = "e2",
                            exerciseName = "Отжимания на брусьях",
                            rows = listOf(
                                diffRow("s5", 1, LyteDiffTone.Met, bodyweight(12), bodyweight(12)),
                                diffRow("s6", 2, LyteDiffTone.Met, bodyweight(12), bodyweight(12)),
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
private fun HistorySessionDetailsContentLoadingPreview() {
    LyteTheme {
        HistorySessionDetailsContent(state = HistorySessionDetailsUiState.Loading, onIntent = {})
    }
}

@Composable
@Preview
private fun HistorySessionDetailsContentErrorPreview() {
    LyteTheme {
        HistorySessionDetailsContent(state = HistorySessionDetailsUiState.Error(message = null), onIntent = {})
    }
}

private fun weighted(reps: Int, weight: String): HistorySetValueUiModel = HistorySetValueUiModel.Weighted(reps, weight)

private fun bodyweight(reps: Int): HistorySetValueUiModel = HistorySetValueUiModel.Bodyweight(reps)

private fun diffRow(
    id: String,
    index: Int,
    tone: LyteDiffTone,
    target: HistorySetValueUiModel,
    actual: HistorySetValueUiModel?,
    note: String? = null,
): HistoryDiffRowUiModel = HistoryDiffRowUiModel(id = id, index = index, tone = tone, target = target, actual = actual, note = note)
