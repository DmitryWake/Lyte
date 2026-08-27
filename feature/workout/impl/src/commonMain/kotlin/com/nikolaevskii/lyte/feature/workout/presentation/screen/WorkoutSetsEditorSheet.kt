package com.nikolaevskii.lyte.feature.workout.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.overlay.LyteBottomSheet
import com.nikolaevskii.lyte.core.design.component.stepper.LyteSetEditRow
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutExerciseWithRepsEntity
import com.nikolaevskii.lyte.core.workout.domain.model.WorkoutRepEntity
import com.nikolaevskii.lyte.feature.workout.generated.resources.Res
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_set_count
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_set_number
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_sets_add
import com.nikolaevskii.lyte.feature.workout.generated.resources.workout_details_done
import com.nikolaevskii.lyte.feature.workout.presentation.model.mvi.WorkoutDetailsIntent
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val SetsEditorRowGap = 10.dp
private val SetsEditorBottomBarPadding = 28.dp

/**
 * Шторка редактирования подходов упражнения (3.4) — открывается по карандашу на карточке
 * упражнения в редакторе программы, а также сразу после выбора упражнения в
 * [WorkoutExercisePickerSheet]. Правки применяются сразу к состоянию экрана; отдельного
 * сохранения у шторки нет, «Готово» лишь закрывает её. Название упражнения и счётчик подходов —
 * в title/subtitle шторки (закреплены сверху), «Добавить подход» и «Готово» — в её bottomBar
 * (закреплены снизу): ни то ни другое не должно скроллиться вместе со списком подходов.
 *
 * Скролл контента [LyteBottomSheet] не реализует — это делает потребитель. Подходов немного, а их
 * появление/исчезновение анимируется через `animateContentSize` + `AnimatedVisibility`, поэтому
 * здесь обычный `verticalScroll`, а не `LazyColumn`: ленивый список пересоздавал бы строки и ломал
 * эти анимации.
 */
@Composable
fun WorkoutSetsEditorSheet(
    exercise: WorkoutExerciseWithRepsEntity,
    onIntent: (WorkoutDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LyteBottomSheet(
        title = exercise.exercise.name,
        subtitle = pluralStringResource(Res.plurals.workout_details_set_count, exercise.reps.size, exercise.reps.size),
        onDismissRequest = { onIntent(WorkoutDetailsIntent.OnSetsEditorDismissed) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = LyteTheme.elevation.level2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s2),
                    modifier = Modifier.padding(
                        start = LyteTheme.spacing.s5,
                        end = LyteTheme.spacing.s5,
                        top = LyteTheme.spacing.s3,
                        bottom = SetsEditorBottomBarPadding,
                    ),
                ) {
                    // «Добавить подход» прибита к низу вместе с «Готово», а не едет за списком:
                    // при десятке подходов её иначе пришлось бы доскроллить.
                    LyteButton(
                        text = stringResource(Res.string.workout_details_sets_add),
                        onClick = { onIntent(WorkoutDetailsIntent.OnAddSetClicked) },
                        variant = LyteButtonVariant.Tonal,
                        icon = LyteIcons.Plus,
                        fullWidth = true,
                    )
                    LyteButton(
                        text = stringResource(Res.string.workout_details_done),
                        onClick = { onIntent(WorkoutDetailsIntent.OnSetsEditorDismissed) },
                        variant = LyteButtonVariant.Text,
                        fullWidth = true,
                    )
                }
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(
                    start = LyteTheme.spacing.s5,
                    end = LyteTheme.spacing.s5,
                    bottom = LyteTheme.spacing.s5,
                ),
        ) {
            // animateContentSize сглаживает изменение высоты списка при добавлении/удалении подхода —
            // без него соседние строки и кнопка ниже прыгали бы на новое место мгновенно. Плюс сама
            // добавленная строка проявляется через AnimatedVisibility (fade+expand): нужен именно
            // MutableTransitionState с initialState=false — обычный AnimatedVisibility(visible = true)
            // не анимирует появление, если true уже на первой композиции.
            Column(
                verticalArrangement = Arrangement.spacedBy(SetsEditorRowGap),
                modifier = Modifier.animateContentSize(),
            ) {
                exercise.reps.forEachIndexed { index, rep ->
                    key(index) {
                        AnimatedVisibility(
                            visibleState = remember { MutableTransitionState(false) }.apply { targetState = true },
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            LyteSetEditRow(
                                title = stringResource(Res.string.workout_details_set_number, index + 1),
                                reps = rep.count,
                                weight = rep.weight ?: 0.0,
                                onRepsChange = { reps -> onIntent(WorkoutDetailsIntent.OnSetRepsChanged(index, reps)) },
                                onWeightChange = { weight -> onIntent(WorkoutDetailsIntent.OnSetWeightChanged(index, weight)) },
                                // Последний подход удалить нельзя, поэтому у него и кнопки нет.
                                onRemove = if (exercise.reps.size > 1) {
                                    { onIntent(WorkoutDetailsIntent.OnRemoveSetClicked(index)) }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun WorkoutSetsEditorSheetPreview() {
    LyteTheme {
        WorkoutSetsEditorSheet(
            exercise = WorkoutExerciseWithRepsEntity(
                exercise = WorkoutExerciseEntity(
                    id = "1",
                    name = "Жим лёжа",
                    description = "Штанга, горизонтальная скамья, хват чуть шире плеч.",
                ),
                reps = listOf(
                    WorkoutRepEntity(count = 8, weight = 80.0),
                    WorkoutRepEntity(count = 8, weight = 80.0),
                    WorkoutRepEntity(count = 8, weight = 77.5),
                ),
            ),
            onIntent = {},
        )
    }
}
