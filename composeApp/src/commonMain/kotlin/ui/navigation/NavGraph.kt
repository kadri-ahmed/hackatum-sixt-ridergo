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
import ui.screens.BookingSummaryScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Chat : Screen("chat")
    object Profile : Screen("profile")
    object VehicleDetail : Screen("vehicle_detail")
    // object Protection : Screen("protection")
    object BookingSummary : Screen("booking_summary")
    object TripDetails : Screen("trip_details")
    object BookingList : Screen("booking_list")
    object BookingDetail : Screen("booking_detail/{bookingId}") {
        fun createRoute(bookingId: String) = "booking_detail/$bookingId"
    }
    object Signup : Screen("signup")
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    onVehicleSelect: (Deal) -> Unit,
    selectedVehicle: Deal?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isDemoMode: Boolean,
    onToggleDemoMode: () -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Signup.route) {
            ui.screens.SignupScreen(
                onSignupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onVehicleSelect = { deal ->
                    onVehicleSelect(deal)
                    navController.navigate(Screen.VehicleDetail.route)
                },
                navigateToProfile = { _, _ -> navController.navigate(Screen.Profile.route) },
                navigateToSearch = { navController.navigate(Screen.Search.route) },
                navigateToTripDetails = { navController.navigate(Screen.TripDetails.route) },
                navigateToChat = { 
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = false
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                popBackStack = { navController.popBackStack() },
                popUpToLogin = { }
            )
        }
        composable(Screen.Search.route) {
            ui.screens.SearchScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TripDetails.route) {
            ui.screens.TripDetailsScreen(
                onBack = { navController.popBackStack() },
                onSearch = { bookingId ->
                    // When trip details are updated, maybe go back to home to see new vehicles?
                    navController.popBackStack()
                }
            )
        }
        composable(Screen.Chat.route) {
            ChatScreen(
                onVehicleSelect = { deal ->
                    onVehicleSelect(deal)
                    navController.navigate(Screen.VehicleDetail.route)
                },
                onBookingSaved = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                isDemoMode = isDemoMode,
                onToggleDemoMode = onToggleDemoMode,
                apiKey = apiKey,
                onApiKeyChange = onApiKeyChange
            )
        }
        composable(Screen.BookingList.route) {
            ui.screens.BookingListScreen(
                onBack = { navController.popBackStack() },
                onBookingSelected = { deal ->
                    onVehicleSelect(deal)
                    navController.navigate(Screen.VehicleDetail.route)
                },
                onBookingDetail = { bookingId ->
                    navController.navigate(Screen.BookingDetail.createRoute(bookingId))
                }
            )
        }
        
        composable(Screen.BookingDetail.route) { backStackEntry ->
            val bookingId = backStackEntry.savedStateHandle.get<String>("bookingId")
            
            if (bookingId != null) {
                ui.screens.BookingDetailScreen(
                    bookingId = bookingId,
                    onBack = { navController.popBackStack() }
                )
            } else {
                androidx.compose.material3.Text("Error: Booking ID missing")
            }
        }
        
        composable(Screen.VehicleDetail.route) {
            if (selectedVehicle != null) {
                VehicleDetailScreen(
                    deal = selectedVehicle,
                    onBack = { navController.popBackStack() },
                    onUpgrade = { deal ->
                        navController.navigate(Screen.BookingSummary.route)
                    }
                )
            }
        }
        // Protection Screen removed as it's now integrated into VehicleDetailScreen
        /*
        composable(Screen.Protection.route) {
            ui.screens.ProtectionScreen(
                onBack = { navController.popBackStack() },
                onConfirm = {
                    navController.navigate(Screen.BookingSummary.route)
                }
            )
        }
        */
        composable(Screen.BookingSummary.route) {
            BookingSummaryScreen(
                onConfirm = {
                    // Reset to home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
