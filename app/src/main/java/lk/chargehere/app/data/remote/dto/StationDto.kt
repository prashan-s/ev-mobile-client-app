package lk.chargehere.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// MongoDB ObjectId structure
data class ObjectIdDto(
    @SerializedName("timestamp")
    val timestamp: Int? = null,
    @SerializedName("creationTime")
    val creationTime: String? = null
) {
    // Helper to convert to string ID
    fun toIdString(): String = creationTime ?: timestamp?.toString() ?: ""
}

// Station DTOs - Handles both list (simple id) and detail (complex id) responses
data class StationDto(
    // ID can be either string (list) or complex object (detail)
    @SerializedName("id")
    val id: Any? = null,  // Can be String or ObjectIdDto

    val name: String = "",
    @SerializedName("stationCode")
    val stationCode: String? = null,

    // Location is always an object
    val location: LocationDto? = null,
    val address: String? = null,

    // Type handling
    @SerializedName("stationType")
    val stationType: String? = null,

    @SerializedName("totalSlots")
    val totalSlots: Int? = null,
    @SerializedName("availableSlots")
    val availableSlots: Int? = null,
    @SerializedName("pricePerHour")
    val pricePerHour: Double? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null,

    // Operating hours
    @SerializedName("operatingHours")
    val operatingHours: List<OperatingHourDto>? = null,

    @SerializedName("createdBy")
    val createdBy: Any? = null, // Can be ObjectIdDto

    @SerializedName("createdDate")
    val createdDate: String? = null,

    @SerializedName("domainEvents")
    val domainEvents: List<Map<String, Any>>? = null
) {
    // Helper properties for unified access
    fun getStationId(): String {
        return try {
            when (id) {
                is String -> id
                is Map<*, *> -> {
                    // Handle ObjectId as Map - extract timestamp or creationTime
                    val timestamp = id["timestamp"] as? Number
                    val creationTime = id["creationTime"] as? String
                    
                    // Prefer using a hash of creationTime or timestamp for uniqueness
                    creationTime?.let { 
                        // Extract just the hex portion if it looks like a MongoDB ObjectId
                        if (it.length == 24 && it.matches(Regex("[a-f0-9]{24}"))) {
                            it
                        } else {
                            // Otherwise use timestamp
                            timestamp?.toString() ?: ""
                        }
                    } ?: timestamp?.toString() ?: ""
                }
                else -> {
                    android.util.Log.w("StationDto", "Unable to extract station ID from: $id")
                    ""
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("StationDto", "Error extracting station ID", e)
            ""
        }
    }

    fun getStationLatitude(): Double = location?.latitude ?: 0.0

    fun getStationLongitude(): Double = location?.longitude ?: 0.0

    fun getStationAddress(): String = address ?: location?.address ?: ""

    fun getStationCity(): String = location?.city ?: ""

    fun getUnifiedStationType(): String = stationType ?: "ac"

    fun getStationIsActive(): Boolean = isActive ?: false

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