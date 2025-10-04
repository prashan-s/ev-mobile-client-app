package lk.chargehere.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Reservation DTOs - Updated to match new API spec
data class ReservationDto(
    val id: String? = null,
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String,
    @SerializedName("chargingStationId")
    val chargingStationId: String,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null,
    @SerializedName("creationTime")
    val creationTime: String? = null,
    @SerializedName("cancellationReason")
    val cancellationReason: String? = null,
    @SerializedName("bookingNumber")
    val bookingNumber: String? = null,
    // Nested objects might be included
    val station: StationDto? = null
)

// Detailed booking response from GetBookingById
data class BookingDetailDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("bookingNumber")
    val bookingNumber: String? = null,
    @SerializedName("effectiveBookingNumber")
    val effectiveBookingNumber: String? = null,
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String? = null,
    @SerializedName("chargingStationId")
    val chargingStationId: String? = null,
    @SerializedName("bookingDate")
    val bookingDate: String? = null,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String? = null,
    @SerializedName("physicalSlot")
    val physicalSlot: Int? = null,
    @SerializedName("effectivePhysicalSlot")
    val effectivePhysicalSlot: Int? = null,
    @SerializedName("endDateTime")
    val endDateTime: String? = null,
    @SerializedName("effectiveEndDateTime")
    val effectiveEndDateTime: String? = null,
    @SerializedName("durationMinutes")
    val durationMinutes: Int? = null,
    @SerializedName("effectiveDurationMinutes")
    val effectiveDurationMinutes: Int? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("qrCode")
    val qrCode: String? = null,
    @SerializedName("createdDate")
    val createdDate: String? = null,
    @SerializedName("updatedDate")
    val updatedDate: String? = null,
    @SerializedName("cancelledDate")
    val cancelledDate: String? = null,
    @SerializedName("cancelledBy")
    val cancelledBy: String? = null,
    @SerializedName("cancellationReason")
    val cancellationReason: String? = null,
    @SerializedName("domainEvents")
    val domainEvents: List<Map<String, Any>>? = null
) {
    // Helper to get ID as string
    fun getIdString(): String = id ?: ""

    // Helper to get station ID as string
    fun getStationIdString(): String = chargingStationId ?: ""

    // Helper to get timestamp for created date
    fun getTimestamp(): Long = System.currentTimeMillis()

    // Helper to resolve the effective booking number (fallback to bookingNumber if effectiveBookingNumber is null)
    fun resolveBookingNumber(): String = effectiveBookingNumber ?: bookingNumber ?: ""

    // Helper to resolve the effective physical slot
    fun resolvePhysicalSlot(): Int = effectivePhysicalSlot ?: physicalSlot ?: 1

    // Helper to resolve the effective duration in minutes
    fun resolveDurationMinutes(): Int = effectiveDurationMinutes ?: durationMinutes ?: 60

    // Helper to resolve the effective end date time
    fun resolveEndDateTime(): String = effectiveEndDateTime ?: endDateTime ?: ""
}

data class CreateBookingRequest(
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String,
    @SerializedName("chargingStationId")
    val chargingStationId: String,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String, // ISO 8601 format: "2025-01-01T12:00:00Z"
    @SerializedName("physicalSlot")
    val physicalSlot: Int = 1, // Slot number, must be > 0
    @SerializedName("durationMinutes")
    val durationMinutes: Int = 60 // Must be >= 15 minutes
)

// Response from POST /api/v1/bookings
// Returns the created booking detail (same structure as BookingDetailDto)
data class CreateBookingResponse(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("bookingNumber")
    val bookingNumber: String? = null,
    @SerializedName("effectiveBookingNumber")
    val effectiveBookingNumber: String? = null,
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String? = null,
    @SerializedName("chargingStationId")
    val chargingStationId: String? = null,
    @SerializedName("bookingDate")
    val bookingDate: String? = null,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("qrCode")
    val qrCode: String? = null,
    @SerializedName("createdDate")
    val createdDate: String? = null
) {
    // Helper to convert to BookingDetailDto for consistency
    fun toBookingDetailDto(): BookingDetailDto = BookingDetailDto(
        id = id,
        bookingNumber = bookingNumber,
        effectiveBookingNumber = effectiveBookingNumber,
        evOwnerNIC = evOwnerNIC,
        chargingStationId = chargingStationId,
        bookingDate = bookingDate,
        reservationDateTime = reservationDateTime,
        status = status,
        qrCode = qrCode,
        createdDate = createdDate,
        updatedDate = null,
        cancelledDate = null,
        cancelledBy = null,
        cancellationReason = null,
        domainEvents = null
    )
}

data class CancelBookingRequest(
    @SerializedName("cancellationReason")
    val cancellationReason: String?
)

// Paginated response for getAllBookings
data class PaginatedBookingsResponse(
    val data: List<ReservationDto>,
    val pagination: PaginationDto
)

// Keep old DTO for backward compatibility during migration
@Deprecated("Use CreateBookingRequest instead")
data class PlaceReservationRequest(
    @SerializedName("station_id")
    val stationId: String,
    @SerializedName("start_time")
    val startTime: String,
    val duration: Int
)