package lk.chargehere.app.ui.screens.operator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.data.mapper.toDomain
import lk.chargehere.app.data.repository.ReservationRepository
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class StationManagerBookingUiState(
    val booking: Reservation? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleting: Boolean = false,
    val completionError: String? = null,
    val completionSuccess: Boolean = false
)

@HiltViewModel
class StationManagerBookingViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StationManagerBookingUiState())
    val uiState: StateFlow<StationManagerBookingUiState> = _uiState.asStateFlow()

    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = reservationRepository.getBookingById(bookingId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        booking = result.data.toDomain(),
                        isLoading = false
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                is Result.Loading -> {
                    // Keep loading
                }
            }
        }
    }

    fun completeBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCompleting = true,
                completionError = null
            )

            when (val result = reservationRepository.completeBooking(bookingId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCompleting = false,
                        completionSuccess = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCompleting = false,
                        completionError = result.message
                    )
                }
                is Result.Loading -> {
                    // Keep loading
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(completionError = null)
    }

    fun onCompletionHandled() {
        _uiState.value = _uiState.value.copy(completionSuccess = false)
    }
}
