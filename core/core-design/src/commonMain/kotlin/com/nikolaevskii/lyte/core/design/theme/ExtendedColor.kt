package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Семантические цвета, для которых у Material 3 нет собственных ролей:
 * success (позитивный результат), diff-тона (target→actual в [LyteDiffRow]) и ai-акцент
 * (поверхности, тронутые ИИ — переиспользует tertiary/tertiaryContainer).
 */
data class LyteExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val diffPositive: Color,
    val diffPositiveBg: Color,
    val diffNegative: Color,
    val diffNegativeBg: Color,
    val diffNeutral: Color,
    val diffNeutralBg: Color,
    val diffSkipped: Color,
    val diffSkippedBg: Color,
    val diffMet: Color,
    val diffMetBg: Color,
    val aiAccent: Color,
    val aiAccentContainer: Color,
)

internal val LyteLightExtendedColors = LyteExtendedColors(
    success = Color(0xFF2F7D1F),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD3F8BD),
    onSuccessContainer = Color(0xFF072B00),
    diffPositive = Color(0xFF2F7D1F),
    diffPositiveBg = Color(0xFFD3F8BD),
    diffNegative = Color(0xFFBA1A1A),
    diffNegativeBg = Color(0xFFFFDAD6),
    diffNeutral = Color(0xFF5B6053),
    diffNeutralBg = Color(0xFFE0E5D3),
    diffSkipped = Color(0xFF8F9289),
    diffSkippedBg = Color(0xFFE1E4D9),
    // «попал точно в цель» — мягкий зелёный тинт (отличен от «превысил» = diffPositive).
    // srgb color-mix: diffMet = onSuccessContainer 30% + onSurfaceVariant 70%;
    // diffMetBg = successContainer 16% + surfaceContainerHigh 84%.
    diffMet = Color(0xFF313F2A),
    diffMetBg = Color(0xFFE5EDD9),
    aiAccent = Color(0xFF4B4FAB),
    aiAccentContainer = Color(0xFFE0DEFF),
)

internal val LyteDarkExtendedColors = LyteExtendedColors(
    success = Color(0xFF9ADB85),
    onSuccess = Color(0xFF123B06),
    successContainer = Color(0xFF1C5A0E),
    onSuccessContainer = Color(0xFFD3F8BD),
    diffPositive = Color(0xFF9ADB85),
    diffPositiveBg = Color(0xFF1C5A0E),
    diffNegative = Color(0xFFFFB4AB),
    diffNegativeBg = Color(0xFF93000A),
    diffNeutral = Color(0xFFA8AD9D),
    diffNeutralBg = Color(0xFF43483C),
    diffSkipped = Color(0xFF8F9289),
    diffSkippedBg = Color(0xFF444741),
    diffMet = Color(0xFFC9D7BA),
    diffMetBg = Color(0xFF263322),
    aiAccent = Color(0xFFBCBDFF),
    aiAccentContainer = Color(0xFF363A8C),
)

internal val LocalLyteExtendedColors = staticCompositionLocalOf { LyteLightExtendedColors }
