package lk.chargehere.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class RegisterUiState(
    val nic: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val nicError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    
    fun updateNic(nic: String) {
        _uiState.value = _uiState.value.copy(
            nic = nic,
            nicError = null,
            errorMessage = null
        )
    }
    
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
    
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null,
            errorMessage = null
        )
    }
    
    fun updateFirstName(firstName: String) {
        _uiState.value = _uiState.value.copy(
            firstName = firstName,
            firstNameError = null,
            errorMessage = null
        )
    }
    
    fun updateLastName(lastName: String) {
        _uiState.value = _uiState.value.copy(
            lastName = lastName,
            lastNameError = null,
            errorMessage = null
        )
    }
    
    fun updatePhoneNumber(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(
            phoneNumber = phoneNumber,
            errorMessage = null
        )
    }
    
    fun register() {
        val currentState = _uiState.value
        
        // Validate all fields
        val nicError = validateNic(currentState.nic)
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)
        val confirmPasswordError = validateConfirmPassword(currentState.password, currentState.confirmPassword)
        val firstNameError = validateFirstName(currentState.firstName)
        val lastNameError = validateLastName(currentState.lastName)
        
        if (nicError != null || emailError != null || passwordError != null || 
            confirmPasswordError != null || firstNameError != null || lastNameError != null) {
            _uiState.value = currentState.copy(
                nicError = nicError,
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                firstNameError = firstNameError,
                lastNameError = lastNameError
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
            
            val result = authManager.registerEVOwner(
                nic = currentState.nic,
                email = currentState.email,
                password = currentState.password,
                firstName = currentState.firstName,
                lastName = currentState.lastName,
                phoneNumber = currentState.phoneNumber.ifBlank { null }
            )
            
            when (result) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegistrationSuccessful = true
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
    
    private fun validateNic(nic: String): String? {
        return when {
            nic.isBlank() -> "NIC is required"
            nic.length < 9 -> "NIC must be at least 9 characters"
            nic.length > 12 -> "NIC must be at most 12 characters"
            !isValidNicFormat(nic) -> "Invalid NIC format"
            else -> null
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
    
    private fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Please confirm your password"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
    
    private fun validateFirstName(firstName: String): String? {
        return when {
            firstName.isBlank() -> "First name is required"
            firstName.length < 2 -> "First name must be at least 2 characters"
            else -> null
        }
    }
    
    private fun validateLastName(lastName: String): String? {
        return when {
            lastName.isBlank() -> "Last name is required"
            lastName.length < 2 -> "Last name must be at least 2 characters"
            else -> null
        }
    }
    
    private fun isValidNicFormat(nic: String): Boolean {
        // Simple NIC validation - adjust based on your country's format
        // This is a basic example for Sri Lankan NIC format
        val oldNicPattern = "^[0-9]{9}[vVxX]$".toRegex()
        val newNicPattern = "^[0-9]{12}$".toRegex()
        
        return nic.matches(oldNicPattern) || nic.matches(newNicPattern)
    }
}