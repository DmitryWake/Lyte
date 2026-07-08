package com.nikolaevskii.lyte.core.design.component.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nikolaevskii.lyte.core.design.LyteTheme

enum class LyteSetDotState { Hit, Miss, Skipped, Current, Todo }

private val SetDotSize = 8.dp
private val SetDotCurrentWidth = 26.dp
private val SetDotGap = 8.dp

/**
 * Компактный индикатор прогресса подходов: по точке на подход, текущий — вытянутая «пилюля».
 * Беглая сводка «как идут подходы» без полной раскладки (см. [LyteSetOverview]).
 */
@Composable
fun LyteSetDots(
    states: List<LyteSetDotState>,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(SetDotGap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        states.forEach { state ->
            Box(
                modifier = Modifier
                    .width(if (state == LyteSetDotState.Current) SetDotCurrentWidth else SetDotSize)
                    .height(SetDotSize)
                    .clip(CircleShape)
                    .background(setDotColor(state)),
            )
        }
    }
}

@Composable
private fun setDotColor(state: LyteSetDotState): Color = when (state) {
    LyteSetDotState.Hit -> MaterialTheme.colorScheme.primary
    LyteSetDotState.Miss -> LyteTheme.extendedColors.diffNegative
    LyteSetDotState.Skipped -> LyteTheme.extendedColors.diffSkipped
    LyteSetDotState.Current -> MaterialTheme.colorScheme.onSurface
    LyteSetDotState.Todo -> MaterialTheme.colorScheme.surfaceContainerHighest
}

@Preview
@Composable
private fun LyteSetDotsPreview() {
    LyteTheme {
        LyteSetDots(
            states = listOf(
                LyteSetDotState.Hit,
                LyteSetDotState.Hit,
                LyteSetDotState.Miss,
                LyteSetDotState.Current,
                LyteSetDotState.Todo,
                LyteSetDotState.Todo,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}
