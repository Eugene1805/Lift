package com.eugene.lift.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import com.eugene.lift.R

/**
 * Configuration for bottom navigation items in the app.
 */
object BottomNavConfig {
    /**
     * Returns the list of bottom navigation items for the main app shell.
     */
    fun getBottomNavItems() = listOf(
        BottomNavItem(R.string.nav_profile, Icons.Default.Person, ProfileRoute),
        BottomNavItem(R.string.nav_history, Icons.Default.History, HistoryRoute),
        BottomNavItem(R.string.nav_workout, Icons.Default.Add, WorkoutRoute),
        BottomNavItem(R.string.nav_exercises, Icons.Default.FitnessCenter, ExerciseListRoute),
        BottomNavItem(R.string.nav_settings, Icons.Default.Settings, SettingsRoute)
    )
}
