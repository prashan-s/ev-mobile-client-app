package lk.chargehere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.data.repository.UserRepository
import lk.chargehere.app.data.repository.ReservationRepository
import lk.chargehere.app.domain.model.User
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val totalReservations: Int = 0,
    val totalChargingTime: Int = 0,
    val favoriteStations: Int = 0,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reservationRepository: ReservationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val nic = authManager.getCurrentUserNic()
            if (nic == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }
            
            // Load user profile
            when (val userResult = userRepository.getCurrentProfile(nic)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        user = userResult.data,
                        isLoading = false
                    )
                    loadUserStats(nic)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = userResult.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun loadUserStats(nic: String) {
        viewModelScope.launch {
            // Load reservations to calculate stats
            when (val reservationsResult = reservationRepository.getMyReservations(nic)) {
                is Result.Success -> {
                    val reservations = reservationsResult.data
                    val completedCount = reservations.count { it.status.equals("COMPLETED", ignoreCase = true) }
                    
                    _uiState.value = _uiState.value.copy(
                        totalReservations = reservations.size,
                        totalChargingTime = completedCount * 60, // Assuming 60 minutes average per session
                        favoriteStations = 5 // Placeholder value
                    )
                }
                is Result.Error -> {
                    // Don't show error for stats, just keep defaults
                }
                is Result.Loading -> {
                    // Loading handled at profile level
                }
            }
        }
    }

    fun updateProfile(fullName: String, email: String?, phone: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val nic = authManager.getCurrentUserNic()
            if (nic == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }
            
            val nameParts = fullName.trim().split(" ", limit = 2)
            val firstName = nameParts.getOrNull(0).orEmpty()
            val lastName = nameParts.getOrNull(1) ?: ""
            
            when (val result = userRepository.updateProfile(nic, firstName, lastName, email, phone)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = result.data
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun deactivateAccount() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val nic = authManager.getCurrentUserNic()
            if (nic == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }
            
            when (val result = authManager.deactivateAccount(nic)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedOut = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Call deactivate endpoint if needed (optional based on business logic)
                // userRepository.deactivateAccount()
                
                // Clear local auth data
                authManager.logout()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedOut = true
                )
            } catch (e: Exception) {
                // Even if server call fails, clear local data
                authManager.logout()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedOut = true
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
