package lk.chargehere.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import lk.chargehere.app.ui.screens.auth.*
import lk.chargehere.app.ui.screens.main.*
import lk.chargehere.app.ui.screens.operator.*

@Composable
fun ChargeHereNavigation(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Authentication Graph
        navigation(
            startDestination = Screen.Splash.route,
            route = NavigationGraph.Auth.route
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToMain = {
                        navController.navigate(NavigationGraph.Main.route) {
                            popUpTo(NavigationGraph.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToOperator = {
                        navController.navigate(NavigationGraph.Operator.route) {
                            popUpTo(NavigationGraph.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    }
                )
            }
            
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToMain = {
                        navController.navigate(NavigationGraph.Main.route) {
                            popUpTo(NavigationGraph.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToOperator = {
                        navController.navigate(NavigationGraph.Operator.route) {
                            popUpTo(NavigationGraph.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToMain = {
                        navController.navigate(NavigationGraph.Main.route) {
                            popUpTo(NavigationGraph.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Main Graph (EV Owner)
        navigation(
            startDestination = Screen.Home.route,
            route = NavigationGraph.Main.route
        ) {
            composable(Screen.Home.route) {
                android.util.Log.d("ChargeHereNavigation", "Composing MainAppScreen in Main graph")
                MainAppScreen(
                    onNavigateToStationDetail = { stationId ->
                        navController.navigate(Screen.StationDetail.createRoute(stationId))
                    },
                    onNavigateToReservationFlow = { stationId ->
                        navController.navigate(Screen.ReservationFlow.createRoute(stationId))
                    },
                    onNavigateToReservationDetail = { reservationId ->
                        navController.navigate(Screen.ReservationDetail.createRoute(reservationId))
                    },
                    onNavigateToAuth = {
                        navController.navigate(NavigationGraph.Auth.route) {
                            popUpTo(NavigationGraph.Main.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(
                route = Screen.StationDetail.route,
                arguments = listOf(
                    navArgument("stationId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val stationId = backStackEntry.arguments?.getString("stationId") ?: ""
                
                StationDetailScreen(
                    stationId = stationId,
                    onNavigateToReservation = { stationId ->
                        navController.navigate(Screen.ReservationFlow.createRoute(stationId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.ReservationFlow.route,
                arguments = listOf(
                    navArgument("stationId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val stationId = backStackEntry.arguments?.getString("stationId") ?: ""
                
                ReservationFlowScreen(
                    stationId = stationId,
                    onNavigateToConfirmation = { reservationId ->
                        navController.navigate(Screen.ReservationConfirmation.createRoute(reservationId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.ReservationConfirmation.route,
                arguments = listOf(
                    navArgument("reservationId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
                
                ReservationConfirmationScreen(
                    reservationId = reservationId,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToReservations = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }
        }
        
        // Global routes accessible from any navigation graph
        composable(
            route = Screen.ReservationDetail.route,
            arguments = listOf(
                navArgument("reservationId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId") ?: ""
            
            ReservationDetailScreen(
                reservationId = reservationId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Operator Graph
        navigation(
            startDestination = Screen.OperatorHome.route,
            route = NavigationGraph.Operator.route
        ) {
            composable(Screen.OperatorHome.route) {
                OperatorHomeScreen(
                    onNavigateToQRScanner = {
                        navController.navigate(Screen.QRScanner.route)
                    },
                    onNavigateToAuth = {
                        navController.navigate(NavigationGraph.Auth.route) {
                            popUpTo(NavigationGraph.Operator.route) { inclusive = true }
                        }
                    }
                )
            }
            
            composable(Screen.QRScanner.route) {
                QRScannerScreen(
                    onNavigateToSessionValidation = { sessionId ->
                        navController.navigate(Screen.SessionValidation.createRoute(sessionId))
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.SessionValidation.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                
                SessionValidationScreen(
                    sessionId = sessionId,
                    onNavigateToSessionDetail = { sessionId ->
                        navController.navigate(Screen.SessionDetail.createRoute(sessionId)) {
                            popUpTo(Screen.OperatorHome.route)
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(
                route = Screen.SessionDetail.route,
                arguments = listOf(
                    navArgument("sessionId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                
                SessionDetailScreen(
                    sessionId = sessionId,
                    onNavigateToHome = {
                        navController.navigate(Screen.OperatorHome.route) {
                            popUpTo(Screen.OperatorHome.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
