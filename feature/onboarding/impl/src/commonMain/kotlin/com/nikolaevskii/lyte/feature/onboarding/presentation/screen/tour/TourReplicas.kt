package com.nikolaevskii.lyte.feature.onboarding.presentation.screen.tour

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.icon.LyteIcons
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonVariant
import com.nikolaevskii.lyte.core.design.component.card.LyteSessionCard
import com.nikolaevskii.lyte.core.design.component.feedback.LyteEmptyState
import com.nikolaevskii.lyte.core.design.component.overline.LyteOverline
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTone
import com.nikolaevskii.lyte.core.design.component.progress.LyteProgressTrackMode
import com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetRow
import com.nikolaevskii.lyte.core.design.component.session.LyteTrackSetState
import com.nikolaevskii.lyte.core.design.theme.LyteAccent
import com.nikolaevskii.lyte.core.design.icon.LyteExerciseGlyph
import com.nikolaevskii.lyte.core.design.model.LyteSetValue
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.Res
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_history_duration
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_history_overline
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_history_subtitle
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_history_title
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_landing_hint
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_landing_start
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_landing_title
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_set_done
import com.nikolaevskii.lyte.feature.onboarding.generated.resources.onboarding_replica_set_skip
import org.jetbrains.compose.resources.stringResource

/**
 * Раскладка реплик задана константами, а не измеряется: прямоугольник подсветки берётся из этих же
 * величин, поэтому вырез не может разъехаться с тем, что нарисовано. Замер поверх живого дерева дал
 * бы точность, но и молчаливую поломку при первом же рефакторинге разметки.
 */
internal val ReplicaPaddingHorizontal = 20.dp
internal val ReplicaTopGap = 72.dp
internal val ReplicaSetRowHeight = 172.dp
internal val ReplicaActionsGap = 10.dp
internal val ReplicaPrimaryActionHeight = 64.dp
internal val ReplicaTextActionHeight = 48.dp
internal val ReplicaHistoryCardHeight = 96.dp
internal val ReplicaHistoryOverlineHeight = 28.dp

/** Кадр 0.1: лендинг трекера. Подсвечивается кнопка «Начать» — [StartReplicaButtonTop]. */
@Composable
internal fun StartReplica(modifier: Modifier = Modifier) {
    LyteEmptyState(
        message = stringResource(Res.string.onboarding_replica_landing_title),
        icon = LyteIcons.Dumbbell,
        hint = stringResource(Res.string.onboarding_replica_landing_hint),
        actionLabel = stringResource(Res.string.onboarding_replica_landing_start),
        actionIcon = LyteIcons.Play,
        onAction = {},
        modifier = modifier.fillMaxSize(),
    )
}

/**
 * Кадры 0.2 и 0.3: карточка текущего подхода. Тот же [LyteTrackSetRow], что на боевом экране, —
 * реплика собрана из компонента, а не нарисована заново, поэтому разойтись они могут только вместе
 * с самим компонентом.
 *
 * Ориентиры заполнены обе строки: [LyteTrackSetState.Current.last] появился в RD-27, и без него
 * реплика оказалась бы ниже боевой карточки.
 */
@Composable
internal fun SetReplica(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ReplicaActionsGap),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ReplicaPaddingHorizontal),
    ) {
        Spacer(modifier = Modifier.height(ReplicaTopGap))
        LyteTrackSetRow(
            number = 3,
            state = LyteTrackSetState.Current(
                total = 4,
                reps = 10,
                weight = 62.5,
                target = LyteSetValue(reps = 10, weight = 62.5),
                last = LyteSetValue(reps = 8, weight = 57.5),
            ),
            modifier = Modifier.height(ReplicaSetRowHeight),
        )
        LyteButton(
            text = stringResource(Res.string.onboarding_replica_set_done),
            onClick = {},
            size = LyteButtonSize.Large,
            fullWidth = true,
        )
        LyteButton(
            text = stringResource(Res.string.onboarding_replica_set_skip),
            onClick = {},
            variant = LyteButtonVariant.Text,
            fullWidth = true,
        )
    }
}

/** Кадр 0.4: карточка тренировки в истории вместе со своим месячным заголовком. */
@Composable
internal fun HistoryReplica(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ReplicaPaddingHorizontal),
    ) {
        Spacer(modifier = Modifier.height(ReplicaTopGap))
        LyteOverline(
            text = stringResource(Res.string.onboarding_replica_history_overline),
            modifier = Modifier.height(ReplicaHistoryOverlineHeight),
        )
        LyteSessionCard(
            title = stringResource(Res.string.onboarding_replica_history_title),
            subtitle = stringResource(Res.string.onboarding_replica_history_subtitle),
            duration = stringResource(Res.string.onboarding_replica_history_duration),
            accent = LyteAccent.Slate,
            glyph = LyteExerciseGlyph.BenchPress,
            onClick = {},
            track = LyteProgressTrackMode.Tones(tones = historyReplicaTones()),
            modifier = Modifier.height(ReplicaHistoryCardHeight),
        )
    }
}

/** Пятнадцать выполненных подходов и один пропущенный — ровно та сводка, что стоит в подписи. */
private fun historyReplicaTones(): List<LyteProgressTone> =
    List(15) { LyteProgressTone.Met } + LyteProgressTone.Skipped

/** Заголовок реплики лендинга по центру — вспомогательный текст для превью самих реплик. */
@Composable
internal fun ReplicaCaption(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/** Верх кнопки «Начать» внутри [StartReplica]: пустое состояние центрируется, поэтому считается от него. */
internal fun startReplicaButtonTop(containerHeight: Dp): Dp =
    containerHeight / 2 + LyteEmptyStateActionOffset

/** Расстояние от центра пустого состояния до верха его кнопки действия — из раскладки `LyteEmptyState`. */
private val LyteEmptyStateActionOffset = 62.dp
