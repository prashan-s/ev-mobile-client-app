package lk.chargehere.app.data.mapper

import lk.chargehere.app.data.local.entities.*
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.*
import java.security.MessageDigest
import java.util.UUID

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
    val resolvedReservationId = getReservationId().ifBlank {
        listOfNotNull(
            bookingNumber?.takeIf { it.isNotBlank() },
            "$chargingStationId-$reservationDateTime"
        ).firstOrNull { it.isNotBlank() } ?: UUID.randomUUID().toString()
    }

    val startTimestamp = parseISOToTimestamp(reservationDateTime)
    val endTimestamp = parseISOToTimestampOrNull(endDateTime)
        ?: durationMinutes?.let { startTimestamp + it * 60_000L }
    val resolvedDuration = durationMinutes
        ?: endTimestamp?.let { ((it - startTimestamp) / 60_000L).toInt().coerceAtLeast(1) }
        ?: 60

    val statusValue = (status ?: "PENDING").uppercase()

    return ReservationEntity(
        reservationId = resolvedReservationId,
        stationId = chargingStationId,
        userId = evOwnerNIC,
        status = statusValue,
        startTimestamp = startTimestamp,
        durationMinutes = resolvedDuration,
        endTimestamp = endTimestamp,
        physicalSlot = physicalSlot,
        bookingNumber = bookingNumber,
        qrPayload = null, // QR payload will be generated locally
        stationName = station?.name,
        stationCode = station?.stationCode,
        pricePerHour = station?.pricePerHour,
        stationType = station?.stationType?.uppercase(),
        stationCity = station?.location?.city,
        stationLatitude = station?.location?.latitude,
        stationLongitude = station?.location?.longitude,
        reservationIso = reservationDateTime,
        bookingDateIso = creationTime,
        evOwnerName = null,
        cancellationReason = cancellationReason,
        cancelledBy = null,
        cancelledByRole = null,
        cancelledAt = null,
        canBeModified = null,
        createdAt = timestamp ?: System.currentTimeMillis()
    )
}

// BookingDetailDto Mappers - For detailed booking responses
fun BookingDetailDto.toEntity(): ReservationEntity {
    val resolvedReservationId = getIdString().ifBlank {
        listOfNotNull(
            bookingNumber?.takeIf { it.isNotBlank() },
            getStationIdString().takeIf { it.isNotBlank() }?.let { "$it-${reservationDateTime ?: createdDate}" }
        ).firstOrNull { it.isNotBlank() } ?: UUID.randomUUID().toString()
    }

    val startTimestamp = parseISOToTimestamp(reservationDateTime ?: "")
    val endTimestamp = parseISOToTimestampOrNull(endDateTime)
        ?: durationMinutes?.let { startTimestamp + it * 60_000L }
    val resolvedDuration = durationMinutes
        ?: endTimestamp?.let { ((it - startTimestamp) / 60_000L).toInt().coerceAtLeast(1) }
        ?: 60

    val stationSummary = chargingStation

    val statusValue = (status ?: "pending").uppercase()

    return ReservationEntity(
        reservationId = resolvedReservationId,
        stationId = getStationIdString(),
        userId = evOwnerNIC ?: "",
        status = statusValue,
        startTimestamp = startTimestamp,
        durationMinutes = resolvedDuration,
        endTimestamp = endTimestamp,
        physicalSlot = physicalSlot,
        bookingNumber = bookingNumber,
        qrPayload = qrCode ?: bookingNumber, // Use QR code if available, otherwise booking number
        stationName = getStationName(), // Use helper method to get station name
        stationCode = stationSummary?.stationCode ?: stationCode,
        pricePerHour = stationSummary?.pricePerHour,
        stationType = stationSummary?.stationType?.uppercase(),
        stationCity = stationSummary?.location?.city,
        stationLatitude = stationSummary?.location?.latitude,
        stationLongitude = stationSummary?.location?.longitude,
        reservationIso = reservationDateTime,
        bookingDateIso = bookingDate,
        evOwnerName = getOwnerFullName().takeIf { it.isNotBlank() },
        cancellationReason = cancellationReason,
        cancelledBy = listOfNotNull(cancelledByName, cancelledBy).firstOrNull { it.isNotBlank() },
        cancelledByRole = cancelledByRole,
        cancelledAt = parseISOToTimestampOrNull(cancelledDate),
        canBeModified = canBeModified,
        createdAt = parseISOToTimestamp(createdDate ?: bookingDate ?: ""),
        updatedAt = parseISOToTimestamp(updatedDate ?: cancelledDate ?: createdDate ?: "")
    )
}

