package com.nikolaevskii.lyte.feature.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.badge.LyteBadge
import com.nikolaevskii.lyte.core.design.component.badge.LyteBadgeSize
import com.nikolaevskii.lyte.core.design.component.badge.LyteBadgeTone
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDialog
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDiffRow
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.mark.LyteExerciseMark
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBar
import com.nikolaevskii.lyte.core.design.component.navigation.LyteTopBarSize
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrack
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrackMode
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.mvi.LyteError
import com.nikolaevskii.lyte.feature.history.generated.resources.Res
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_delete_a11y
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_delete_dialog_description
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_delete_dialog_title
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_delete_error
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_deleting_a11y
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_error
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_meta
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_not_found
import com.nikolaevskii.lyte.feature.history.generated.resources.history_details_title
import com.nikolaevskii.lyte.feature.history.generated.resources.history_duration
import com.nikolaevskii.lyte.feature.history.generated.resources.history_month_names_genitive
import com.nikolaevskii.lyte.feature.history.presentation.model.HistoryDiffRowUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.HistoryExerciseGroupUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.HistorySessionDetailsUiModel
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsIntent
import com.nikolaevskii.lyte.feature.history.presentation.model.mvi.HistorySessionDetailsUiState
import com.nikolaevskii.lyte.feature.history.presentation.viewmodel.HistorySessionDetailsViewModel
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val DetailsDeleteButtonSize = 44.dp

// Индикатор занимает место кнопки удаления, поэтому меряется от неё: внутренний отступ держит круг
// того же размера, что и иконка внутри LyteIconButton (половина кнопки).
private val DetailsDeleteProgressPadding = 11.dp
private val DetailsDeleteProgressStroke = 2.dp
private val DetailsMarkSize = 36.dp
private val DetailsGroupHeaderGap = 10.dp
private val DetailsSummaryGap = 10.dp
private const val MINUTE_PAD_LENGTH = 2

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
    val content = state as? HistorySessionDetailsUiState.Content
    val genitiveMonths = stringArrayResource(Res.array.history_month_names_genitive)
    Scaffold(
        topBar = {
            LyteTopBar(
                // Пока сессия грузится, названия программы ещё нет — шапка не должна быть пустой.
                title = content?.details?.programName ?: stringResource(Res.string.history_details_title),
                size = LyteTopBarSize.Large,
                onBack = { onIntent(HistorySessionDetailsIntent.OnBackClicked) },
                subtitle = content?.details?.let { details ->
                    sessionMetaLabel(details = details, monthLabel = genitiveMonths[details.monthNumber - 1])
                },
                // Удалять нечего, пока сессия не прочитана.
                trailing = content?.let { loaded ->
                    {
                        // Удаление уже идёт: на месте действия — индикатор, а не живая кнопка. Сам
                        // guard от повторного удаления живёт во ViewModel, это только его отражение.
                        if (loaded.isDeleting) {
                            // Своя семантика обязательна: индикатор занял место кнопки, и без неё
                            // действие для скринридера просто исчезает с экрана.
                            val deletingLabel = stringResource(Res.string.history_details_deleting_a11y)
                            CircularProgressIndicator(
                                strokeWidth = DetailsDeleteProgressStroke,
                                modifier = Modifier
                                    .size(DetailsDeleteButtonSize)
                                    .padding(DetailsDeleteProgressPadding)
                                    .semantics { contentDescription = deletingLabel },
                            )
                        } else {
                            LyteIconButton(
                                icon = LyteIcons.Delete,
                                contentDescription = stringResource(Res.string.history_details_delete_a11y),
                                onClick = { onIntent(HistorySessionDetailsIntent.OnDeleteClicked) },
                                size = DetailsDeleteButtonSize,
                            )
                        }
                    }
                },
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
                        text = stringResource(state.error.toMessageResource()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )

                is HistorySessionDetailsUiState.Content ->
                    HistorySessionDetailsList(content = state)
            }
        }

        if (content?.isDeleteDialogVisible == true) {
            LyteDialog(
                title = stringResource(
                    Res.string.history_details_delete_dialog_title,
                    content.details.programName,
                ),
                description = stringResource(Res.string.history_details_delete_dialog_description),
                onConfirm = { onIntent(HistorySessionDetailsIntent.OnDeleteConfirmed) },
                onDismissRequest = { onIntent(HistorySessionDetailsIntent.OnDeleteDismissed) },
            )
        }
    }
}

