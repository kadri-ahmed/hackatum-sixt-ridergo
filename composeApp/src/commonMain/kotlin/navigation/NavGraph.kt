package navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ui.screens.BookingSummaryScreen
import ui.screens.LandingScreen
import ui.screens.ProtectionScreen
import ui.screens.SearchScreen
import ui.screens.SettingsScreen
import ui.screens.VehicleListScreen

// Shared animation specs
private val slideInOffset = 300
private val animationDuration = 400

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetupNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing,
        modifier = modifier
    ) {
        composable<Screen.Landing>(
            enterTransition = { fadeIn(tween(animationDuration)) },
            exitTransition = { fadeOut(tween(animationDuration / 2)) }
        ) {
            LandingScreen(
                onQuickBook = {
                    navController.navigate(Screen.Search)
                },
                onBrowseVehicles = {
                    navController.navigate(Screen.VehicleList)
                },
                onSettings = {
                    navController.navigate(Screen.Settings)
                }
            )
        }
        composable<Screen.Search>(
            enterTransition = {
                slideInHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            exitTransition = {
                slideOutHorizontally(tween(animationDuration)) { -slideInOffset } +
                        fadeOut(tween(animationDuration))
            }
        ) {
            SearchScreen(
                onSearch = { destination ->
                    navController.navigate(Screen.VehicleList)
                }
            )
        }
        composable<Screen.VehicleList>(
            enterTransition = {
                slideInHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            exitTransition = {
                slideOutHorizontally(tween(animationDuration)) { -slideInOffset } +
                        fadeOut(tween(animationDuration))
            },
            popEnterTransition = {
                slideInHorizontally(tween(animationDuration)) { -slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            popExitTransition = {
                slideOutHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeOut(tween(animationDuration))
            }
        ) {
            VehicleListScreen(
                onBack = { navController.popBackStack() },
                onVehicleSelected = { deal ->
                    navController.navigate(Screen.Protection)
                }
            )
        }
        composable<Screen.Protection>(
            enterTransition = {
                slideInHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            exitTransition = {
                slideOutHorizontally(tween(animationDuration)) { -slideInOffset } +
                        fadeOut(tween(animationDuration))
            },
            popEnterTransition = {
                slideInHorizontally(tween(animationDuration)) { -slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            popExitTransition = {
                slideOutHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeOut(tween(animationDuration))
            }
        ) {
            ProtectionScreen(
                onBack = { navController.popBackStack() },
                onConfirm = {
                    navController.navigate(Screen.BookingSummary)
                }
            )
        }
        composable<Screen.BookingSummary>(
            enterTransition = {
                slideInHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            exitTransition = {
                fadeOut(tween(animationDuration))
            },
            popEnterTransition = {
                slideInHorizontally(tween(animationDuration)) { -slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            popExitTransition = {
                slideOutHorizontally(tween(animationDuration)) { slideInOffset } +
                        fadeOut(tween(animationDuration))
            }
        ) {
            BookingSummaryScreen(
                onBack = { navController.popBackStack() },
                onConfirmBooking = {
                    // Navigate back to landing or show success
                    navController.navigate(Screen.Landing) {
                        popUpTo(Screen.Landing) { inclusive = true }
                    }
                }
            )
        }
        composable<Screen.Settings>(
            enterTransition = {
                slideInVertically(tween(animationDuration)) { slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            exitTransition = {
                slideOutVertically(tween(animationDuration)) { slideInOffset } +
                        fadeOut(tween(animationDuration))
            },
            popEnterTransition = {
                slideInVertically(tween(animationDuration)) { slideInOffset } +
                        fadeIn(tween(animationDuration))
            },
            popExitTransition = {
                slideOutVertically(tween(animationDuration)) { slideInOffset } +
                        fadeOut(tween(animationDuration))
            }
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
