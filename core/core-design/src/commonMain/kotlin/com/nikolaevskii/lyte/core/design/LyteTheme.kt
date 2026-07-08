package com.nikolaevskii.lyte.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.nikolaevskii.lyte.core.design.theme.LocalLyteElevation
import com.nikolaevskii.lyte.core.design.theme.LocalLyteExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LocalLyteExtendedShapes
import com.nikolaevskii.lyte.core.design.theme.LocalLyteNumericTypography
import com.nikolaevskii.lyte.core.design.theme.LocalLyteSpacing
import com.nikolaevskii.lyte.core.design.theme.LyteDarkColorScheme
import com.nikolaevskii.lyte.core.design.theme.LyteDarkExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultElevation
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultExtendedShapes
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultSpacing
import com.nikolaevskii.lyte.core.design.theme.LyteElevation
import com.nikolaevskii.lyte.core.design.theme.LyteExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LyteExtendedShapes
import com.nikolaevskii.lyte.core.design.theme.LyteLightColorScheme
import com.nikolaevskii.lyte.core.design.theme.LyteLightExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LyteNumericTypography
import com.nikolaevskii.lyte.core.design.theme.LyteShapes
import com.nikolaevskii.lyte.core.design.theme.LyteSpacing
import com.nikolaevskii.lyte.core.design.theme.lyteNumericTypography
import com.nikolaevskii.lyte.core.design.theme.lyteTypography

/**
 * Единая точка входа темы приложения: цвета/типографика/форма M3 плюс расширенные токены
 * (semantic success/diff/ai цвета, числовая типографика, spacing, elevation, доп. формы),
 * доступные через аксессор [LyteTheme].
 */
@Composable
fun LyteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LyteDarkColorScheme else LyteLightColorScheme
    val extendedColors = if (darkTheme) LyteDarkExtendedColors else LyteLightExtendedColors

    CompositionLocalProvider(
        LocalLyteExtendedColors provides extendedColors,
        LocalLyteNumericTypography provides lyteNumericTypography(),
        LocalLyteSpacing provides LyteDefaultSpacing,
        LocalLyteElevation provides LyteDefaultElevation,
        LocalLyteExtendedShapes provides LyteDefaultExtendedShapes,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = lyteTypography(),
            shapes = LyteShapes,
            content = content,
        )
    }
}

/** Аксессор к расширенным токенам дизайн-системы, по аналогии с [MaterialTheme]. */
object LyteTheme {
    val extendedColors: LyteExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteExtendedColors.current

    val numericTypography: LyteNumericTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteNumericTypography.current

    val spacing: LyteSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteSpacing.current

    val elevation: LyteElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteElevation.current

    val extendedShapes: LyteExtendedShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteExtendedShapes.current
}
