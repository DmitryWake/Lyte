package com.nikolaevskii.lyte.feature.tracker.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheetHeight
import com.nikolaevskii.lyte.core.design.component.textfield.LyteTextField
import com.nikolaevskii.lyte.core.design.format.lyteSetValueLabel
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.core.design.theme.withTabularNums
import com.nikolaevskii.lyte.feature.tracker.generated.resources.Res
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_exercises_sheet_title
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_note_placeholder
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_note_save
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_note_sheet_title
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_switcher_current_subtitle
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_switcher_done
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_switcher_done_subtitle
import com.nikolaevskii.lyte.feature.tracker.generated.resources.active_session_switcher_now
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSwitcherRowUiModel
import com.nikolaevskii.lyte.feature.tracker.presentation.model.ActiveSessionSwitcherStatus
import com.nikolaevskii.lyte.feature.tracker.presentation.model.mvi.ActiveSessionIntent
import org.jetbrains.compose.resources.stringResource

private val SwitcherRowPaddingHorizontal = 16.dp
private val SwitcherRowPaddingVertical = 13.dp
private val SwitcherRowGap = 12.dp
private val SwitcherNameTextSize = 15.5.sp
private val SwitcherSubtitleTextSize = 13.sp
private val SwitcherSubtitleSpacing = 3.dp
private val SwitcherPillsSpacing = 7.dp
private val SwitcherPillsGap = 5.dp
private val SwitcherPillPaddingHorizontal = 9.dp
private val SwitcherPillPaddingVertical = 3.dp
private val SwitcherPillTextSize = 12.sp
private val SwitcherTrailingTextSize = 12.sp
private val SwitcherTrailingIconSize = 14.dp
private val SwitcherTrailingIconGap = 5.dp
private val SwitcherNowLetterSpacing = 0.4.sp
private val NoteSaveSpacing = 16.dp
private val NoteSaveBottomSpacing = 8.dp
private const val SwitcherDoneAlpha = 0.55f
private const val SwitcherCurrentSubtitleAlpha = 0.8f

/**
 * Шторка «Упражнения сессии»: список всех упражнений со статусами, тап по незакрытому переключает
 * текущее. Высота — по контенту (список короткий: упражнения одной программы), при переполнении
 * прокручивается.
 */
@Composable
internal fun ExerciseSwitcherSheet(
    rows: List<ActiveSessionSwitcherRowUiModel>,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    LyteBottomSheet(
        title = stringResource(Res.string.active_session_exercises_sheet_title),
        onDismissRequest = { onIntent(ActiveSessionIntent.OnDismissOverlay) },
        height = LyteBottomSheetHeight.WrapContent,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s2),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = LyteTheme.spacing.s5, end = LyteTheme.spacing.s5, bottom = LyteTheme.spacing.s3),
        ) {
            rows.forEach { row ->
                SwitcherRow(row = row, onIntent = onIntent)
            }
        }
    }
}

/** Шторка заметки к текущему подходу: свободный текст, сохранение — по «Готово». */
@Composable
internal fun SetNoteSheet(
    draft: String,
    onIntent: (ActiveSessionIntent) -> Unit,
) {
    LyteBottomSheet(
        title = stringResource(Res.string.active_session_note_sheet_title),
        onDismissRequest = { onIntent(ActiveSessionIntent.OnDismissOverlay) },
        height = LyteBottomSheetHeight.WrapContent,
    ) {
        LyteTextField(
            value = draft,
            onValueChange = { text -> onIntent(ActiveSessionIntent.OnNoteDraftChanged(text)) },
            placeholder = stringResource(Res.string.active_session_note_placeholder),
            multiline = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LyteTheme.spacing.s5),
        )
        LyteButton(
            text = stringResource(Res.string.active_session_note_save),
            onClick = { onIntent(ActiveSessionIntent.OnSaveNoteClicked) },
            fullWidth = true,
            modifier = Modifier.padding(
                start = LyteTheme.spacing.s5,
                end = LyteTheme.spacing.s5,
                top = NoteSaveSpacing,
                bottom = NoteSaveBottomSpacing,
            ),
        )
    }
}

