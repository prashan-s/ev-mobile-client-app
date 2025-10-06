package lk.chargehere.app.data.remote.dto

import android.util.Log
import com.google.gson.annotations.SerializedName

// Reservation DTOs - Updated to match new API spec
data class ReservationDto(
    @SerializedName("id")
    val id: Any? = null,
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
    @SerializedName("endDateTime")
    val endDateTime: String? = null,
    @SerializedName("durationMinutes")
    val durationMinutes: Int? = null,
    @SerializedName("physicalSlot")
    val physicalSlot: Int? = null,
    // Nested objects might be included
    val station: StationDto? = null
)

fun ReservationDto.getReservationId(): String = id.extractReservationId("ReservationDto")

// Detailed booking response from GetBookingById
data class BookingDetailDto(
    @SerializedName("id")
    val id: Any? = null,
    @SerializedName("bookingNumber")
    val bookingNumber: String? = null,
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String? = null,
    @SerializedName("evOwnerFirstName")
    val evOwnerFirstName: String? = null,
    @SerializedName("evOwnerLastName")
    val evOwnerLastName: String? = null,
    @SerializedName("evOwnerFullName")
    val evOwnerFullName: String? = null,
    @SerializedName("chargingStationId")
    val chargingStationId: String? = null,
    @SerializedName("stationCode")
    val stationCode: String? = null,
    @SerializedName("chargingStationName")
    val chargingStationName: String? = null,
    @SerializedName("bookingDate")
    val bookingDate: String? = null,
    @SerializedName("reservationDateTime")
    val reservationDateTime: String? = null,
    @SerializedName("endDateTime")
    val endDateTime: String? = null,
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
    @SerializedName("cancelledByName")
    val cancelledByName: String? = null,
    @SerializedName("cancelledByRole")
    val cancelledByRole: String? = null,
    @SerializedName("cancellationReason")
    val cancellationReason: String? = null,
    @SerializedName("canBeModified")
    val canBeModified: Boolean? = null,
    @SerializedName("durationMinutes")
    val durationMinutes: Int? = null,
    @SerializedName("physicalSlot")
    val physicalSlot: Int? = null,
    @SerializedName("chargingStation")
    val chargingStation: BookingStationSummaryDto? = null
) {
    // Helper to get ID as string
    fun getIdString(): String = id.extractReservationId("BookingDetailDto")

    // Helper to get station ID as string
    fun getStationIdString(): String = chargingStationId ?: ""

    // Helper to get timestamp for created date
    fun getTimestamp(): Long = System.currentTimeMillis()

    // Helper to get station name
    fun getStationName(): String = chargingStationName ?: chargingStation?.name.orEmpty()

    // Helper to get owner full name
    fun getOwnerFullName(): String = evOwnerFullName ?: "$evOwnerFirstName $evOwnerLastName".trim()
}

data class BookingStationSummaryDto(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("pricePerHour")
    val pricePerHour: Double? = null,
    @SerializedName("stationCode")
    val stationCode: String? = null,
    @SerializedName("stationType")
    val stationType: String? = null,
    @SerializedName("location")
    val location: BookingStationLocationDto? = null
)

data class BookingStationLocationDto(
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("latitude")
    val latitude: Double? = null,
    @SerializedName("longitude")
    val longitude: Double? = null
)

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
    @SerializedName("evOwnerNIC")
    val evOwnerNIC: String? = null,
    @SerializedName("evOwnerFirstName")
    val evOwnerFirstName: String? = null,
    @SerializedName("evOwnerLastName")
    val evOwnerLastName: String? = null,
    @SerializedName("evOwnerFullName")
    val evOwnerFullName: String? = null,
    @SerializedName("chargingStationId")
    val chargingStationId: String? = null,
    @SerializedName("stationCode")
    val stationCode: String? = null,
    @SerializedName("chargingStationName")
    val chargingStationName: String? = null,
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
        evOwnerNIC = evOwnerNIC,
        evOwnerFirstName = evOwnerFirstName,
        evOwnerLastName = evOwnerLastName,
        evOwnerFullName = evOwnerFullName,
        chargingStationId = chargingStationId,
        stationCode = stationCode,
        chargingStationName = chargingStationName,
        bookingDate = bookingDate,
        reservationDateTime = reservationDateTime,
        status = status,
        qrCode = qrCode,
        createdDate = createdDate,
        updatedDate = null,
        cancelledDate = null,
        cancelledBy = null,
        cancelledByName = null,
        cancelledByRole = null,
        cancellationReason = null,
        canBeModified = null
    )
}

data class CancelBookingRequest(
    @SerializedName("cancellationReason")
    val cancellationReason: String?
)

private fun Any?.extractReservationId(tag: String): String {
    return when (this) {
        is String -> this
        is ObjectIdDto -> this.creationTime ?: this.timestamp?.toString().orEmpty()
        is Map<*, *> -> {
            val map = this
            val directOid = (map["${'$'}oid"] as? String)
                ?: (map["oid"] as? String)
                ?: ((map["_id"] as? Map<*, *>)?.get("${'$'}oid") as? String)
                ?: (map["value"] as? String)
                ?: (map["id"] as? String)
                ?: (map["creationTime"] as? String)
                ?: (map["timestamp"] as? Number)?.toLong()?.toString()

            if (!directOid.isNullOrBlank()) {
                directOid
            } else {
                Log.w(tag, "Unable to parse reservation id from map: $this")
                ""
            }
        }
        else -> {
            Log.w(tag, "Unable to parse reservation id from: $this")
            ""
        }
    }
}

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