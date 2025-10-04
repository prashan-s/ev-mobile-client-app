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

    init {
        android.util.Log.d("HomeViewModel", "HomeViewModel initialized - loading stations")
        loadAllStations()
        loadUpcomingReservations()
    }

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
            android.util.Log.d("HomeViewModel", "Loading all stations")

            // Step 1: Load cached stations from database immediately (fast)
            val cachedStations = stationRepository.getCachedStations()
            android.util.Log.d("HomeViewModel", "Loaded ${cachedStations.size} cached stations from database")

            if (cachedStations.isEmpty()) {
                // Show loading indicator only if no cached data
                android.util.Log.d("HomeViewModel", "No cached stations - showing loading indicator")
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    nearbyStations = emptyList(),
                    error = null
                )
            } else {
                // Show cached data immediately
                android.util.Log.d("HomeViewModel", "Showing ${cachedStations.size} cached stations")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    nearbyStations = cachedStations,
                    error = null
                )
            }

            // Step 2: Fetch from API in the background and update database
            android.util.Log.d("HomeViewModel", "Fetching stations from API: GET /api/v1/charging-stations")
            when (val result = stationRepository.getAllStations(page = 1, pageSize = 100)) {
                is Result.Success -> {
                    android.util.Log.d("HomeViewModel", "Successfully loaded ${result.data.size} stations from API")
                    // Step 3: Update UI with fresh data from API
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        nearbyStations = result.data,
                        error = null
                    )
                }
                is Result.Error -> {
                    android.util.Log.e("HomeViewModel", "Failed to load stations from API: ${result.message}")
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
            android.util.Log.d("HomeViewModel", "Loading upcoming reservations")
            val nic = authManager.getCurrentUserNic()

            if (nic == null) {
                android.util.Log.w("HomeViewModel", "Cannot load reservations - user NIC is null")
                _uiState.value = _uiState.value.copy(error = "User not authenticated")
                return@launch
            }

            android.util.Log.d("HomeViewModel", "Fetching reservations for NIC: $nic")
            when (val result = reservationRepository.getMyReservations(nic)) {
                is Result.Success -> {
                    android.util.Log.d("HomeViewModel", "Received ${result.data.size} reservations from API")

                    // Get the most recent upcoming reservation
                    val upcomingReservation = result.data
                        .filter { it.status in listOf("PENDING", "APPROVED") }
                        .sortedBy { it.startTime }
                        .firstOrNull()

                    android.util.Log.d("HomeViewModel", "Upcoming reservation: ${upcomingReservation?.id ?: "none"}")

                    _uiState.value = _uiState.value.copy(
                        upcomingReservation = upcomingReservation
                    )
                }
                is Result.Error -> {
                    android.util.Log.e("HomeViewModel", "Failed to load reservations: ${result.message}")
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

    /**
     * Refresh all data (stations and reservations)
     * Call this after login or when user pulls to refresh
     */
    fun refreshData() {
        android.util.Log.d("HomeViewModel", "Refreshing all data")
        loadAllStations()
        loadUpcomingReservations()
    }
}