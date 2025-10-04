package lk.chargehere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.data.repository.ReservationRepository
import lk.chargehere.app.data.mapper.toDomain
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class ReservationDetailUiState(
    val isLoading: Boolean = false,
    val reservation: Reservation? = null,
    val error: String? = null,
    val isCancelled: Boolean = false
)

@HiltViewModel
class ReservationDetailViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationDetailUiState())
    val uiState: StateFlow<ReservationDetailUiState> = _uiState.asStateFlow()

    fun loadReservationDetail(reservationId: String) {
        viewModelScope.launch {
            android.util.Log.d("ReservationDetailViewModel", "Loading reservation detail for ID: $reservationId")
            android.util.Log.d("ReservationDetailViewModel", "API Call: GET /api/v1/bookings/$reservationId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Use the GetBookingById endpoint
            when (val result = reservationRepository.getBookingById(reservationId)) {
                is Result.Success -> {
                    android.util.Log.d("ReservationDetailViewModel", "Successfully loaded booking detail from API")
                    // Convert BookingDetailDto to Reservation domain model
                    val bookingDetail = result.data
                    val reservation = bookingDetail.toDomain()
                    android.util.Log.d("ReservationDetailViewModel", "Reservation: ${reservation.stationName}, status: ${reservation.status}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reservation = reservation
                    )
                }
                is Result.Error -> {
                    android.util.Log.e("ReservationDetailViewModel", "Failed to load booking detail: ${result.message}")
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
                    _uiState.value = _uiState.value.copy(
                        isCancelled = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    // Could show loading state for cancellation
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
