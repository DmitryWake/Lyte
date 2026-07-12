package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.chip.LyteChip
import com.nikolaevskii.lyte.core.design.component.datadisplay.LyteSessionStopwatch
import com.nikolaevskii.lyte.core.design.component.datadisplay.LyteStopwatchSize
import com.nikolaevskii.lyte.core.design.component.feedback.LyteDialog
import com.nikolaevskii.lyte.core.design.component.iconbutton.LyteIconButton
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.session.LyteSetOverview
import com.nikolaevskii.lyte.core.design.component.session.LyteSetOverviewItem
import com.nikolaevskii.lyte.core.design.component.session.LyteSetOverviewTone
import com.nikolaevskii.lyte.core.design.component.stepper.LyteStepper
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_add_note
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_all_done_summary
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_all_done_title
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_complete_set
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_end_early_cancel
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_end_early_cd
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_end_early_confirm
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_end_early_description
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_end_early_title
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_exercise_position
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_exercises_cd
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_finish
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_mutation_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_overview_caption_current
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_overview_caption_number
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_overview_value_skipped
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_retry
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_set_position
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_set_value_bodyweight
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_set_value_weighted
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_skip_set
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_stepper_reps
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_stepper_weight
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_stepper_weight_unit
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_summary_set_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_target
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_to_landing
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionCurrentUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionOverlayUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSetPlaqueUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSetStatus
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSetValueUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState.ActiveSessionContent
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.ActiveSessionViewModel
import kotlin.math.roundToInt
import kotlin.time.Instant
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// Метрики — из дизайн-прототипа (спека 4.3, вариант «Фокус»).
private val HeaderPaddingTop = 6.dp
private val HeaderPaddingHorizontal = 16.dp
private val TitleBlockSpacing = 16.dp
private val TitlePaddingTop = 6.dp
private val TitlePaddingHorizontal = 32.dp
private val TitleFontSize = 26.sp
private val TitleLineHeight = 32.sp
private val TitleLetterSpacing = (-0.4).sp
private val OverviewSpacing = 16.dp
private val SetCardSpacing = 14.dp
private val SetCardPaddingHorizontal = 20.dp
private val SetCardPadding = 20.dp
private val SetCardHeaderIconSize = 16.dp
private val SetCardHeaderIconGap = 8.dp
private val SetCardHeaderTextSize = 14.sp
private val SetCardSteppersSpacing = 22.dp
private val SetCardStepperLabelGap = 6.dp
private val SetCardWeightSpacing = 18.dp
private val SetCardTargetSpacing = 20.dp
private val TargetPillPaddingHorizontal = 16.dp
private val TargetPillPaddingVertical = 7.dp
private val TargetPillTextSize = 13.sp
private val NoteRowSpacing = 12.dp
private val NotePillPaddingHorizontal = 16.dp
private val NotePillPaddingVertical = 8.dp
private val NotePillIconSize = 15.dp
private val NotePillTextSize = 14.sp
private val NotePillTextMaxWidth = 250.dp
private val ActionsPaddingHorizontal = 20.dp
private val ActionsPaddingBottom = 26.dp
private val ActionsGap = 4.dp
private val BannerSpacing = 8.dp
private val AllDoneStopwatchPaddingTop = 6.dp
private val AllDoneBadgeSize = 120.dp
private val AllDoneIconSize = 56.dp
private val AllDoneBadgeSpacing = 30.dp
private val AllDoneTitleLetterSpacing = (-0.3).sp
private val AllDoneSummarySpacing = 8.dp
private val AllDoneContentPaddingHorizontal = 32.dp
private val AllDoneActionPaddingBottom = 30.dp
private val ErrorContentPaddingHorizontal = 32.dp
private val ErrorContentGap = 12.dp

private const val RepsStepperStep = 1.0
private const val RepsStepperMin = 1.0
private const val WeightStepperStep = 2.5

// Общий `@Preview` в commonMain не принимает device/widthDp — даём превью телефонный размер сами.
private val PreviewDeviceWidth = 411.dp
private val PreviewDeviceHeight = 914.dp

