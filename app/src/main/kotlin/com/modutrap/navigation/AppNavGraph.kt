package com.modutrap.navigation

import androidx.compose.foundation.layout.InsetType
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.modutrap.core.AppContainer
import com.modutrap.ui.screens.about.AboutScreen
import com.modutrap.ui.screens.flags.FlagsScreen
import com.modutrap.ui.screens.home.HomeScreen
import com.modutrap.ui.screens.integrations.IntegrationsScreen
import com.modutrap.ui.screens.personalization.PersonalizationScreen
import com.modutrap.ui.screens.settings.GraphicsScreen
import com.modutrap.ui.screens.settings.LaunchScreen
import com.modutrap.ui.screens.settings.PerformanceScreen
import com.modutrap.ui.screens.settings.SettingsScreen

/** Bottom-bar destinations. */
private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val destinations = listOf(
    BottomDestination(Routes.HOME, "Início", Icons.Outlined.Home),
    BottomDestination(Routes.SETTINGS, "Configurações", Icons.Outlined.Settings),
    BottomDestination(Routes.FLAGS, "Fast Flags", Icons.Outlined.ToggleOn),
    BottomDestination(Routes.INTEGRATIONS, "Integrações", Icons.Outlined.Devices),
    BottomDestination(Routes.ABOUT, "Sobre", Icons.Outlined.AccountCircle)
)

/**
 * Root navigation: a [Scaffold] with a bottom bar on the primary destinations
 * and a [NavHost] for every screen. Sub-screens (Graphics, Performance,
 * Launch, Personalization) are pushed without the bottom bar.
 */
@Composable
fun AppNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        // Reserve only the bottom (navigation bar) inset here; each screen's
        // TopAppBar (or the Home screen's statusBarsPadding) handles the top
        // status bar so app bars sit correctly under it.
        contentWindowInsets = WindowInsets.only(InsetType.BOTTOM),
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    destinations.forEach { dest ->
                        val selected = currentRoute == dest.route ||
                            (dest.route == Routes.SETTINGS &&
                                currentRoute in listOf(Routes.GRAPHICS, Routes.PERFORMANCE, Routes.LAUNCH, Routes.PERSONALIZATION))
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(container, onNavigate = { route -> navController.navigate(route) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(container, onNavigate = { route -> navController.navigate(route) })
            }
            composable(Routes.GRAPHICS) {
                GraphicsScreen(container, onBack = { navController.popBackStack() })
            }
            composable(Routes.PERFORMANCE) {
                PerformanceScreen(container, onBack = { navController.popBackStack() })
            }
            composable(Routes.LAUNCH) {
                LaunchScreen(container, onBack = { navController.popBackStack() })
            }
            composable(Routes.PERSONALIZATION) {
                PersonalizationScreen(
                    container,
                    onBack = { navController.popBackStack() },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Routes.FLAGS) { FlagsScreen(container) }
            composable(Routes.INTEGRATIONS) { IntegrationsScreen(container) }
            composable(Routes.ABOUT) { AboutScreen(container) }
        }
    }
}
