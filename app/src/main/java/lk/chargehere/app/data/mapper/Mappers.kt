package lk.chargehere.app.data.mapper

import lk.chargehere.app.data.local.entities.*
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.*
import java.security.MessageDigest

// User Mappers - Updated for new API structure
fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        userId = java.util.UUID.randomUUID().toString(), // Generate UUID for user ID
        nic = nic, // Store actual NIC
        nicHash = hashNIC(nic),
        name = "$firstName $lastName",
        email = email,
        role = "OWNER", // EVOwner role
        isActive = isActive
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = userId,
        nic = nic, // Use actual NIC field
        nicHash = nicHash,
        name = name,
        email = email,
        phone = null,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        userId = id, // Use id as userId (UUID)
        nic = nic, // Store actual NIC
        nicHash = nicHash,
        name = name,
        email = email,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Station Mappers - Updated for new API structure
fun StationDto.toEntity(explicitStationId: String? = null): StationEntity {
    return StationEntity(
        stationId = explicitStationId ?: getStationId(),
        name = name,
        address = getStationAddress(),
        latitude = getStationLatitude(),
        longitude = getStationLongitude(),
        maxKw = maxKw,
        chargerType = getUnifiedStationType().uppercase(),
        isReservable = isReservable,
        isAvailable = isAvailable,
        distanceMeters = null
    )
}

fun StationDto.toDomain(explicitStationId: String? = null): Station {
    return Station(
        id = explicitStationId ?: getStationId(),
        stationCode = stationCode,
        name = name,
        address = getStationAddress(),
        latitude = getStationLatitude(),
        longitude = getStationLongitude(),
        maxPower = maxKw,
        chargerType = getUnifiedStationType().uppercase(),
        isReservable = isReservable,
        isAvailable = isAvailable,
        distanceMeters = null,
        operatingHours = operatingHours?.map { it.toDomain() },
        createdAt = parseISOToTimestamp(createdDate ?: ""),
        updatedAt = System.currentTimeMillis()
    )
}

fun StationEntity.toDomain(): Station {
    return Station(
        id = stationId,
        stationCode = null, // Not stored in entity
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        maxPower = maxKw,
        chargerType = chargerType,
        isReservable = isReservable,
        isAvailable = isAvailable,
        distanceMeters = distanceMeters,
        operatingHours = null, // Operating hours not stored in local DB
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Station.toEntity(): StationEntity {
    return StationEntity(
        stationId = id,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        maxKw = maxPower,
        chargerType = chargerType,
        isReservable = isReservable,
        isAvailable = isAvailable,
        distanceMeters = distanceMeters,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Reservation Mappers - Updated for new API structure
fun ReservationDto.toEntity(): ReservationEntity {
    return ReservationEntity(
        reservationId = id ?: "",
        stationId = chargingStationId,
        userId = evOwnerNIC,
        status = status ?: "PENDING",
        startTimestamp = parseISOToTimestamp(reservationDateTime),
        durationMinutes = 60, // Default duration, can be updated
        qrPayload = null, // QR payload will be generated locally
        stationName = station?.name,
        createdAt = timestamp ?: System.currentTimeMillis()
    )
}

// BookingDetailDto Mappers - For detailed booking responses
fun BookingDetailDto.toEntity(): ReservationEntity {
    return ReservationEntity(
        reservationId = getIdString(),
        stationId = getStationIdString(),
        userId = evOwnerNIC ?: "",
        status = status ?: "pending",
        startTimestamp = parseISOToTimestamp(reservationDateTime ?: ""),
        durationMinutes = 60, // Default duration - TODO: Get from API when available
        qrPayload = qrCode ?: bookingNumber, // Use QR code if available, otherwise booking number
        stationName = getStationName(), // Use helper method to get station name
        createdAt = parseISOToTimestamp(createdDate ?: "")
    )
}

fun BookingDetailDto.toDomain(): Reservation {
    return Reservation(
        id = getIdString(),
        bookingNumber = bookingNumber,
        stationId = getStationIdString(),
        userId = evOwnerNIC ?: "",
        status = status ?: "pending",
        startTime = parseISOToTimestamp(reservationDateTime ?: ""),
        durationMinutes = 60, // Default duration - TODO: Get from API when available
        qrPayload = qrCode ?: bookingNumber ?: "",
        stationName = chargingStationName ?: "", // Use chargingStationName field
        createdAt = parseISOToTimestamp(createdDate ?: ""),
        updatedAt = parseISOToTimestamp(updatedDate ?: createdDate ?: "")
    )
}

fun ReservationEntity.toDomain(): Reservation {
    return Reservation(
        id = reservationId,
        bookingNumber = null, // Not stored in entity
        stationId = stationId,
        userId = userId,
        status = status,
        startTime = startTimestamp,
        durationMinutes = durationMinutes,
        qrPayload = qrPayload ?: "",
        stationName = stationName ?: "",
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Reservation.toEntity(): ReservationEntity {
    return ReservationEntity(
        reservationId = id,
        stationId = stationId,
        userId = userId,
        status = status,
        startTimestamp = startTime,
        durationMinutes = durationMinutes,
        qrPayload = qrPayload,
        stationName = stationName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Helper function to parse ISO date string to timestamp
private fun parseISOToTimestamp(isoString: String): Long {
    if (isoString.isBlank()) {
        return System.currentTimeMillis()
    }

    return try {
        java.time.Instant.parse(isoString).toEpochMilli()
    } catch (e: Exception) {
        // Try parsing without 'Z' suffix
        try {
            java.time.LocalDateTime.parse(isoString).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e2: Exception) {
            System.currentTimeMillis()
        }
    }
}

// Helper function to hash NIC
private fun hashNIC(nic: String): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(nic.toByteArray())
        hash.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        nic // Fallback to plain NIC if hashing fails
    }
}

// Operator Session Mappers
fun OperatorSessionEntity.toDomain(): OperatorSession {
    return OperatorSession(
        sessionId = sessionId,
        reservationId = reservationId,
        operatorId = operatorId,
        status = SessionStatus.valueOf(status),
        validationTimestamp = validationTimestamp,
        closeTimestamp = closeTimestamp,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun OperatorSession.toEntity(): OperatorSessionEntity {
    return OperatorSessionEntity(
        sessionId = sessionId,
        reservationId = reservationId,
        operatorId = operatorId,
        status = status.name,
        validationTimestamp = validationTimestamp,
        closeTimestamp = closeTimestamp,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Operating Hours Mappers
fun OperatingHourDto.toDomain(): OperatingHour {
    return OperatingHour(
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        isOpen = isOpen
    )
}