package lk.chargehere.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.domain.model.UserRole
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {
    
    fun checkAuthStatus(
        onNavigateToOnboarding: () -> Unit,
        onNavigateToMain: () -> Unit,
        onNavigateToOperator: () -> Unit
    ) {
        android.util.Log.d("SplashViewModel", "Checking auth status")
        val isLoggedIn = authManager.isLoggedIn()
        android.util.Log.d("SplashViewModel", "User logged in: $isLoggedIn")

        if (isLoggedIn) {
            // User is logged in, check role and navigate accordingly
            val userRole = authManager.getCurrentUserRole()
            android.util.Log.d("SplashViewModel", "User role: $userRole")
            when (userRole) {
                UserRole.OPERATOR.name -> {
                    android.util.Log.d("SplashViewModel", "Navigating to Operator screen")
                    onNavigateToOperator()
                }
                UserRole.OWNER.name -> {
                    android.util.Log.d("SplashViewModel", "Navigating to Main screen")
                    onNavigateToMain()
                }
                else -> {
                    android.util.Log.d("SplashViewModel", "Default navigation to Main screen")
                    onNavigateToMain() // Default to main
                }
            }
        } else {
            // User is not logged in, show onboarding
            android.util.Log.d("SplashViewModel", "Navigating to Onboarding screen")
            onNavigateToOnboarding()
        }
    }
}