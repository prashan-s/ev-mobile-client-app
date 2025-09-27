package lk.chargehere.app.navigation

sealed class Screen(val route: String) {
    // Authentication Flow
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")
    data object Register : Screen("register")
    
    // Main Flow - EV Owner
    data object Home : Screen("home")
    data object StationDetail : Screen("station_detail/{stationId}") {
        fun createRoute(stationId: String) = "station_detail/$stationId"
    }
    data object ReservationFlow : Screen("reservation_flow/{stationId}") {
        fun createRoute(stationId: String) = "reservation_flow/$stationId"
    }
    data object ReservationConfirmation : Screen("reservation_confirmation/{reservationId}") {
        fun createRoute(reservationId: String) = "reservation_confirmation/$reservationId"
    }
    data object ReservationDetail : Screen("reservation_detail/{reservationId}") {
        fun createRoute(reservationId: String) = "reservation_detail/$reservationId"
    }
    
    // Operator Flow
    data object OperatorHome : Screen("operator_home")
    data object QRScanner : Screen("qr_scanner")
    data object SessionValidation : Screen("session_validation/{sessionId}") {
        fun createRoute(sessionId: String) = "session_validation/$sessionId"
    }
    data object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: String) = "session_detail/$sessionId"
    }
}

// Navigation graphs
sealed class NavigationGraph(val route: String) {
    data object Auth : NavigationGraph("auth_graph")
    data object Main : NavigationGraph("main_graph")
    data object Operator : NavigationGraph("operator_graph")
}