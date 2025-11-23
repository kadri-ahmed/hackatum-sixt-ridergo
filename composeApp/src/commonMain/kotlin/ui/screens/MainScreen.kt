package ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dto.Deal
import ui.navigation.NavGraph
import ui.navigation.Screen
import ui.theme.SixtOrange

enum class MainTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String) {
    Home("Home", Icons.Default.Home, Screen.Home.route),
    Bookings("Bookings", Icons.AutoMirrored.Filled.List, Screen.BookingList.route),
    Chat("Chat", Icons.Default.Email, Screen.Chat.route),
    Profile("Profile", Icons.Default.Person, Screen.Profile.route)
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun MainScreen(
    startDestination: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isDemoMode: Boolean,
    onToggleDemoMode: () -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
    val navController = rememberNavController()
    var selectedVehicle by remember { mutableStateOf<Deal?>(null) }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            
            // Hide bottom bar on detail screens if needed, or keep it.
            // For now, we show it unless it's a full screen flow.
            // Hide bottom bar on detail screens and signup
            val currentRoute = currentDestination?.route
            val shouldShowBottomBar = currentRoute != Screen.VehicleDetail.route && currentRoute != Screen.Signup.route
            
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                ) {
                    MainTab.entries.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SixtOrange,
                                selectedTextColor = SixtOrange,
                                indicatorColor = SixtOrange.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
            NavGraph(
                navController = navController,
                startDestination = startDestination,
                onVehicleSelect = { deal ->
                    selectedVehicle = deal
                },selectedVehicle = selectedVehicle,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                isDemoMode = isDemoMode,
                onToggleDemoMode = onToggleDemoMode,
                apiKey = apiKey,
                onApiKeyChange = onApiKeyChange
            )
        }
    }
}
