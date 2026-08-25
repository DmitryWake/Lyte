package com.nikolaevskii.lyte.core.design.component.brand

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.LyteTheme
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.wordmark
import com.nikolaevskii.lyte.core.design.generated.resources.wordmark_accent
import com.nikolaevskii.lyte.core.design.theme.lyteWordmarkFontFamily
import org.jetbrains.compose.resources.stringResource

private val WordmarkLetterSpacing = (-0.5).sp

// Интерлиньяж задан явно, долей кегля: в макете вордмарк набран 40/46, то есть 1.15. Дефолт шрифта
// даёт другое отношение, и знак вставал бы в блок разной высоты в зависимости от кегля.
private const val WordmarkLineHeightRatio = 1.15f

private const val WordmarkFullDotAlpha = 1f

private val PreviewWordmarkFontSize = 40.sp
private val PreviewLargeWordmarkFontSize = 64.sp
private const val PreviewDimmedDotAlpha = 0.32f

/**
 * Вордмарк «Lyte.» — знак бренда: «Lyte» в `onSurface` и акцентная точка в `primary` на общей базовой
 * линии. Единственное место в системе, где используется Inter Tight (см. `lyteWordmarkFontFamily`).
 *
 * [fontSize] задаёт вызывающий: на сплэше кегль адаптивный (доля ширины экрана), на экранах — из
 * макета. [dotAlpha] выведен наружу ради «дышащей» точки сплэша; альфа применяется слоем, поэтому
 * анимация не перезапускает разметку текста.
 */
@Composable
fun LyteWordmark(
    fontSize: TextUnit,
    dotAlpha: Float = WordmarkFullDotAlpha,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.wordmark),
            fontFamily = lyteWordmarkFontFamily(),
            fontSize = fontSize,
            lineHeight = fontSize * WordmarkLineHeightRatio,
            letterSpacing = WordmarkLetterSpacing,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(Res.string.wordmark_accent),
            fontFamily = lyteWordmarkFontFamily(),
            fontSize = fontSize,
            lineHeight = fontSize * WordmarkLineHeightRatio,
            letterSpacing = WordmarkLetterSpacing,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer { alpha = dotAlpha },
        )
    }
}

@Preview
@Composable
private fun LyteWordmarkPreview() {
    LyteTheme {
        // Своего фона у знака нет — превью подкладывает поверхность, иначе в тёмной теме светлый
        // текст лёг бы на светлый холст.
        Column(
            verticalArrangement = Arrangement.spacedBy(LyteTheme.spacing.s4),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(LyteTheme.spacing.s5),
        ) {
            LyteWordmark(fontSize = PreviewWordmarkFontSize)
            LyteWordmark(fontSize = PreviewLargeWordmarkFontSize, dotAlpha = PreviewDimmedDotAlpha)
        }
    }
}
