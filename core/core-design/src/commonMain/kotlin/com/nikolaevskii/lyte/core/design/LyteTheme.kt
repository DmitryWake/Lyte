package com.nikolaevskii.lyte.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.nikolaevskii.lyte.core.design.theme.LocalLyteAccents
import com.nikolaevskii.lyte.core.design.theme.LocalLyteElevation
import com.nikolaevskii.lyte.core.design.theme.LocalLyteExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LocalLyteExtendedShapes
import com.nikolaevskii.lyte.core.design.theme.LocalLyteHitTarget
import com.nikolaevskii.lyte.core.design.theme.LocalLyteMotion
import com.nikolaevskii.lyte.core.design.theme.LocalLyteNumericTypography
import com.nikolaevskii.lyte.core.design.theme.LocalLyteSpacing
import com.nikolaevskii.lyte.core.design.theme.LyteAccents
import com.nikolaevskii.lyte.core.design.theme.LyteDarkAccents
import com.nikolaevskii.lyte.core.design.theme.LyteDarkColorScheme
import com.nikolaevskii.lyte.core.design.theme.LyteDarkExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultElevation
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultExtendedShapes
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultHitTarget
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultMotion
import com.nikolaevskii.lyte.core.design.theme.LyteDefaultSpacing
import com.nikolaevskii.lyte.core.design.theme.LyteElevation
import com.nikolaevskii.lyte.core.design.theme.LyteExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LyteExtendedShapes
import com.nikolaevskii.lyte.core.design.theme.LyteHitTarget
import com.nikolaevskii.lyte.core.design.theme.LyteLightAccents
import com.nikolaevskii.lyte.core.design.theme.LyteLightColorScheme
import com.nikolaevskii.lyte.core.design.theme.LyteLightExtendedColors
import com.nikolaevskii.lyte.core.design.theme.LyteMotion
import com.nikolaevskii.lyte.core.design.theme.LyteNumericTypography
import com.nikolaevskii.lyte.core.design.theme.LyteShapes
import com.nikolaevskii.lyte.core.design.theme.LyteSpacing
import com.nikolaevskii.lyte.core.design.theme.lyteNumericTypography
import com.nikolaevskii.lyte.core.design.theme.lyteTypography

/**
 * Единая точка входа темы приложения: цвета/типографика/форма M3 плюс расширенные токены
 * (semantic success/diff/ai цвета, акценты упражнений, числовая типографика, spacing, elevation,
 * доп. формы, движение, зоны касания), доступные через аксессор [LyteTheme].
 */
@Composable
fun LyteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LyteDarkColorScheme else LyteLightColorScheme
    val extendedColors = if (darkTheme) LyteDarkExtendedColors else LyteLightExtendedColors
    val accents = if (darkTheme) LyteDarkAccents else LyteLightAccents

    CompositionLocalProvider(
        LocalLyteExtendedColors provides extendedColors,
        LocalLyteAccents provides accents,
        LocalLyteNumericTypography provides lyteNumericTypography(),
        LocalLyteSpacing provides LyteDefaultSpacing,
        LocalLyteElevation provides LyteDefaultElevation,
        LocalLyteExtendedShapes provides LyteDefaultExtendedShapes,
        LocalLyteHitTarget provides LyteDefaultHitTarget,
        LocalLyteMotion provides LyteDefaultMotion,
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

    val accents: LyteAccents
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteAccents.current

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

    val motion: LyteMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteMotion.current

    val hitTarget: LyteHitTarget
        @Composable
        @ReadOnlyComposable
        get() = LocalLyteHitTarget.current
}
