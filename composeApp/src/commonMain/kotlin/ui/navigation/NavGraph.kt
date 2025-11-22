package ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dto.Deal
import ui.screens.ChatScreen
import ui.screens.HomeScreen
import ui.screens.ProfileScreen
import ui.screens.VehicleDetailScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object VehicleDetail : Screen("vehicle_detail")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    onVehicleSelect: (Deal) -> Unit,
    selectedVehicle: Deal?
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onVehicleSelect = { deal ->
                    onVehicleSelect(deal)
                    navController.navigate(Screen.VehicleDetail.route)
                },
                navigateToProfile = { _, _ -> navController.navigate(Screen.Profile.route) },
                navigateToSearch = { },
                popBackStack = { navController.popBackStack() },
                popUpToLogin = { }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                id = 0,
                showDetails = true,
                popBackStack = { navController.popBackStack() },
                popUpToLogin = { }
            )
        }
        composable(Screen.VehicleDetail.route) {
            if (selectedVehicle != null) {
                VehicleDetailScreen(
                    deal = selectedVehicle,
                    onBack = { navController.popBackStack() },
                    onUpgrade = { /* Handle upgrade */ }
                )
            }
        }
    }
}
