package com.nikolaevskii.lyte.core.design.icon

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.CircleCheckBig
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.CircleMinus
import com.composables.icons.lucide.CirclePlus
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.Dumbbell
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.History
import com.composables.icons.lucide.ListChecks
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquarePlus
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.PencilLine
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X

/**
 * Небольшой, осознанно ограниченный словарь иконок Lyte поверх Lucide
 * (см. `assets/ICONOGRAPHY.md` дизайн-системы) — единая точка подключения,
 * не позволяющая фичам тянуть произвольную иконку из полного набора Lucide.
 */
object LyteIcons {
    val Dumbbell: ImageVector get() = Lucide.Dumbbell
    val ClipboardList: ImageVector get() = Lucide.ClipboardList
    val History: ImageVector get() = Lucide.History
    val Plus: ImageVector get() = Lucide.Plus
    val Minus: ImageVector get() = Lucide.Minus
    val Check: ImageVector get() = Lucide.Check
    val Close: ImageVector get() = Lucide.X
    val ChevronRight: ImageVector get() = Lucide.ChevronRight
    val ChevronDown: ImageVector get() = Lucide.ChevronDown
    val ChevronLeft: ImageVector get() = Lucide.ChevronLeft
    val GripVertical: ImageVector get() = Lucide.GripVertical
    val OverflowMenu: ImageVector get() = Lucide.EllipsisVertical
    val Search: ImageVector get() = Lucide.Search
    val Sparkles: ImageVector get() = Lucide.Sparkles
    val AddNote: ImageVector get() = Lucide.MessageSquarePlus
    val SkipForward: ImageVector get() = Lucide.SkipForward
    val AddCircle: ImageVector get() = Lucide.CirclePlus
    val Delete: ImageVector get() = Lucide.Trash2
    val Edit: ImageVector get() = Lucide.PencilLine
    val ListChecks: ImageVector get() = Lucide.ListChecks
    val Circle: ImageVector get() = Lucide.Circle
    val CircleDot: ImageVector get() = Lucide.CircleDot
    val CircleCheck: ImageVector get() = Lucide.CircleCheckBig
    val CircleX: ImageVector get() = Lucide.CircleX
    val CircleMinus: ImageVector get() = Lucide.CircleMinus
}
