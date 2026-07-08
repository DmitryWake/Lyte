package com.nikolaevskii.lyte.core.design.component.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/** Одна вкладка [LyteBottomNavigationBar]. */
data class LyteBottomNavigationBarItem(
    val icon: ImageVector,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)
