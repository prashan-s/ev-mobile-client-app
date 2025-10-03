package lk.chargehere.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Station ID DTO for complex object responses
data class StationIdDto(
    val timestamp: Long,
    val creationTime: String
)

// Station DTOs - Handles both list (simple id) and detail (complex id) responses
data class StationDto(
    // ID can be either string (list) or complex object (detail)
    // Gson will try to deserialize 'id' field to whichever type matches
    val id: Any? = null,  // Can be String or StationIdDto

    val name: String = "",
    @SerializedName("stationCode")
    val stationCode: String? = null,

    // Location can be object (list) or fields spread out (detail)
    val location: LocationDto? = null,
    val address: String? = null,  // For detail endpoint

    // Type can be "AC"/"DC" (list) or "ac"/"dc" (detail)
    val type: String? = null,
    @SerializedName("stationType")
    val stationType: String? = null,

    @SerializedName("totalSlots")
    val totalSlots: Int? = null,
    @SerializedName("availableSlots")
    val availableSlots: Int? = null,
    @SerializedName("pricePerHour")
    val pricePerHour: Double? = null,

    val status: String? = null,
    @SerializedName("isActive")
    val isActive: Boolean? = null,

    // Schedule/operating hours
    val schedule: List<ScheduleDto>? = null,
    @SerializedName("operatingHours")
    val operatingHours: List<OperatingHourDto>? = null,

    @SerializedName("operatorId")
    val operatorId: String? = null,
    @SerializedName("createdBy")
    val createdBy: StationIdDto? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("createdDate")
    val createdDate: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("domainEvents")
    val domainEvents: List<Map<String, Any>>? = null
) {
    // Helper properties for unified access
    fun getStationId(): String = when (id) {
        is String -> id
        is Map<*, *> -> {
            // Handle complex object as Map
            (id["timestamp"] as? Double)?.toLong()?.toString() ?: ""
        }
        else -> ""
    }

    fun getStationLatitude(): Double = location?.latitude ?: 0.0

    fun getStationLongitude(): Double = location?.longitude ?: 0.0

    fun getStationAddress(): String = location?.address ?: address ?: ""

    fun getStationCity(): String = location?.city ?: ""

    fun getUnifiedStationType(): String = type ?: stationType ?: ""

    fun getStationIsActive(): Boolean = when {
        isActive != null -> isActive
        status == "active" -> true
        else -> false
    }

    // Backward compatibility properties
    val maxKw: Double
        get() = if (getUnifiedStationType().uppercase() == "DC") 150.0 else 50.0

    val isReservable: Boolean
        get() = getStationIsActive() && (totalSlots ?: 0) > 0

    val isAvailable: Boolean
        get() = (availableSlots ?: 0) > 0
}

// Location DTO for nested location data
data class LocationDto(
    val address: String? = null,
    val city: String? = null,
    val latitude: Double,
    val longitude: Double
)

// Schedule DTOs for station availability (list endpoint)
data class ScheduleDto(
    val dayOfWeek: String,
    val isOpen: Boolean,
    val timeSlots: List<TimeSlotDto>? = null
)

data class TimeSlotDto(
    val startTime: String,
    val endTime: String
)

// Operating hours DTO for station detail endpoint
data class OperatingHourDto(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val isOpen: Boolean
)

// Response wrapper for nearby stations
data class NearbyStationsResponse(
    val data: List<StationDto>
)

// Paginated response for getAllStations
data class PaginatedStationsResponse(
    val data: List<StationDto>,
    val pagination: PaginationDto
)

data class PaginationDto(
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrev: Boolean
) {
    // Backward compatibility properties
    val currentPage: Int get() = page
    val totalItems: Int get() = total
}

// Charging Station Statistics Response
data class ChargingStationStatsResponse(
    @SerializedName("totalStations")
    val totalStations: Int,
    @SerializedName("totalActiveStations")
    val totalActiveStations: Int,
    @SerializedName("totalDCFastChargingStations")
    val totalDCFastChargingStations: Int
)

// Busiest Station DTO for GetTopBusiestStations
data class BusiestStationDto(
    val stationId: String,
    val stationName: String,
    val utilizationPercentage: Double,
    val totalBookings: Int,
    val address: String?
)