@Composable
fun ActiveSessionScreen(
    sessionId: String,
    viewModel: ActiveSessionViewModel = koinViewModel { parametersOf(sessionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ActiveSessionContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun ActiveSessionContent(
    state: ActiveSessionUiState,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val content = state.content) {
                ActiveSessionContent.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                ActiveSessionContent.Error ->
                    ActiveSessionErrorContent(onIntent = onIntent, modifier = Modifier.align(Alignment.Center))

                is ActiveSessionContent.AllDone ->
                    ActiveSessionAllDoneContent(content = content, elapsedSeconds = state.elapsedSeconds, onIntent = onIntent)

                is ActiveSessionContent.Tracking -> {
                    ActiveSessionTrackingContent(content = content, elapsedSeconds = state.elapsedSeconds, onIntent = onIntent)
                    ActiveSessionOverlay(content = content, onIntent = onIntent)
                }
            }
        }
    }
}

/** Оверлеи существуют только поверх трекинга (шторки/диалог), поэтому диспатчатся из его ветки. */
@Composable
private fun ActiveSessionOverlay(
    content: ActiveSessionContent.Tracking,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    when (val overlay = content.overlay) {
        ActiveSessionOverlayUiModel.None -> Unit

        ActiveSessionOverlayUiModel.ExerciseSheet -> ExerciseSwitcherSheet(
            rows = content.switcherRows,
            onIntent = onIntent,
        )

        is ActiveSessionOverlayUiModel.NoteSheet -> SetNoteSheet(
            draft = overlay.draft,
            onIntent = onIntent,
        )

        ActiveSessionOverlayUiModel.EndEarlyDialog -> LyteDialog(
            title = stringResource(Res.string.active_session_end_early_title),
            description = stringResource(Res.string.active_session_end_early_description),
            confirmLabel = stringResource(Res.string.active_session_end_early_confirm),
            cancelLabel = stringResource(Res.string.active_session_end_early_cancel),
            onConfirm = { onIntent(ActiveSessionIntent.OnEndEarlyConfirmed) },
            onDismissRequest = { onIntent(ActiveSessionIntent.OnDismissOverlay) },
        )
    }
}

/** Локализованная подпись значения подхода: единицы подставляются здесь, формат веса — из модели. */
@Composable
internal fun activeSessionSetValueLabel(value: ActiveSessionSetValueUiModel): String = when (value) {
    is ActiveSessionSetValueUiModel.Weighted ->
        stringResource(Res.string.active_session_set_value_weighted, value.reps, value.weight)

    is ActiveSessionSetValueUiModel.Bodyweight ->
        stringResource(Res.string.active_session_set_value_bodyweight, value.reps)
}

@Composable
private fun ActiveSessionTrackingContent(
    content: ActiveSessionContent.Tracking,
    elapsedSeconds: Int,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    val current = content.current
    Column(modifier = Modifier.fillMaxSize()) {
        SessionHeader(elapsedSeconds = elapsedSeconds, onIntent = onIntent)

        Spacer(modifier = Modifier.height(TitleBlockSpacing))
        ExerciseTitle(current = current)

        Spacer(modifier = Modifier.height(OverviewSpacing))
        LyteSetOverview(
            sets = current.plaques.map { plaque -> plaque.toOverviewItem() },
            currentIndex = current.currentPlaqueIndex,
        )

        Spacer(modifier = Modifier.height(SetCardSpacing))
        CurrentSetCard(
            current = current,
            draftReps = content.draftReps,
            draftWeight = content.draftWeight,
            onIntent = onIntent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SetCardPaddingHorizontal),
        )

        Spacer(modifier = Modifier.height(NoteRowSpacing))
        NoteRow(
            note = current.note,
            onIntent = onIntent,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.weight(1f))

        if (content.hasMutationError) {
            MutationErrorBanner(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(BannerSpacing))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(ActionsGap),
            modifier = Modifier.padding(
                start = ActionsPaddingHorizontal,
                end = ActionsPaddingHorizontal,
                bottom = ActionsPaddingBottom,
            ),
        ) {
            LyteButton(
                text = stringResource(Res.string.active_session_complete_set),
                onClick = { onIntent(ActiveSessionIntent.OnCompleteSetClicked) },
                size = LyteButtonSize.Large,
                fullWidth = true,
            )
            LyteButton(
                text = stringResource(Res.string.active_session_skip_set),
                onClick = { onIntent(ActiveSessionIntent.OnSkipSetClicked) },
                variant = LyteButtonVariant.Text,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun SessionHeader(
    elapsedSeconds: Int,
    onIntent: (ActiveSessionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HeaderPaddingTop, start = HeaderPaddingHorizontal, end = HeaderPaddingHorizontal),
    ) {
        LyteIconButton(
            icon = LyteIcons.ListChecks,
            contentDescription = stringResource(Res.string.active_session_exercises_cd),
            onClick = { onIntent(ActiveSessionIntent.OnOpenExerciseSheetClicked) },
        )
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            LyteSessionStopwatch(seconds = elapsedSeconds, size = LyteStopwatchSize.Large)
        }
        LyteIconButton(
            icon = LyteIcons.Close,
            contentDescription = stringResource(Res.string.active_session_end_early_cd),
            onClick = { onIntent(ActiveSessionIntent.OnEndEarlyClicked) },
        )
    }
}

@Composable
private fun ExerciseTitle(
    current: ActiveSessionCurrentUiModel,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        LyteOverline(
            text = stringResource(
                Res.string.active_session_exercise_position,
                current.exerciseIndex,
                current.exerciseCount,
            ),
        )
        Text(
            text = current.exerciseName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = TitleFontSize,
                lineHeight = TitleLineHeight,
                fontWeight = FontWeight.Bold,
                letterSpacing = TitleLetterSpacing,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = TitlePaddingTop, start = TitlePaddingHorizontal, end = TitlePaddingHorizontal),
        )
    }
}

