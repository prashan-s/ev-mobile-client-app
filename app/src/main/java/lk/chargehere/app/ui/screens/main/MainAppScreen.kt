package lk.chargehere.app.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import lk.chargehere.app.navigation.Screen
import lk.chargehere.app.ui.components.ChargeHereBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    onNavigateToStationDetail: (String) -> Unit,
    onNavigateToReservationFlow: (String) -> Unit,
    onNavigateToReservationDetail: (String) -> Unit,
    onNavigateToAuth: () -> Unit
) {
    android.util.Log.d("MainAppScreen", "MainAppScreen composing")
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        android.util.Log.d("MainAppScreen", "Current route: $currentRoute")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content area (full screen for home with maps)
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("home") {
                android.util.Log.d("MainAppScreen", "Composing ModernHomeScreen (home route)")
                ModernHomeScreen(
                    onNavigateToSearch = {
                        navController.navigate("search")
                    },
                    onNavigateToStationDetail = onNavigateToStationDetail,
                    onNavigateToReservationDetail = onNavigateToReservationDetail
                )
            }
            
            composable("search") {
                ModernSearchScreen(
                    onNavigateToStationDetail = onNavigateToStationDetail,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("reservations") {
                android.util.Log.d("MainAppScreen", "Composing ModernReservationsScreen (reservations route)")
                ModernReservationsScreen(
                    onNavigateToReservationDetail = onNavigateToReservationDetail
                )
            }
            
            composable("profile") {
                ModernProfileScreen(
                    onNavigateToAuth = onNavigateToAuth
                )
            }
        }

        // Floating bottom navigation
        ChargeHereBottomNavigation(
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // reselecting the same item
                    launchSingleTop = true
                    // Restore state when reselecting a previously selected item
                    restoreState = true
                }
            },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
        )
    }
}