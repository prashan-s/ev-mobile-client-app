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
    val id: String? = null,
    @SerializedName("bookingNumber")
    val bookingNumber: String? = null,
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String? = null,
    @SerializedName("chargingStationId")
    val chargingStationId: String? = null,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String? = null,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName("timestamp")
    val timestamp: Long? = null,
    @SerializedName("creationTime")
    val creationTime: String? = null,
    @SerializedName("cancellationReason")
    val cancellationReason: String? = null,
    // Nested station details
    val station: StationDto? = null
)

data class CreateBookingRequest(
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String,
    @SerializedName("chargingStationId")
    val chargingStationId: String,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String // ISO 8601 format: "2024-01-01T12:00:00Z"
)

data class CreateBookingResponse(
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("creationTime")
    val creationTime: String
)

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