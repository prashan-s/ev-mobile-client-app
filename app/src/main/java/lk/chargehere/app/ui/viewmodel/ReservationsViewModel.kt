package lk.chargehere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.data.repository.ReservationRepository
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class ReservationsUiState(
    val isLoading: Boolean = false,
    val upcomingReservations: List<Reservation> = emptyList(),
    val pastReservations: List<Reservation> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    fun loadReservations() {
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
            
            when (val result = reservationRepository.getMyReservations(nic)) {
                is Result.Success -> {
                    val allReservations = result.data
                    val now = System.currentTimeMillis()
                    
                    val upcoming = allReservations.filter { reservation ->
                        reservation.status in listOf("PENDING", "APPROVED") && 
                        reservation.startTime > now
                    }.sortedBy { it.startTime }
                    
                    val past = allReservations.filter { reservation ->
                        reservation.status in listOf("COMPLETED", "CANCELLED") ||
                        (reservation.startTime <= now)
                    }.sortedByDescending { it.startTime }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        upcomingReservations = upcoming,
                        pastReservations = past
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

    fun cancelReservation(reservationId: String, reason: String? = null) {
        viewModelScope.launch {
            when (val result = reservationRepository.cancelReservation(reservationId, reason)) {
                is Result.Success -> {
                    // Reload reservations to reflect the change
                    loadReservations()
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    // Could show loading state for specific reservation
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}