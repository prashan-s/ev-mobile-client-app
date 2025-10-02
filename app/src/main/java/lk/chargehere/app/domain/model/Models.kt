package lk.chargehere.app.domain.model

data class User(
    val id: String,
    val nic: String, // NIC for API calls
    val nicHash: String = "", // Kept for backward compatibility
    val name: String,
    val email: String,
    val phone: String? = null,
    val role: String = "USER", // Changed to String to match API
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class UserRole {
    OWNER,
    OPERATOR,
    USER
}

data class Station(
    val id: String, // Changed from stationId to id
    val name: String,
    val address: String? = null,
    val latitude: Double,
    val longitude: Double,
    val maxPower: Double, // Changed from maxKw to maxPower
    val chargerType: String = "AC", // AC or DC from API
    val isReservable: Boolean = true,
    val isAvailable: Boolean = true,
    val distanceMeters: Double? = null,
    val operatingHours: List<OperatingHour>? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OperatingHour(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val isOpen: Boolean
)

data class Reservation(
    val id: String, // Changed from reservationId to id
    val stationId: String,
    val userId: String,
    val status: String, // Changed to String to match API
    val startTime: Long, // Changed from startTimestamp to startTime
    val durationMinutes: Int,
    val qrPayload: String? = null,
    val stationName: String = "", // Made non-nullable with default
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ReservationStatus {
    PENDING,
    APPROVED,
    CANCELLED,
    COMPLETED
}

data class OperatorSession(
    val sessionId: String,
    val reservationId: String,
    val operatorId: String,
    val status: SessionStatus,
    val validationTimestamp: Long? = null,
    val closeTimestamp: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class SessionStatus {
    VALIDATED,
    IN_PROGRESS,
    CLOSED
}

data class SessionDetail(
    val sessionId: String,
    val reservation: Reservation,
    val user: User,
    val station: Station
)