@Composable
private fun SwitcherRow(
    row: ActiveSessionSwitcherRowUiModel,
    onIntent: (ActiveSessionIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCurrent = row.status == ActiveSessionSwitcherStatus.Current
    val isDone = row.status == ActiveSessionSwitcherStatus.Done
    val contentColor = if (isCurrent) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = { onIntent(ActiveSessionIntent.OnExerciseSelected(row.exerciseId)) },
        enabled = row.isSelectable,
        shape = MaterialTheme.shapes.large,
        color = if (isCurrent) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDone) SwitcherDoneAlpha else 1f),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SwitcherRowGap),
            modifier = Modifier.padding(horizontal = SwitcherRowPaddingHorizontal, vertical = SwitcherRowPaddingVertical),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = SwitcherNameTextSize,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = contentColor,
                )
                SwitcherRowDetails(row = row)
            }
            SwitcherRowTrailing(row = row)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SwitcherRowDetails(row: ActiveSessionSwitcherRowUiModel) {
    when (row.status) {
        ActiveSessionSwitcherStatus.Current -> row.currentSetIndex?.let { setIndex ->
            SwitcherSubtitle(
                text = stringResource(Res.string.active_session_switcher_current_subtitle, setIndex, row.setCount),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = SwitcherCurrentSubtitleAlpha),
            )
        }

        ActiveSessionSwitcherStatus.Done -> SwitcherSubtitle(
            text = stringResource(Res.string.active_session_switcher_done_subtitle, row.doneCount, row.setCount),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ActiveSessionSwitcherStatus.Pending -> FlowRow(
            horizontalArrangement = Arrangement.spacedBy(SwitcherPillsGap),
            verticalArrangement = Arrangement.spacedBy(SwitcherPillsGap),
            modifier = Modifier.padding(top = SwitcherPillsSpacing),
        ) {
            row.targetPills.forEach { pill ->
                SwitcherTargetPill(value = pill)
            }
        }
    }
}

@Composable
private fun SwitcherSubtitle(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = SwitcherSubtitleTextSize).withTabularNums(),
        color = color,
        modifier = Modifier.padding(top = SwitcherSubtitleSpacing),
    )
}

@Composable
private fun SwitcherTargetPill(value: LyteSetValue) {
    Surface(
        shape = LyteTheme.extendedShapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Text(
            text = lyteSetValueLabel(value),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = SwitcherPillTextSize,
                fontWeight = FontWeight.SemiBold,
            ).withTabularNums(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = SwitcherPillPaddingHorizontal,
                vertical = SwitcherPillPaddingVertical,
            ),
        )
    }
}

@Composable
private fun SwitcherRowTrailing(row: ActiveSessionSwitcherRowUiModel) {
    when (row.status) {
        ActiveSessionSwitcherStatus.Current -> Text(
            text = stringResource(Res.string.active_session_switcher_now).uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = SwitcherTrailingTextSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = SwitcherNowLetterSpacing,
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )

        ActiveSessionSwitcherStatus.Done -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SwitcherTrailingIconGap),
        ) {
            Icon(
                imageVector = LyteIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(SwitcherTrailingIconSize),
            )
            Text(
                text = stringResource(Res.string.active_session_switcher_done),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = SwitcherTrailingTextSize,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        ActiveSessionSwitcherStatus.Pending -> Unit
    }
}

// Шторки в панели превью не рендерятся (ModalBottomSheet) — превьюим строки напрямую.
@Composable
@Preview
private fun SwitcherRowPreview() {
    LyteTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SwitcherRow(
                row = ActiveSessionSwitcherRowUiModel(
                    exerciseId = "e1",
                    name = "Жим лёжа",
                    status = ActiveSessionSwitcherStatus.Done,
                    doneCount = 4,
                    setCount = 4,
                    currentSetIndex = null,
                    targetPills = emptyList(),
                    isSelectable = false,
                ),
                onIntent = {},
            )
            SwitcherRow(
                row = ActiveSessionSwitcherRowUiModel(
                    exerciseId = "e2",
                    name = "Жим гантелей на наклонной",
                    status = ActiveSessionSwitcherStatus.Current,
                    doneCount = 1,
                    setCount = 3,
                    currentSetIndex = 2,
                    targetPills = emptyList(),
                    isSelectable = true,
                ),
                onIntent = {},
            )
            SwitcherRow(
                row = ActiveSessionSwitcherRowUiModel(
                    exerciseId = "e3",
                    name = "Отжимания на брусьях",
                    status = ActiveSessionSwitcherStatus.Pending,
                    doneCount = 0,
                    setCount = 3,
                    currentSetIndex = null,
                    targetPills = listOf(
                        LyteSetValue(reps = 12),
                        LyteSetValue(reps = 12),
                        LyteSetValue(reps = 10),
                    ),
                    isSelectable = true,
                ),
                onIntent = {},
            )
        }
    }
}