@Composable
private fun ActiveSessionSetPlaqueUiModel.toOverviewItem(): LyteSetOverviewItem = LyteSetOverviewItem(
    caption = if (status == ActiveSessionSetStatus.Current) {
        stringResource(Res.string.active_session_overview_caption_current)
    } else {
        stringResource(Res.string.active_session_overview_caption_number, index)
    },
    value = value?.let { setValue -> activeSessionSetValueLabel(setValue) }
        ?: stringResource(Res.string.active_session_overview_value_skipped),
    tone = when (status) {
        ActiveSessionSetStatus.Current -> LyteSetOverviewTone.Current
        ActiveSessionSetStatus.Hit -> LyteSetOverviewTone.Hit
        ActiveSessionSetStatus.Exceeded -> LyteSetOverviewTone.Exceed
        ActiveSessionSetStatus.Missed -> LyteSetOverviewTone.Miss
        ActiveSessionSetStatus.Skipped -> LyteSetOverviewTone.Skip
        ActiveSessionSetStatus.Todo -> LyteSetOverviewTone.Todo
    },
)

@Composable
private fun CurrentSetCard(
    current: ActiveSessionCurrentUiModel,
    draftReps: Int,
    draftWeight: Double,
    onIntent: (ActiveSessionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = LyteTheme.elevation.level1,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(SetCardPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SetCardHeaderIconGap),
            ) {
                Icon(
                    imageVector = LyteIcons.CircleDot,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(SetCardHeaderIconSize),
                )
                Text(
                    text = stringResource(Res.string.active_session_set_position, current.setIndex, current.setCount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = SetCardHeaderTextSize,
                        fontWeight = FontWeight.Bold,
                    ).withTabularNums(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(SetCardSteppersSpacing))
            LyteOverline(text = stringResource(Res.string.active_session_stepper_reps))
            Spacer(modifier = Modifier.height(SetCardStepperLabelGap))
            LyteStepper(
                value = draftReps.toDouble(),
                onValueChange = { value -> onIntent(ActiveSessionIntent.OnDraftRepsChanged(value.roundToInt())) },
                step = RepsStepperStep,
                min = RepsStepperMin,
                allowDecimal = false,
            )

            if (current.targetWeight != null) {
                Spacer(modifier = Modifier.height(SetCardWeightSpacing))
                LyteOverline(text = stringResource(Res.string.active_session_stepper_weight))
                Spacer(modifier = Modifier.height(SetCardStepperLabelGap))
                LyteStepper(
                    value = draftWeight,
                    onValueChange = { value -> onIntent(ActiveSessionIntent.OnDraftWeightChanged(value)) },
                    step = WeightStepperStep,
                    unit = stringResource(Res.string.active_session_stepper_weight_unit),
                )
            }

            Spacer(modifier = Modifier.height(SetCardTargetSpacing))
            TargetPill(label = stringResource(Res.string.active_session_target, activeSessionSetValueLabel(current.target)))
        }
    }
}

