package lk.chargehere.app.data.mapper

import lk.chargehere.app.data.local.entities.*
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.*
import java.security.MessageDigest

// User Mappers - Updated for new API structure
fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        userId = nic, // Use NIC as user ID
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
        nic = userId, // NIC is stored as userId
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
        userId = nic, // Use NIC as userId
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
fun StationDto.toEntity(): StationEntity {
    return StationEntity(
        stationId = getStationId(),
        name = name,
        address = getStationAddress(),
        latitude = getStationLatitude(),
        longitude = getStationLongitude(),
        maxKw = maxKw,
        isReservable = isReservable,
        isAvailable = isAvailable,
        distanceMeters = null
    )
}

fun StationEntity.toDomain(): Station {
    return Station(
        id = stationId,
        name = name,
        address = address,
        latitude = latitude,
        longitude = longitude,
        maxPower = maxKw,
        isReservable = isReservable,
        isAvailable = isAvailable,
        distanceMeters = distanceMeters,
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
        reservationId = id,
        stationId = chargingStationId,
        userId = evOwnerNIC,
        status = status,
        startTimestamp = parseISOToTimestamp(reservationDateTime),
        durationMinutes = 60, // Default duration
        qrPayload = bookingNumber, // Use booking number as QR payload
        stationName = null, // Will be populated from station data if needed
        createdAt = timestamp
    )
}

fun BookingDetailDto.toDomain(): Reservation {
    return Reservation(
        id = id,
        stationId = chargingStationId,
        userId = evOwnerNIC,
        status = status,
        startTime = parseISOToTimestamp(reservationDateTime),
        durationMinutes = 60,
        qrPayload = bookingNumber,
        stationName = "", // Will be populated separately if needed
        createdAt = timestamp,
        updatedAt = System.currentTimeMillis()
    )
}

fun ReservationEntity.toDomain(): Reservation {
    return Reservation(
        id = reservationId,
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