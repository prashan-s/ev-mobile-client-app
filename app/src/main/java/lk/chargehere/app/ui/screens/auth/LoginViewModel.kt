package lk.chargehere.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.domain.model.UserRole
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigationEvent: LoginNavigationEvent? = null
)

sealed class LoginNavigationEvent {
    data object NavigateToMain : LoginNavigationEvent()
    data object NavigateToOperator : LoginNavigationEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null,
            errorMessage = null
        )
    }
    
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null,
            errorMessage = null
        )
    }
    
    fun login() {
        val currentState = _uiState.value
        
        // Validate email
        val emailError = validateEmail(currentState.email)
        if (emailError != null) {
            _uiState.value = currentState.copy(emailError = emailError)
            return
        }
        
        // Validate password
        val passwordError = validatePassword(currentState.password)
        if (passwordError != null) {
            _uiState.value = currentState.copy(passwordError = passwordError)
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            when (val result = authManager.loginEVOwner(currentState.email, currentState.password)) {
                is Result.Success -> {
                    // User successfully logged in
                    val userRole = authManager.getCurrentUserRole()
                    val navigationEvent = when (userRole) {
                        UserRole.OPERATOR.name -> LoginNavigationEvent.NavigateToOperator
                        else -> LoginNavigationEvent.NavigateToMain
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        navigationEvent = navigationEvent
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Result.Loading -> {
                    // Keep loading state
                }
            }
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
            else -> null
        }
    }
    
    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }
    
    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigationEvent = null)
    }
}