@Composable
private fun TargetPill(
    label: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = LyteTheme.extendedShapes.full,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = TargetPillTextSize,
                fontWeight = FontWeight.Medium,
            ).withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = TargetPillPaddingHorizontal, vertical = TargetPillPaddingVertical),
        )
    }
}

@Composable
private fun NoteRow(
    note: String,
    onIntent: (ActiveSessionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (note.isEmpty()) {
        LyteChip(
            text = stringResource(Res.string.active_session_add_note),
            selected = false,
            onClick = { onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked) },
            icon = LyteIcons.Edit,
            modifier = modifier,
        )
    } else {
        NotePill(
            note = note,
            onClick = { onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked) },
            modifier = modifier,
        )
    }
}

/** Пилюля с текстом сохранённой заметки: в отличие от чипа, текст обрезается многоточием. */
@Composable
private fun NotePill(
    note: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = LyteTheme.extendedShapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SetCardHeaderIconGap),
            modifier = Modifier.padding(horizontal = NotePillPaddingHorizontal, vertical = NotePillPaddingVertical),
        ) {
            Icon(
                imageVector = LyteIcons.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(NotePillIconSize),
            )
            Text(
                text = note,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = NotePillTextSize,
                    fontWeight = FontWeight.Medium,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = NotePillTextMaxWidth),
            )
        }
    }
}

@Composable
private fun MutationErrorBanner(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.active_session_mutation_error),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = ActionsPaddingHorizontal),
    )
}

@Composable
private fun ActiveSessionAllDoneContent(
    content: ActiveSessionContent.AllDone,
    elapsedSeconds: Int,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AllDoneStopwatchPaddingTop),
        ) {
            LyteSessionStopwatch(seconds = elapsedSeconds, size = LyteStopwatchSize.Large)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = AllDoneContentPaddingHorizontal),
        ) {
            Surface(
                shape = CircleShape,
                color = LyteTheme.extendedColors.successContainer,
                modifier = Modifier.size(AllDoneBadgeSize),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = LyteIcons.Check,
                        contentDescription = null,
                        tint = LyteTheme.extendedColors.onSuccessContainer,
                        modifier = Modifier.size(AllDoneIconSize),
                    )
                }
            }
            Spacer(modifier = Modifier.height(AllDoneBadgeSpacing))
            Text(
                text = stringResource(Res.string.active_session_all_done_title),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = AllDoneTitleLetterSpacing,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(AllDoneSummarySpacing))
            Text(
                text = allDoneSummary(content = content),
                style = MaterialTheme.typography.bodyMedium.withTabularNums(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        LyteButton(
            text = stringResource(Res.string.active_session_finish),
            onClick = { onIntent(ActiveSessionIntent.OnFinishClicked) },
            size = LyteButtonSize.Large,
            fullWidth = true,
            modifier = Modifier.padding(
                start = ActionsPaddingHorizontal,
                end = ActionsPaddingHorizontal,
                bottom = AllDoneActionPaddingBottom,
            ),
        )
    }
}

/** Сводка итога: «Push Day · выполнено 15 из 16 подходов» (выполненные — без пропущенных). */
@Composable
private fun allDoneSummary(content: ActiveSessionContent.AllDone): String = stringResource(
    Res.string.active_session_all_done_summary,
    content.programName,
    pluralStringResource(
        Res.plurals.active_session_summary_set_count,
        content.totalCount,
        content.completedCount,
        content.totalCount,
    ),
)

@Composable
private fun ActiveSessionErrorContent(
    onIntent: (ActiveSessionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ErrorContentGap),
        modifier = modifier.padding(horizontal = ErrorContentPaddingHorizontal),
    ) {
        Text(
            text = stringResource(Res.string.active_session_error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        LyteButton(
            text = stringResource(Res.string.active_session_retry),
            onClick = { onIntent(ActiveSessionIntent.OnRetryClicked) },
        )
        LyteButton(
            text = stringResource(Res.string.active_session_to_landing),
            onClick = { onIntent(ActiveSessionIntent.OnBackToLandingClicked) },
            variant = LyteButtonVariant.Text,
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            ActiveSessionContent(
                state = ActiveSessionUiState(
                    content = previewTracking(),
                    startedAt = Instant.fromEpochMilliseconds(0),
                    elapsedSeconds = 1224,
                ),
                onIntent = {},
            )
        }
    }
}

@Composable
@Preview
private fun ActiveSessionContentBodyweightPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            ActiveSessionContent(
                state = ActiveSessionUiState(
                    content = previewBodyweightTracking(),
                    startedAt = Instant.fromEpochMilliseconds(0),
                    elapsedSeconds = 754,
                ),
                onIntent = {},
            )
        }
    }
}

