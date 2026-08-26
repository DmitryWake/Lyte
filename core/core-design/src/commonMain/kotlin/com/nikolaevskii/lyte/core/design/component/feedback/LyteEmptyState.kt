package com.nikolaevskii.lyte.core.design.component.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.component.button.LyteButton
import com.nikolaevskii.lyte.core.design.component.button.LyteButtonSize
import com.nikolaevskii.lyte.core.design.icon.LyteIcons

private val EmptyStateMinHeight = 320.dp
private val EmptyStatePaddingHorizontal = 32.dp
private val EmptyStatePaddingVertical = 64.dp
private val EmptyStateIconBadgeSize = 112.dp
private val EmptyStateIconSize = 48.dp
private val EmptyStateMessageSpacing = 28.dp
private val EmptyStateMessageMaxWidth = 280.dp
private val EmptyStateHintSpacing = 8.dp
private val EmptyStateHintMaxWidth = 260.dp
private val EmptyStateActionSpacing = 28.dp
private val EmptyStateMessageTracking = (-0.3).sp

/**
 * Полноэкранное пустое состояние: иконка-метка, заголовок, подсказка, одно действие.
 *
 * [actionIcon] рисуется внутри кнопки действия: в списке программ (3.1) она подписана знаком
 * («+ Новая программа»), а в шторке выбора программы (4.1) знака нет — поэтому иконка необязательна,
 * а не прошита в компонент.
 */
@Composable
fun LyteEmptyState(
    message: String,
    icon: ImageVector = LyteIcons.Dumbbell,
    hint: String? = null,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // По центру отведённой высоты, а не по её верхнему краю: блок короче своего минимума.
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = EmptyStateMinHeight)
            .padding(horizontal = EmptyStatePaddingHorizontal, vertical = EmptyStatePaddingVertical),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(EmptyStateIconBadgeSize),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(EmptyStateIconSize),
                )
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = EmptyStateMessageTracking,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = EmptyStateMessageSpacing).widthIn(max = EmptyStateMessageMaxWidth),
        )
        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = EmptyStateHintSpacing).widthIn(max = EmptyStateHintMaxWidth),
            )
        }
        if (actionLabel != null && onAction != null) {
            LyteButton(
                text = actionLabel,
                onClick = onAction,
                size = LyteButtonSize.Small,
                icon = actionIcon,
                modifier = Modifier.padding(top = EmptyStateActionSpacing),
            )
        }
    }
}

@Preview
@Composable
private fun LyteEmptyStatePreview() {
    LyteTheme {
        // Своего фона у пустого состояния нет — оно занимает область экрана, поэтому превью
        // подкладывает поверхность само: иначе в тёмной теме светлый текст лежит на светлом холсте.
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            LyteEmptyState(
                message = "Создайте первую программу",
                icon = LyteIcons.ClipboardList,
                hint = "Программа — это список упражнений и подходов, по которому идёт тренировка.",
                actionLabel = "Новая программа",
                actionIcon = LyteIcons.Plus,
                onAction = {},
            )
        }
    }
}

/** Без действия — так пустое состояние выглядит в истории (5.1) и в списке программ (3.1). */
@Preview
@Composable
private fun LyteEmptyStateWithoutActionPreview() {
    LyteTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            LyteEmptyState(
                message = "Здесь появятся ваши тренировки",
                icon = LyteIcons.History,
                hint = "Итоги сохраняются автоматически после каждой сессии.",
            )
        }
    }
}
