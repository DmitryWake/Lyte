package com.nikolaevskii.lyte.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.nikolaevskii.lyte.core.design.generated.resources.Res
import com.nikolaevskii.lyte.core.design.generated.resources.inter_tight_bold
import com.nikolaevskii.lyte.core.design.generated.resources.space_grotesk_bold
import com.nikolaevskii.lyte.core.design.generated.resources.space_grotesk_medium
import com.nikolaevskii.lyte.core.design.generated.resources.space_grotesk_regular
import com.nikolaevskii.lyte.core.design.generated.resources.space_grotesk_semibold
import org.jetbrains.compose.resources.Font

/** Табличные цифры — числа не "прыгают" при обновлении (секундомер, степпер). */
private const val TABULAR_NUMS_FEATURE = "tnum"

/** Применяет табличные цифры к любому текстовому стилю (напр. числовая сводка на body-размере). */
fun TextStyle.withTabularNums(): TextStyle = copy(fontFeatureSettings = TABULAR_NUMS_FEATURE)

@Composable
fun lyteFontFamily(): FontFamily = FontFamily(
    Font(Res.font.space_grotesk_regular, weight = FontWeight.Normal),
    Font(Res.font.space_grotesk_medium, weight = FontWeight.Medium),
    Font(Res.font.space_grotesk_semibold, weight = FontWeight.SemiBold),
    Font(Res.font.space_grotesk_bold, weight = FontWeight.Bold),
)

/** Шрифт вордмарка «Lyte» — сохранённое утверждённое начертание Inter Tight, только для логотипа. */
@Composable
fun lyteWordmarkFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_tight_bold, weight = FontWeight.Bold),
)

@Composable
fun lyteTypography(): Typography {
    val brand = lyteFontFamily()
    return Typography(
        displayLarge = TextStyle(fontFamily = brand, fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
        displayMedium = TextStyle(fontFamily = brand, fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),
        displaySmall = TextStyle(fontFamily = brand, fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),
        headlineLarge = TextStyle(fontFamily = brand, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
        headlineMedium = TextStyle(fontFamily = brand, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontFamily = brand, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
        titleLarge = TextStyle(fontFamily = brand, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontFamily = brand, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontFamily = brand, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = brand, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.15.sp),
        bodyMedium = TextStyle(fontFamily = brand, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = brand, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = brand, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = brand, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = brand, fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
    )
}

/**
 * Табличная числовая шкала для «живых» чисел (секундомер, степпер, счётчики подходов) —
 * никогда не используется для обычного текста, только там, где цифры обновляются на глазах.
 */
data class LyteNumericTypography(
    val hero: TextStyle,
    val large: TextStyle,
    val medium: TextStyle,
)

@Composable
fun lyteNumericTypography(): LyteNumericTypography {
    val brand = lyteFontFamily()
    fun numericStyle(size: TextUnit, lineHeight: TextUnit, weight: FontWeight): TextStyle = TextStyle(
        fontFamily = brand,
        fontSize = size,
        lineHeight = lineHeight,
        fontWeight = weight,
        fontFeatureSettings = TABULAR_NUMS_FEATURE,
    )
    return LyteNumericTypography(
        hero = numericStyle(size = 64.sp, lineHeight = 68.sp, weight = FontWeight.Bold),
        large = numericStyle(size = 40.sp, lineHeight = 44.sp, weight = FontWeight.Bold),
        medium = numericStyle(size = 24.sp, lineHeight = 28.sp, weight = FontWeight.SemiBold),
    )
}

internal val LocalLyteNumericTypography = staticCompositionLocalOf<LyteNumericTypography> {
    error("LyteNumericTypography not provided — wrap content in LyteTheme")
}
