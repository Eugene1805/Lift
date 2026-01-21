package com.eugene.lift.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eugene.lift.ui.components.LiftBottomNavigationBar
import com.eugene.lift.ui.navigation.BottomNavConfig
import com.eugene.lift.ui.navigation.LiftNavGraph

/**
 * Main app shell that contains the navigation structure and bottom navigation bar.
 *
 * This composable:
 * - Sets up the navigation controller
 * - Displays bottom navigation bar when on main screens
 * - Contains the navigation graph
 */
@Composable
fun MainAppShell() {
    val navController = rememberNavController()
    val bottomNavItems = BottomNavConfig.getBottomNavItems()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Show bottom bar only on main navigation destinations
    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LiftBottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    items = bottomNavItems
                )
            }
        }
    ) { innerPadding ->
        LiftNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
