package navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ui.screens.BookingSummaryScreen
import ui.screens.ProtectionScreen
import ui.screens.SearchScreen
import ui.screens.VehicleListScreen

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Search,
        modifier = modifier
    ) {
        composable<Screen.Search> {
            SearchScreen(
                onSearch = { destination ->
                    navController.navigate(Screen.VehicleList)
                }
            )
        }
        composable<Screen.VehicleList> {
            VehicleListScreen(
                onBack = { navController.popBackStack() },
                onVehicleSelected = { deal ->
                    navController.navigate(Screen.Protection)
                }
            )
        }
        composable<Screen.Protection> {
            ProtectionScreen(
                onBack = { navController.popBackStack() },
                onConfirm = {
                    navController.navigate(Screen.BookingSummary)
                }
            )
        }
        composable<Screen.BookingSummary> {
            BookingSummaryScreen(
                onBack = { navController.popBackStack() },
                onConfirmBooking = {
                    // Navigate back to search or show success
                    navController.navigate(Screen.Search) {
                        popUpTo(Screen.Search) { inclusive = true }
                    }
                }
            )
        }
    }
}
