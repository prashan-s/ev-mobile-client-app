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
    val error: String? = null,
    val isCancellationInProgress: Boolean = false,
    val cancellationSuccess: Boolean = false
)

@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    init {
        android.util.Log.d("ReservationsViewModel", "ReservationsViewModel initialized - loading reservations")
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            android.util.Log.d("ReservationsViewModel", "Loading reservations from API")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val nic = authManager.getCurrentUserNic()
            if (nic == null) {
                android.util.Log.w("ReservationsViewModel", "Cannot load reservations - user NIC is null")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )
                return@launch
            }

            android.util.Log.d("ReservationsViewModel", "Fetching reservations for NIC: $nic")
            android.util.Log.d("ReservationsViewModel", "API Call: GET /api/v1/bookings/evowner/$nic")

            when (val result = reservationRepository.getMyReservations(nic)) {
                is Result.Success -> {
                    val allReservations = result.data
                    android.util.Log.d("ReservationsViewModel", "Successfully loaded ${allReservations.size} reservations from API")

                    // Log reservation details for debugging
                    allReservations.forEach { reservation ->
                        android.util.Log.d("ReservationsViewModel", "Reservation: id=${reservation.id}, status='${reservation.status}', startTime=${reservation.startTime}, now=${System.currentTimeMillis()}")
                    }

                    val now = System.currentTimeMillis()
                    val activeStatuses = setOf("PENDING", "APPROVED", "CONFIRMED", "IN_PROGRESS")

                    val upcoming = allReservations.filter { reservation ->
                        val statusUpper = reservation.status.uppercase()
                        val isFutureReservation = reservation.startTime > now
                        val isInProgress = statusUpper == "IN_PROGRESS"
                        val isActiveStatus = statusUpper in activeStatuses
                        android.util.Log.d(
                            "ReservationsViewModel",
                            "Filter upcoming: id=${reservation.id}, status='${reservation.status}' (upper='$statusUpper'), isFuture=$isFutureReservation, isActive=$isActiveStatus, isInProgress=$isInProgress"
                        )
                        (isInProgress || isFutureReservation) && isActiveStatus
                    }.sortedByDescending { it.startTime }

                    val upcomingIds = upcoming.map { it.id }.toSet()

                    val past = allReservations.filter { reservation ->
                        val statusUpper = reservation.status.uppercase()
                        when {
                            reservation.id in upcomingIds -> false
                            statusUpper in listOf("COMPLETED", "CANCELLED") -> true
                            statusUpper !in activeStatuses -> true
                            reservation.startTime <= now -> true
                            else -> false
                        }
                    }.sortedByDescending { it.startTime }

                    android.util.Log.d("ReservationsViewModel", "Categorized: ${upcoming.size} upcoming, ${past.size} past")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        upcomingReservations = upcoming,
                        pastReservations = past
                    )
                }
                is Result.Error -> {
                    android.util.Log.e("ReservationsViewModel", "Failed to load reservations: ${result.message}")
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
            _uiState.value = _uiState.value.copy(
                isCancellationInProgress = true,
                error = null,
                cancellationSuccess = false
            )
            
            val cancellationReason = reason ?: "Cancelled by the User."

            when (val result = reservationRepository.cancelReservation(reservationId, cancellationReason)) {
                is Result.Success -> {
                    android.util.Log.d("ReservationsViewModel", "Successfully cancelled reservation: $reservationId")
                    _uiState.value = _uiState.value.copy(
                        cancellationSuccess = true,
                        isCancellationInProgress = false
                    )
                    // Reload reservations to reflect the change
                    loadReservations()
                }
                is Result.Error -> {
                    android.util.Log.e("ReservationsViewModel", "Failed to cancel reservation: ${result.message}")
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isCancellationInProgress = false,
                        cancellationSuccess = false
                    )
                }
                is Result.Loading -> {
                    _uiState.value = _uiState.value.copy(isCancellationInProgress = true)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}