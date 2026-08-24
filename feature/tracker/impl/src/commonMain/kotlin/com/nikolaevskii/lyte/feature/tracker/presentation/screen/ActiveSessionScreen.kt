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
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrack
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrackMode
import com.nikolaevskii.lyte.core.design.component.session.LyteExerciseSetList
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_add_note
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
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_last_set
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_last_set_in_exercise
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_mutation_error
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_retry
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_save_workout
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_skip_set
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_summary_set_count
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_to_landing
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionCurrentUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionLastSetLabel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionOverlayUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSetStatus
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSetUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.lastSetLabel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.toTrackSetStates
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionIntent
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionUiState.ActiveSessionContent
import com.nikolaevskii.lyte.feature.tracker.presentation.viewmodel.ActiveSessionViewModel
import kotlin.time.Instant
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// Метрики — из дизайн-прототипа (спека 4.3, `LyteScreen.dc.html:91–183`).
private val HeaderPaddingTop = 6.dp
private val HeaderPaddingHorizontal = 16.dp
private val TitleBlockPaddingTop = 6.dp
private val TitleBlockPaddingBottom = 8.dp
private val TitlePaddingHorizontal = 32.dp
private val TitlePaddingTop = 6.dp
private val TitleFontSize = 24.sp
private val TitleLineHeight = 29.sp
private val TitleLetterSpacing = (-0.4).sp
private const val TitleMaxLines = 3
private val SetListPaddingHorizontal = 20.dp
private val NoteBlockPaddingHorizontal = 12.dp
private val NoteBlockPaddingVertical = 10.dp
private val NoteBlockGap = 8.dp
private val NoteBlockIconSize = 15.dp
private val NoteBlockIconPaddingTop = 1.dp
private val NoteBlockTextSize = 12.5.sp
private val NoteBlockLineHeight = 17.sp
private val FooterPaddingTop = 10.dp
private val FooterPaddingBottom = 2.dp
private val ActionsPaddingTop = 10.dp
private val ActionsPaddingHorizontal = 20.dp
private val ActionsPaddingBottom = 26.dp
private val ActionsGap = 2.dp
private val BannerSpacing = 8.dp
private val AllDoneStopwatchPaddingTop = 10.dp
private val AllDoneBadgeSize = 120.dp
private val AllDoneIconSize = 56.dp
private val AllDoneBadgeSpacing = 30.dp
private val AllDoneTitleLetterSpacing = (-0.3).sp
private val AllDoneProgramSpacing = 8.dp
private val AllDoneTrackSpacing = 20.dp
// Потолок ширины из макета: во всю ширину экрана короткая сессия читалась бы парой широких плашек.
private val AllDoneTrackMaxWidth = 220.dp
private val AllDoneSummarySpacing = 10.dp
private val AllDoneContentPaddingHorizontal = 40.dp
private val AllDoneActionPaddingBottom = 30.dp
private val ErrorContentPaddingHorizontal = 32.dp
private val ErrorContentGap = 12.dp

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

@Composable
private fun ActiveSessionTrackingContent(
    content: ActiveSessionContent.Tracking,
    elapsedSeconds: Int,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    val current = content.current
    val lastSetText = content.lastSetLabel?.let { label -> lastSetLabelText(label) }
    val footer: (@Composable () -> Unit)? = lastSetText?.let { text ->
        {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FooterPaddingTop, bottom = FooterPaddingBottom),
            ) {
                LyteOverline(text = text)
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        SessionHeader(elapsedSeconds = elapsedSeconds, onIntent = onIntent)
        ExerciseTitle(current = current)

        // Список получает ограниченную по высоте область: от её нижнего края считается якорь
        // фокус-карточки. Без `weight` он не сработает — список станет обычной колонкой.
        LyteExerciseSetList(
            sets = content.trackSets,
            onRepsChange = { reps -> onIntent(ActiveSessionIntent.OnDraftRepsChanged(reps)) },
            onWeightChange = { weight -> onIntent(ActiveSessionIntent.OnDraftWeightChanged(weight)) },
            currentContent = { CurrentSetNote(note = current.note, onIntent = onIntent) },
            footer = footer,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = SetListPaddingHorizontal),
        )

        if (content.hasMutationError) {
            MutationErrorBanner(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(BannerSpacing))
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(ActionsGap),
            modifier = Modifier.padding(
                top = ActionsPaddingTop,
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

/** Единственное, что от подписи хвоста остаётся экрану: выбор уже сделан маппером. */
@Composable
private fun lastSetLabelText(label: ActiveSessionLastSetLabel): String = when (label) {
    ActiveSessionLastSetLabel.LastInSession -> stringResource(Res.string.active_session_last_set)
    ActiveSessionLastSetLabel.LastInExercise -> stringResource(Res.string.active_session_last_set_in_exercise)
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
            icon = LyteIcons.List,
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
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TitleBlockPaddingTop, bottom = TitleBlockPaddingBottom),
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
            maxLines = TitleMaxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = TitlePaddingTop, start = TitlePaddingHorizontal, end = TitlePaddingHorizontal),
        )
    }
}

