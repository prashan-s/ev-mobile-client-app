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
        if (authManager.isLoggedIn()) {
            // User is logged in, check role and navigate accordingly
            val userRole = authManager.getCurrentUserRole()
            when (userRole) {
                UserRole.OPERATOR.name -> onNavigateToOperator()
                UserRole.OWNER.name -> onNavigateToMain()
                else -> onNavigateToMain() // Default to main
            }
        } else {
            // User is not logged in, show onboarding
            onNavigateToOnboarding()
        }
    }
}