@Composable
@Preview
private fun ActiveSessionContentAllDonePreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            ActiveSessionContent(
                state = ActiveSessionUiState(
                    content = ActiveSessionContent.AllDone(
                        programName = "Push Day",
                        completedCount = 15,
                        totalCount = 16,
                    ),
                    startedAt = Instant.fromEpochMilliseconds(0),
                    elapsedSeconds = 3161,
                ),
                onIntent = {},
            )
        }
    }
}

@Composable
@Preview
private fun ActiveSessionContentLoadingPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            ActiveSessionContent(state = ActiveSessionUiState(), onIntent = {})
        }
    }
}

@Composable
@Preview
private fun ActiveSessionContentErrorPreview() {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            ActiveSessionContent(
                state = ActiveSessionUiState(content = ActiveSessionContent.Error),
                onIntent = {},
            )
        }
    }
}

private fun previewTracking(): ActiveSessionContent.Tracking = ActiveSessionContent.Tracking(
    current = ActiveSessionCurrentUiModel(
        exerciseId = "e2",
        exerciseIndex = 2,
        exerciseCount = 5,
        exerciseName = "Жим гантелей на наклонной",
        plaques = listOf(
            ActiveSessionSetPlaqueUiModel(
                index = 1,
                status = ActiveSessionSetStatus.Exceeded,
                value = ActiveSessionSetValueUiModel.Weighted(reps = 12, weight = "24"),
            ),
            ActiveSessionSetPlaqueUiModel(
                index = 2,
                status = ActiveSessionSetStatus.Current,
                value = ActiveSessionSetValueUiModel.Weighted(reps = 10, weight = "26"),
            ),
            ActiveSessionSetPlaqueUiModel(
                index = 3,
                status = ActiveSessionSetStatus.Todo,
                value = ActiveSessionSetValueUiModel.Weighted(reps = 10, weight = "26"),
            ),
        ),
        currentPlaqueIndex = 1,
        setIndex = 2,
        setCount = 3,
        currentSetId = "set2",
        targetReps = 10,
        targetWeight = 26.0,
        target = ActiveSessionSetValueUiModel.Weighted(reps = 10, weight = "26"),
        note = "",
    ),
    switcherRows = emptyList(),
    draftReps = 10,
    draftWeight = 26.0,
    overlay = ActiveSessionOverlayUiModel.None,
    hasMutationError = false,
)

private fun previewBodyweightTracking(): ActiveSessionContent.Tracking = ActiveSessionContent.Tracking(
    current = ActiveSessionCurrentUiModel(
        exerciseId = "e3",
        exerciseIndex = 3,
        exerciseCount = 5,
        exerciseName = "Отжимания на брусьях",
        plaques = listOf(
            ActiveSessionSetPlaqueUiModel(
                index = 1,
                status = ActiveSessionSetStatus.Skipped,
                value = null,
            ),
            ActiveSessionSetPlaqueUiModel(
                index = 2,
                status = ActiveSessionSetStatus.Current,
                value = ActiveSessionSetValueUiModel.Bodyweight(reps = 12),
            ),
            ActiveSessionSetPlaqueUiModel(
                index = 3,
                status = ActiveSessionSetStatus.Todo,
                value = ActiveSessionSetValueUiModel.Bodyweight(reps = 10),
            ),
        ),
        currentPlaqueIndex = 1,
        setIndex = 2,
        setCount = 3,
        currentSetId = "set2",
        targetReps = 12,
        targetWeight = null,
        target = ActiveSessionSetValueUiModel.Bodyweight(reps = 12),
        note = "Наклон вперёд, локти уже",
    ),
    switcherRows = emptyList(),
    draftReps = 12,
    draftWeight = 0.0,
    overlay = ActiveSessionOverlayUiModel.None,
    hasMutationError = false,
)