@Composable
private fun HistorySessionDetailsList(
    content: HistorySessionDetailsUiState.Content,
    modifier: Modifier = Modifier,
) {
    val details = content.details
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
        content.actionError?.let { error ->
            item(key = "action-error") {
                Text(
                    text = stringResource(Res.string.history_details_delete_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(bottom = LyteTheme.spacing.s2),
                )
            }
        }
        item(key = "summary") {
            HistorySessionSummaryRow(details = details)
        }
        details.exercises.forEach { group ->
            item(key = "title-${group.exerciseId}") {
                HistoryExerciseGroupHeader(group = group)
            }
            items(items = group.rows, key = { row -> row.id }) { row ->
                LyteDiffRow(
                    index = row.index,
                    tone = row.tone,
                    target = row.target,
                    actual = row.actual,
                    note = row.note,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Итог сессии одной строкой: длительность бейджем, исходы всех подходов — треком на всю остальную ширину. */
@Composable
private fun HistorySessionSummaryRow(
    details: HistorySessionDetailsUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DetailsSummaryGap),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = LyteTheme.spacing.s2),
    ) {
        LyteBadge(
            text = stringResource(Res.string.history_duration, details.durationMinutes),
            tone = LyteBadgeTone.Neutral,
            size = LyteBadgeSize.Medium,
        )
        LyteProgressTrack(
            mode = LyteProgressTrackMode.Tones(tones = details.setTones),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HistoryExerciseGroupHeader(
    group: HistoryExerciseGroupUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DetailsGroupHeaderGap),
        modifier = modifier.padding(top = LyteTheme.spacing.s3),
    ) {
        LyteExerciseMark(accent = group.accent, glyph = group.glyph, size = DetailsMarkSize)
        Text(
            text = group.exerciseName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Подзаголовок шапки: «6 августа 2026 · начало 19:02». */
@Composable
private fun sessionMetaLabel(details: HistorySessionDetailsUiModel, monthLabel: String): String {
    val startTime = "${details.startHour}:${details.startMinute.toString().padStart(MINUTE_PAD_LENGTH, '0')}"
    return stringResource(
        Res.string.history_details_meta,
        details.dayOfMonth,
        monthLabel,
        details.year,
        startTime,
    )
}

private fun LyteError.toMessageResource() = when (this) {
    LyteError.NotFound -> Res.string.history_details_not_found
    else -> Res.string.history_details_error
}

@Composable
@Preview
private fun HistorySessionDetailsContentPreview() {
    LyteTheme {
        HistorySessionDetailsContent(
            state = HistorySessionDetailsUiState.Content(details = previewDetails()),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun HistorySessionDetailsContentDeleteDialogPreview() {
    LyteTheme {
        HistorySessionDetailsContent(
            state = HistorySessionDetailsUiState.Content(details = previewDetails(), isDeleteDialogVisible = true),
            onIntent = {},
        )
    }
}

/** Удаление подтверждено и уже идёт: действие в шапке заменено индикатором, повторный тап невозможен. */
@Composable
@Preview
private fun HistorySessionDetailsContentDeletingPreview() {
    LyteTheme {
        HistorySessionDetailsContent(
            state = HistorySessionDetailsUiState.Content(details = previewDetails(), isDeleting = true),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun HistorySessionDetailsContentActionErrorPreview() {
    LyteTheme {
        HistorySessionDetailsContent(
            state = HistorySessionDetailsUiState.Content(details = previewDetails(), actionError = LyteError.Storage),
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
        HistorySessionDetailsContent(
            state = HistorySessionDetailsUiState.Error(LyteError.Storage),
            onIntent = {},
        )
    }
}

private fun previewDetails(): HistorySessionDetailsUiModel {
    val exercises = listOf(
        HistoryExerciseGroupUiModel(
            exerciseId = "e1",
            exerciseName = "Жим лёжа",
            accent = LyteAccent.Indigo,
            glyph = LyteExerciseGlyph.BenchPress,
            rows = listOf(
                diffRow("s1", 1, LyteProgressTone.Met, weighted(8, 80.0), weighted(8, 80.0)),
                diffRow("s2", 2, LyteProgressTone.Positive, weighted(8, 80.0), weighted(9, 82.5)),
                diffRow("s3", 3, LyteProgressTone.Negative, weighted(8, 80.0), weighted(6, 80.0), note = "тяжело"),
                diffRow("s4", 4, LyteProgressTone.Skipped, weighted(8, 80.0), actual = null),
            ),
        ),
        HistoryExerciseGroupUiModel(
            exerciseId = "e2",
            exerciseName = "Отжимания на брусьях",
            accent = LyteAccent.Coral,
            glyph = LyteExerciseGlyph.Rack,
            rows = listOf(
                diffRow("s5", 1, LyteProgressTone.Met, bodyweight(12), bodyweight(12)),
                diffRow("s6", 2, LyteProgressTone.Positive, bodyweight(12), bodyweight(15)),
            ),
        ),
    )
    return HistorySessionDetailsUiModel(
        programName = "Push Day",
        year = 2026,
        monthNumber = 7,
        dayOfMonth = 2,
        startHour = 18,
        startMinute = 24,
        durationMinutes = 52,
        setTones = exercises.flatMap { group -> group.rows.map { row -> row.tone } },
        exercises = exercises,
    )
}

private fun weighted(reps: Int, weight: Double): LyteSetValue = LyteSetValue(reps = reps, weight = weight)

private fun bodyweight(reps: Int): LyteSetValue = LyteSetValue(reps = reps)

private fun diffRow(
    id: String,
    index: Int,
    tone: LyteProgressTone,
    target: LyteSetValue,
    actual: LyteSetValue?,
    note: String? = null,
): HistoryDiffRowUiModel = HistoryDiffRowUiModel(id = id, index = index, tone = tone, target = target, actual = actual, note = note)