/**
 * Заметка внизу фокус-карточки: написанная — блоком с полным текстом (заметку между подходами не
 * обрезают), пустая — чипом-приглашением. Оба варианта ведут в ту же шторку.
 */
@Composable
private fun CurrentSetNote(
    note: String,
    onIntent: (ActiveSessionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (note.isEmpty()) {
        Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
            LyteChip(
                text = stringResource(Res.string.active_session_add_note),
                selected = false,
                onClick = { onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked) },
                icon = LyteIcons.Edit,
            )
        }
    } else {
        Surface(
            onClick = { onIntent(ActiveSessionIntent.OnOpenNoteSheetClicked) },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(NoteBlockGap),
                modifier = Modifier.padding(
                    horizontal = NoteBlockPaddingHorizontal,
                    vertical = NoteBlockPaddingVertical,
                ),
            ) {
                Icon(
                    imageVector = LyteIcons.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = NoteBlockIconPaddingTop)
                        .size(NoteBlockIconSize),
                )
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = NoteBlockTextSize,
                        lineHeight = NoteBlockLineHeight,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
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
            Spacer(modifier = Modifier.height(AllDoneProgramSpacing))
            Text(
                text = content.programName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(AllDoneTrackSpacing))
            LyteProgressTrack(
                mode = LyteProgressTrackMode.Tones(tones = content.setTones),
                // Сначала потолок, потом заполнение: иначе `fillMaxWidth` придёт с минимумом во всю
                // ширину и ограничение перестанет работать.
                modifier = Modifier
                    .widthIn(max = AllDoneTrackMaxWidth)
                    .fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(AllDoneSummarySpacing))
            Text(
                text = allDoneSummary(content = content),
                style = MaterialTheme.typography.bodySmall.withTabularNums(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        LyteButton(
            text = stringResource(Res.string.active_session_save_workout),
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

/** Сводка итога: «15 из 16 подходов выполнено» — выполненные считаются без пропущенных. */
@Composable
private fun allDoneSummary(content: ActiveSessionContent.AllDone): String = pluralStringResource(
    Res.plurals.active_session_summary_set_count,
    content.totalCount,
    content.completedCount,
    content.totalCount,
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
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = trackingState(content = previewTracking(currentIndex = 1, setCount = 3), elapsedSeconds = 1224),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentFirstSetPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = trackingState(content = previewTracking(currentIndex = 0, setCount = 8), elapsedSeconds = 92),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentMiddleSetPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = trackingState(content = previewTracking(currentIndex = 4, setCount = 8), elapsedSeconds = 1544),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentLastSetPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = trackingState(content = previewTracking(currentIndex = 7, setCount = 8), elapsedSeconds = 2960),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentNotePreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = trackingState(
                content = previewTracking(
                    currentIndex = 2,
                    setCount = 4,
                    note = "Пояс затянул туго — на следующем подходе ослабить и добавить 2.5 кг",
                ),
                elapsedSeconds = 1120,
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentBodyweightPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = trackingState(content = previewBodyweightTracking(), elapsedSeconds = 754),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentAllDonePreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = ActiveSessionUiState(
                content = ActiveSessionContent.AllDone(
                    programName = "Push Day",
                    completedCount = 15,
                    totalCount = 16,
                    setTones = previewAllDoneTones(),
                ),
                startedAt = Instant.fromEpochMilliseconds(0),
                elapsedSeconds = 3161,
            ),
            onIntent = {},
        )
    }
}

/** Короткая сессия: у трека мало сегментов, и потолок ширины виден на глаз. */
@Composable
@Preview
private fun ActiveSessionContentAllDoneShortPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = ActiveSessionUiState(
                content = ActiveSessionContent.AllDone(
                    programName = "Утренняя разминка",
                    completedCount = 3,
                    totalCount = 4,
                    setTones = listOf(
                        LyteProgressTone.Met,
                        LyteProgressTone.Positive,
                        LyteProgressTone.Negative,
                        LyteProgressTone.Skipped,
                    ),
                ),
                startedAt = Instant.fromEpochMilliseconds(0),
                elapsedSeconds = 738,
            ),
            onIntent = {},
        )
    }
}

@Composable
@Preview
private fun ActiveSessionContentLoadingPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(state = ActiveSessionUiState(), onIntent = {})
    }
}

@Composable
@Preview
private fun ActiveSessionContentErrorPreview() {
    ActiveSessionPreviewDevice {
        ActiveSessionContent(
            state = ActiveSessionUiState(content = ActiveSessionContent.Error),
            onIntent = {},
        )
    }
}