fun BookingDetailDto.toDomain(): Reservation {
    val resolvedReservationId = getIdString().ifBlank {
        listOfNotNull(
            bookingNumber?.takeIf { it.isNotBlank() },
            getStationIdString().takeIf { it.isNotBlank() }?.let { "$it-${reservationDateTime ?: createdDate}" }
        ).firstOrNull { it.isNotBlank() } ?: UUID.randomUUID().toString()
    }

    val startTimestamp = parseISOToTimestamp(reservationDateTime ?: "")
    val endTimestamp = parseISOToTimestampOrNull(endDateTime)
        ?: durationMinutes?.let { startTimestamp + it * 60_000L }
    val resolvedDuration = durationMinutes
        ?: endTimestamp?.let { ((it - startTimestamp) / 60_000L).toInt().coerceAtLeast(1) }
        ?: 60

    val stationSummary = chargingStation

    val statusValue = (status ?: "pending").uppercase()

    return Reservation(
        id = resolvedReservationId,
        bookingNumber = bookingNumber,
        stationId = getStationIdString(),
        userId = evOwnerNIC ?: "",
        status = statusValue,
        startTime = startTimestamp,
        durationMinutes = resolvedDuration,
        endTime = endTimestamp,
        physicalSlot = physicalSlot,
        qrPayload = qrCode ?: bookingNumber,
        stationName = getStationName(),
        stationCode = stationSummary?.stationCode ?: stationCode,
        pricePerHour = stationSummary?.pricePerHour,
        stationType = stationSummary?.stationType?.uppercase(),
        stationCity = stationSummary?.location?.city,
        stationLatitude = stationSummary?.location?.latitude,
        stationLongitude = stationSummary?.location?.longitude,
        reservationIso = reservationDateTime,
        bookingDateIso = bookingDate ?: createdDate,
        evOwnerName = getOwnerFullName().takeIf { it.isNotBlank() },
        cancellationReason = cancellationReason,
        cancelledBy = listOfNotNull(cancelledByName, cancelledBy).firstOrNull { it.isNotBlank() },
        cancelledByRole = cancelledByRole,
        cancelledAt = parseISOToTimestampOrNull(cancelledDate),
        canBeModified = canBeModified ?: true,
        createdAt = parseISOToTimestamp(createdDate ?: bookingDate ?: ""),
        updatedAt = parseISOToTimestamp(updatedDate ?: cancelledDate ?: createdDate ?: "")
    )
}

fun ReservationEntity.toDomain(): Reservation {
    return Reservation(
        id = reservationId,
        bookingNumber = bookingNumber,
        stationId = stationId,
        userId = userId,
        status = status,
        startTime = startTimestamp,
        durationMinutes = durationMinutes,
        endTime = endTimestamp,
        physicalSlot = physicalSlot,
        qrPayload = qrPayload ?: bookingNumber,
        stationName = stationName ?: "",
        stationCode = stationCode,
        pricePerHour = pricePerHour,
        stationType = stationType,
        stationCity = stationCity,
        stationLatitude = stationLatitude,
        stationLongitude = stationLongitude,
        reservationIso = reservationIso,
        bookingDateIso = bookingDateIso,
        evOwnerName = evOwnerName,
        cancellationReason = cancellationReason,
        cancelledBy = cancelledBy,
        cancelledByRole = cancelledByRole,
        cancelledAt = cancelledAt,
        canBeModified = canBeModified ?: true,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Reservation.toEntity(): ReservationEntity {
    return ReservationEntity(
        reservationId = id,
        stationId = stationId,
        userId = userId,
        status = status.uppercase(),
        startTimestamp = startTime,
        durationMinutes = durationMinutes,
        endTimestamp = endTime,
        physicalSlot = physicalSlot,
        bookingNumber = bookingNumber,
        qrPayload = qrPayload,
        stationName = stationName,
        stationCode = stationCode,
        pricePerHour = pricePerHour,
        stationType = stationType,
        stationCity = stationCity,
        stationLatitude = stationLatitude,
        stationLongitude = stationLongitude,
        reservationIso = reservationIso,
        bookingDateIso = bookingDateIso,
        evOwnerName = evOwnerName,
        cancellationReason = cancellationReason,
        cancelledBy = cancelledBy,
        cancelledByRole = cancelledByRole,
        cancelledAt = cancelledAt,
        canBeModified = canBeModified,
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

private fun parseISOToTimestampOrNull(isoString: String?): Long? {
    if (isoString.isNullOrBlank()) {
        return null
    }

    return try {
        java.time.Instant.parse(isoString).toEpochMilli()
    } catch (e: Exception) {
        try {
            java.time.LocalDateTime.parse(isoString).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e2: Exception) {
            null
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