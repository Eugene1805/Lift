package com.eugene.lift.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a bottom navigation item.
 *
 * @param T The route type (must be a navigation route class)
 * @param labelRes String resource ID for the navigation item label
 * @param icon Icon to display for this navigation item
 * @param route Navigation route associated with this item
 */
data class BottomNavItem<T : Any>(
    @get:StringRes val labelRes: Int,
    val icon: ImageVector,
    val route: T
)