@Composable
private fun ActiveSessionPreviewDevice(content: @Composable () -> Unit) {
    LyteTheme {
        Box(modifier = Modifier.size(width = PreviewDeviceWidth, height = PreviewDeviceHeight)) {
            content()
        }
    }
}

private fun trackingState(content: ActiveSessionContent, elapsedSeconds: Int): ActiveSessionUiState =
    ActiveSessionUiState(
        content = content,
        startedAt = Instant.fromEpochMilliseconds(0),
        elapsedSeconds = elapsedSeconds,
    )

private fun previewTracking(
    currentIndex: Int,
    setCount: Int,
    note: String = "",
): ActiveSessionContent.Tracking {
    val target = LyteSetValue(reps = 10, weight = 62.5)
    val current = ActiveSessionCurrentUiModel(
        exerciseId = "e2",
        exerciseIndex = 2,
        exerciseCount = 5,
        exerciseName = "Жим гантелей на наклонной",
        sets = List(setCount) { index ->
            ActiveSessionSetUiModel(
                index = index + 1,
                status = previewStatus(index = index, currentIndex = currentIndex),
                value = previewValue(index = index, currentIndex = currentIndex, target = target),
                note = if (index == 1 && index < currentIndex) "Тяжело, форма поплыла" else "",
            )
        },
        currentSetIndex = currentIndex,
        setCount = setCount,
        currentSetId = "set${currentIndex + 1}",
        targetReps = 10,
        targetWeight = 62.5,
        target = target,
        note = note,
    )
    return current.toTrackingContent(draftReps = 10, draftWeight = 62.5)
}

/** Сессия из четырёх упражнений по четыре подхода: один пропущен, остальные с разными исходами. */
private fun previewAllDoneTones(): List<LyteProgressTone> = listOf(
    LyteProgressTone.Met, LyteProgressTone.Met, LyteProgressTone.Positive, LyteProgressTone.Met,
    LyteProgressTone.Met, LyteProgressTone.Positive, LyteProgressTone.Negative, LyteProgressTone.Met,
    LyteProgressTone.Met, LyteProgressTone.Met, LyteProgressTone.Met, LyteProgressTone.Negative,
    LyteProgressTone.Positive, LyteProgressTone.Met, LyteProgressTone.Met, LyteProgressTone.Skipped,
)

private fun previewStatus(index: Int, currentIndex: Int): ActiveSessionSetStatus = when {
    index == currentIndex -> ActiveSessionSetStatus.Current
    index > currentIndex -> ActiveSessionSetStatus.Todo
    index % 3 == 0 -> ActiveSessionSetStatus.Exceeded
    index % 3 == 1 -> ActiveSessionSetStatus.Hit
    else -> ActiveSessionSetStatus.Skipped
}

private fun previewValue(index: Int, currentIndex: Int, target: LyteSetValue): LyteSetValue? = when {
    index >= currentIndex -> target
    index % 3 == 0 -> LyteSetValue(reps = 12, weight = 62.5)
    index % 3 == 1 -> LyteSetValue(reps = 10, weight = 62.5)
    else -> null
}

/** Цель — свой вес, но степпер веса на месте и стоит на нуле: им и добавляют пояс. */
private fun previewBodyweightTracking(): ActiveSessionContent.Tracking = ActiveSessionCurrentUiModel(
    exerciseId = "e3",
    exerciseIndex = 3,
    exerciseCount = 5,
    exerciseName = "Отжимания на брусьях",
    sets = listOf(
        ActiveSessionSetUiModel(index = 1, status = ActiveSessionSetStatus.Skipped, value = null, note = ""),
        ActiveSessionSetUiModel(
            index = 2,
            status = ActiveSessionSetStatus.Current,
            value = LyteSetValue(reps = 12),
            note = "",
        ),
        ActiveSessionSetUiModel(
            index = 3,
            status = ActiveSessionSetStatus.Todo,
            value = LyteSetValue(reps = 10),
            note = "",
        ),
    ),
    currentSetIndex = 1,
    setCount = 3,
    currentSetId = "set2",
    targetReps = 12,
    targetWeight = null,
    target = LyteSetValue(reps = 12),
    note = "Наклон вперёд, локти уже",
).toTrackingContent(draftReps = 12, draftWeight = 0.0)

/** Собирает превьюшный `Tracking` тем же маппером, что и ViewModel, — иначе превью врало бы. */
private fun ActiveSessionCurrentUiModel.toTrackingContent(
    draftReps: Int,
    draftWeight: Double,
): ActiveSessionContent.Tracking = ActiveSessionContent.Tracking(
    current = this,
    trackSets = toTrackSetStates(draftReps = draftReps, draftWeight = draftWeight),
    lastSetLabel = lastSetLabel(),
    switcherRows = emptyList(),
    draftReps = draftReps,
    draftWeight = draftWeight,
    overlay = ActiveSessionOverlayUiModel.None,
    hasMutationError = false,
)
