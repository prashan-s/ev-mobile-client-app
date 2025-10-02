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
import lk.chargehere.app.data.repository.StationRepository
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.utils.Result
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val nearbyStations: List<Station> = emptyList(),
    val upcomingReservation: Reservation? = null,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stationRepository: StationRepository,
    private val reservationRepository: ReservationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadNearbyStations(latitude: Double, longitude: Double, radius: Double = 10.0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = stationRepository.getNearbyStations(latitude, longitude, radius)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nearbyStations = result.data
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
     * Load all charging stations from /api/v1/charging-stations
     * Flow: 1. Load from database first (fast)
     *       2. Fetch from API and update database
     *       3. UI refreshes automatically from database
     */
    fun loadAllStations() {
        viewModelScope.launch {
            // Step 1: Load cached stations from database immediately (fast)
            val cachedStations = stationRepository.getCachedStations()

            if (cachedStations.isEmpty()) {
                // Show loading indicator only if no cached data
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    nearbyStations = emptyList(),
                    error = null
                )
            } else {
                // Show cached data immediately
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nearbyStations = cachedStations,
                    error = null
                )
            }

            // Step 2: Fetch from API in the background and update database
            when (val result = stationRepository.getAllStations(page = 1, pageSize = 100)) {
                is Result.Success -> {
                    // Step 3: Update UI with fresh data from API
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nearbyStations = result.data,
                        error = null
                    )
                }
                is Result.Error -> {
                    // Keep cached data visible, just clear loading state
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (cachedStations.isEmpty()) result.message else null
                    )
                }
                is Result.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun loadUpcomingReservations() {
        viewModelScope.launch {
            val nic = authManager.getCurrentUserNic()
            if (nic == null) {
                _uiState.value = _uiState.value.copy(error = "User not authenticated")
                return@launch
            }
            
            when (val result = reservationRepository.getMyReservations(nic)) {
                is Result.Success -> {
                    // Get the most recent upcoming reservation
                    val upcomingReservation = result.data
                        .filter { it.status in listOf("PENDING", "APPROVED") }
                        .sortedBy { it.startTime }
                        .firstOrNull()
                    
                    _uiState.value = _uiState.value.copy(
                        upcomingReservation = upcomingReservation
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message
                    )
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun searchStations(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            when (val result = stationRepository.searchStations(query)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nearbyStations = result.data
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}