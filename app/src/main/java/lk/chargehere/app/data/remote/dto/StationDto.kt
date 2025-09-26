package lk.chargehere.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Station DTOs - Updated to match new API
data class StationDto(
    val id: StationIdDto? = null, // For getById response - contains timestamp and creationTime
    @SerializedName("id") 
    val idString: String? = null, // For getAllStations response - simple string ID
    val name: String,
    val address: String? = null, // For getById response
    val location: LocationDto? = null, // For getAllStations response
    @SerializedName("stationType")
    val stationType: String? = null, // For getById response: "ac" or "dc"
    val type: String? = null, // For getAllStations response: "AC" or "DC"
    @SerializedName("totalSlots")
    val totalSlots: Int? = null,
    @SerializedName("availableSlots")
    val availableSlots: Int? = null,
    @SerializedName("pricePerHour")
    val pricePerHour: Double? = null,
    val status: String? = null, // "active" etc
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    @SerializedName("createdBy")
    val createdBy: StationIdDto? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("createdDate")
    val createdDate: String? = null,
    @SerializedName("stationCode")
    val stationCode: String? = null,
    @SerializedName("operatorId")
    val operatorId: String? = null,
    val schedule: List<ScheduleDto>? = null
) {
    // Helper properties for unified access
    fun getStationId(): String = idString ?: id?.let { "${it.timestamp}" } ?: ""
    
    fun getStationLatitude(): Double = location?.latitude ?: 0.0
    
    fun getStationLongitude(): Double = location?.longitude ?: 0.0
    
    fun getStationAddress(): String = location?.address ?: address ?: ""
    
    fun getUnifiedStationType(): String = type ?: stationType ?: ""
    
    fun getStationIsActive(): Boolean = when {
        isActive != null -> isActive
        status != null -> status == "active"
        else -> true
    }
    
    // Backward compatibility properties
    val maxKw: Double
        get() = if (getUnifiedStationType().uppercase() == "DC") 150.0 else 50.0
    
    val isReservable: Boolean
        get() = getStationIsActive() && (totalSlots ?: 0) > 0
    
    val isAvailable: Boolean
        get() = getStationIsActive()
}

// Station ID DTO for getById response
data class StationIdDto(
    val timestamp: Long,
    val creationTime: String
)

// Location DTO for nested location data
data class LocationDto(
    val address: String? = null,
    val city: String? = null,
    val latitude: Double,
    val longitude: Double
)

// Schedule DTOs for station availability
data class ScheduleDto(
    val dayOfWeek: String? = null, // For getAllStations response
    val isOpen: Boolean? = null,
    val timeSlots: List<TimeSlotDto>? = null,
    
    // For getById response - individual slots
    val slotId: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val isAvailable: Boolean? = null,
    val bookedBy: String? = null
)

data class TimeSlotDto(
    val startTime: String,
    val endTime: String
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