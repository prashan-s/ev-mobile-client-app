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

data class TimeSlot(
    val id: String,
    val displayTime: String,
    val timestampMillis: Long,
    val offsetMinutes: Int
)

data class ReservationCreationUiState(
    val isLoading: Boolean = false,
    val station: Station? = null,
    val availableTimeSlots: List<TimeSlot> = emptyList(),
    val selectedTimeSlot: TimeSlot? = null,
    val selectedDateMillis: Long? = null,
    val selectedTimeHour: Int = 0,
    val selectedTimeMinute: Int = 0,
    val selectedDurationMinutes: Int = 60,
    val selectedPhysicalSlot: Int = 1,
    val currentStep: Int = 1,
    val totalSteps: Int = 4,
    val estimatedCost: Double = 0.0,
    val bookingResponse: CreateBookingResponse? = null,
    val error: String? = null,
    val validationError: String? = null,
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
     * Load station details for booking and generate time slots
     */
    fun loadStation(stationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = stationRepository.getChargingStationById(stationId)) {
                is Result.Success -> {
                    val timeSlots = generateTimeSlots()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        station = result.data,
                        availableTimeSlots = timeSlots,
                        // Auto-select first time slot
                        selectedTimeSlot = timeSlots.firstOrNull()
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
     * Generate available time slots (Now, 30 min, 1 hour, 2 hours, 4 hours, 6 hours)
     */
    private fun generateTimeSlots(): List<TimeSlot> {
        val now = System.currentTimeMillis()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        return listOf(
            TimeSlot(
                id = "now",
                displayTime = "Now",
                timestampMillis = now,
                offsetMinutes = 0
            ),
            TimeSlot(
                id = "30min",
                displayTime = Instant.ofEpochMilli(now + 30 * 60 * 1000L)
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter),
                timestampMillis = now + 30 * 60 * 1000L,
                offsetMinutes = 30
            ),
            TimeSlot(
                id = "1hour",
                displayTime = Instant.ofEpochMilli(now + 60 * 60 * 1000L)
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter),
                timestampMillis = now + 60 * 60 * 1000L,
                offsetMinutes = 60
            ),
            TimeSlot(
                id = "2hours",
                displayTime = Instant.ofEpochMilli(now + 2 * 60 * 60 * 1000L)
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter),
                timestampMillis = now + 2 * 60 * 60 * 1000L,
                offsetMinutes = 120
            ),
            TimeSlot(
                id = "4hours",
                displayTime = Instant.ofEpochMilli(now + 4 * 60 * 60 * 1000L)
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter),
                timestampMillis = now + 4 * 60 * 60 * 1000L,
                offsetMinutes = 240
            ),
            TimeSlot(
                id = "6hours",
                displayTime = Instant.ofEpochMilli(now + 6 * 60 * 60 * 1000L)
                    .atZone(ZoneId.systemDefault())
                    .format(timeFormatter),
                timestampMillis = now + 6 * 60 * 60 * 1000L,
                offsetMinutes = 360
            )
        )
    }

    /**
     * Select a time slot
     */
    fun selectTimeSlot(timeSlot: TimeSlot) {
        _uiState.value = _uiState.value.copy(selectedTimeSlot = timeSlot)
    }

    /**
     * Select date for reservation
     */
    fun selectDate(dateMillis: Long) {
        val validationResult = validateDate(dateMillis)
        if (validationResult != null) {
            _uiState.value = _uiState.value.copy(validationError = validationResult)
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedDateMillis = dateMillis,
            validationError = null
        )
    }

    /**
     * Select time for reservation
     */
    fun selectTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            selectedTimeHour = hour,
            selectedTimeMinute = minute
        )
        updateEstimatedCost()
    }

    /**
     * Select duration
     */
    fun selectDuration(durationMinutes: Int) {
        if (durationMinutes < 15) {
            _uiState.value = _uiState.value.copy(
                validationError = "Duration must be at least 15 minutes"
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            selectedDurationMinutes = durationMinutes,
            validationError = null
        )
        updateEstimatedCost()
    }

    /**
     * Select physical slot
     */
    fun selectPhysicalSlot(slot: Int) {
        _uiState.value = _uiState.value.copy(selectedPhysicalSlot = slot)
    }

    /**
     * Validate selected date is within 7 days and in the future
     */
    private fun validateDate(dateMillis: Long): String? {
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7 * 24 * 60 * 60 * 1000L)

        return when {
            dateMillis < now -> "Please select a future date"
            dateMillis > sevenDaysFromNow -> "Booking must be within 7 days"
            else -> null
        }
    }

    /**
     * Update estimated cost based on duration and station price
     */
    private fun updateEstimatedCost() {
        val station = _uiState.value.station ?: return
        val durationMinutes = _uiState.value.selectedDurationMinutes
        val pricePerHour = station.maxPower // Using maxPower as price placeholder
        val estimatedCost = (durationMinutes / 60.0) * pricePerHour

        _uiState.value = _uiState.value.copy(estimatedCost = estimatedCost)
    }

    /**
     * Navigate to next step
     */
    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep < _uiState.value.totalSteps) {
            _uiState.value = _uiState.value.copy(currentStep = currentStep + 1)
        }
    }

    /**
     * Navigate to previous step
     */
    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep > 1) {
            _uiState.value = _uiState.value.copy(currentStep = currentStep - 1)
        }
    }

    /**
     * Check if current step is complete and can proceed
     */
    fun canProceedToNextStep(): Boolean {
        return when (_uiState.value.currentStep) {
            1 -> _uiState.value.selectedDateMillis != null
            2 -> _uiState.value.selectedTimeHour >= 0 && _uiState.value.selectedTimeMinute >= 0
            3 -> _uiState.value.selectedDurationMinutes >= 15
            4 -> _uiState.value.selectedPhysicalSlot > 0
            else -> false
        }
    }

    /**
     * Create booking using CreateBooking endpoint with all parameters
     * @param stationId The charging station ID
     */
    fun createBookingWithAllDetails(stationId: String) {
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

            // Validate all required fields
            val dateMillis = _uiState.value.selectedDateMillis
            if (dateMillis == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please select a date"
                )
                return@launch
            }

            // Combine date and time
            val selectedDate = Instant.ofEpochMilli(dateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            val dateTime = selectedDate.atTime(
                _uiState.value.selectedTimeHour,
                _uiState.value.selectedTimeMinute
            ).atZone(ZoneId.systemDefault())

            // Convert to ISO 8601 format
            val isoDateTime = dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            when (val result = reservationRepository.createBooking(
                evOwnerNIC = nic,
                chargingStationId = stationId,
                reservationDateTime = isoDateTime,
                durationMinutes = _uiState.value.selectedDurationMinutes,
                physicalSlot = _uiState.value.selectedPhysicalSlot
            )) {
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

            when (val result = reservationRepository.createBooking(
                evOwnerNIC = nic,
                chargingStationId = stationId,
                reservationDateTime = isoDateTime,
                durationMinutes = _uiState.value.selectedDurationMinutes,
                physicalSlot = _uiState.value.selectedPhysicalSlot
            )) {
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
     * Create booking with selected time slot
     */
    fun createBookingWithSelectedSlot(stationId: String) {
        val selectedSlot = _uiState.value.selectedTimeSlot
        if (selectedSlot != null) {
            createBooking(stationId, selectedSlot.timestampMillis)
        } else {
            _uiState.value = _uiState.value.copy(
                error = "Please select a time slot"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null, validationError = null)
    }

    fun resetBooking() {
        _uiState.value = ReservationCreationUiState()
    }
}
