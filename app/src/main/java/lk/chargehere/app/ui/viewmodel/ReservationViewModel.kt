package lk.chargehere.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import lk.chargehere.app.auth.AuthManager
import lk.chargehere.app.data.remote.dto.CreateBookingResponse
import lk.chargehere.app.data.repository.ReservationRepository
import lk.chargehere.app.data.repository.StationRepository
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.utils.Result
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ReservationCreationUiState(
    val isLoading: Boolean = false,
    val station: Station? = null,
    val selectedDateTime: Long? = null,
    val bookingResponse: CreateBookingResponse? = null,
    val error: String? = null,
    val isBookingSuccessful: Boolean = false
)

@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val stationRepository: StationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationCreationUiState())
    val uiState: StateFlow<ReservationCreationUiState> = _uiState.asStateFlow()

    /**
     * Load station details for booking
     */
    fun loadStation(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = stationRepository.getChargingStationById(stationId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        station = result.data
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

    /**
     * Set the selected date/time for reservation
     */
    fun setSelectedDateTime(timestampMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDateTime = timestampMillis)
    }

    /**
     * Create booking using CreateBooking endpoint
     * @param stationId The charging station ID
     * @param dateTimeMillis The selected date/time in milliseconds
     */
    fun createBooking(stationId: String, dateTimeMillis: Long) {
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
            
            // Convert timestamp to ISO 8601 format
            val isoDateTime = Instant.ofEpochMilli(dateTimeMillis)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            
            when (val result = reservationRepository.createBooking(nic, stationId, isoDateTime)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookingResponse = result.data,
                        isBookingSuccessful = true
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

    /**
     * Convenience method that creates booking with current time + offset
     */
    fun createBookingNow(stationId: String, offsetMinutes: Int = 0) {
        val targetTime = System.currentTimeMillis() + (offsetMinutes * 60 * 1000L)
        createBooking(stationId, targetTime)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetBooking() {
        _uiState.value = ReservationCreationUiState()
    }
}
