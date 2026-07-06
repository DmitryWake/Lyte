package com.nikolaevskii.lyte.core.design

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Тонкая обёртка над [MaterialTheme] — единая точка входа темы приложения.
 * Семантические токены дизайн-системы (цвета, типографика, spacing) добавятся отдельным этапом.
 */
@Composable
fun LyteTheme(content: @Composable () -> Unit) = MaterialTheme(content = content)
