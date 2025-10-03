package lk.chargehere.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lk.chargehere.app.data.local.dao.ReservationDao
import lk.chargehere.app.data.mapper.*
import lk.chargehere.app.data.remote.api.ReservationApiService
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.Reservation
import lk.chargehere.app.domain.model.ReservationStatus
import lk.chargehere.app.utils.Result
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationApiService: ReservationApiService,
    private val reservationDao: ReservationDao
) {
    
    // Create booking using CreateBooking
    suspend fun createBooking(
        evOwnerNIC: String,
        chargingStationId: String,
        reservationDateTime: String
    ): Result<CreateBookingResponse> {
        return try {
            android.util.Log.d("ReservationRepository", "Creating booking for NIC: $evOwnerNIC, Station: $chargingStationId")
            android.util.Log.d("ReservationRepository", "Reservation time: $reservationDateTime")
            android.util.Log.d("ReservationRepository", "API Call: POST /api/v1/bookings")

            val request = CreateBookingRequest(evOwnerNIC, chargingStationId, reservationDateTime)
            val response = reservationApiService.createBooking(request)

            if (response.isSuccessful) {
                val bookingResponse = response.body()
                if (bookingResponse != null) {
                    android.util.Log.d("ReservationRepository", "Booking created successfully!")
                    android.util.Log.d("ReservationRepository", "Booking Number: ${bookingResponse.bookingNumber}")
                    android.util.Log.d("ReservationRepository", "Status: ${bookingResponse.status}")
                    android.util.Log.d("ReservationRepository", "QR Code: ${bookingResponse.qrCode}")

                    // Refresh reservations after creating a new one
                    getBookingsByEVOwner(evOwnerNIC)
                    Result.Success(bookingResponse)
                } else {
                    android.util.Log.e("ReservationRepository", "Booking failed: No response data received")
                    Result.Error("Booking failed: No response data received")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                android.util.Log.e("ReservationRepository", "Booking failed: ${response.code()} - $errorBody")
                Result.Error("Booking failed: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepository", "Network error while creating booking: ${e.message}", e)
            Result.Error("Network error: ${e.message}")
        }
    }

    // Convenience method that converts timestamp to ISO string
    suspend fun placeReservation(
        evOwnerNIC: String,
        stationId: String,
        startTime: Long
    ): Result<CreateBookingResponse> {
        val isoDateTime = Instant.ofEpochMilli(startTime)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        android.util.Log.d("ReservationRepository", "Converting timestamp $startTime to ISO: $isoDateTime")
        return createBooking(evOwnerNIC, stationId, isoDateTime)
    }
    
    // Get booking by ID using GetBookingById
    suspend fun getBookingById(bookingId: String): Result<BookingDetailDto> {
        return try {
            android.util.Log.d("ReservationRepository", "Fetching booking detail from API for ID: $bookingId")
            android.util.Log.d("ReservationRepository", "API Call: GET /api/v1/bookings/$bookingId")

            val response = reservationApiService.getBookingById(bookingId)

            if (response.isSuccessful) {
                val booking = response.body()
                if (booking != null) {
                    android.util.Log.d("ReservationRepository", "Successfully loaded booking detail from API")
                    Result.Success(booking)
                } else {
                    android.util.Log.w("ReservationRepository", "API returned successful but null booking body")
                    Result.Error("Booking not found")
                }
            } else {
                android.util.Log.e("ReservationRepository", "Failed to get booking: ${response.code()}")
                Result.Error("Failed to get booking: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepository", "Network error while fetching booking detail: ${e.message}", e)
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Get bookings by EVOwner using GetBookingsByEVOwner
    suspend fun getBookingsByEVOwner(evOwnerNIC: String): Result<List<Reservation>> {
        return try {
            android.util.Log.d("ReservationRepository", "Fetching bookings from API for NIC: $evOwnerNIC")
            android.util.Log.d("ReservationRepository", "API Call: GET /api/v1/bookings/evowner/$evOwnerNIC")

            val response = reservationApiService.getBookingsByEVOwner(evOwnerNIC)

            if (response.isSuccessful) {
                val bookingDetails = response.body() ?: emptyList()
                android.util.Log.d("ReservationRepository", "Successfully loaded ${bookingDetails.size} bookings from API")

                // Convert BookingDetailDto to ReservationEntity
                val reservations = bookingDetails.map { it.toEntity() }

                reservations.forEach { reservationDao.insertReservation(it) }
                android.util.Log.d("ReservationRepository", "Saved ${reservations.size} reservations to database")

                Result.Success(reservations.map { it.toDomain() })
            } else {
                android.util.Log.w("ReservationRepository", "API call failed with code: ${response.code()}, falling back to cached data")
                val errorBody = response.errorBody()?.string()
                android.util.Log.w("ReservationRepository", "Error body: $errorBody")
                // Return cached data if available
                val cachedReservations = reservationDao.getAllReservations().map { it.toDomain() }
                android.util.Log.d("ReservationRepository", "Returning ${cachedReservations.size} cached reservations")
                Result.Success(cachedReservations)
            }
        } catch (e: Exception) {
            android.util.Log.e("ReservationRepository", "Network error: ${e.message}", e)
            e.printStackTrace()
            // Return cached data on network error
            val cachedReservations = reservationDao.getAllReservations().map { it.toDomain() }
            android.util.Log.d("ReservationRepository", "Returning ${cachedReservations.size} cached reservations after network error")
            Result.Success(cachedReservations)
        }
    }
    
    // Convenience method for getting current user's reservations
    // Note: Caller must provide NIC - typically from AuthManager.getCurrentUserNic()
    suspend fun getMyReservations(evOwnerNIC: String): Result<List<Reservation>> {
        return getBookingsByEVOwner(evOwnerNIC)
    }
    
    // Cancel booking using CancelBooking
    suspend fun cancelReservation(
        reservationId: String,
        cancellationReason: String? = null
    ): Result<Unit> {
        return try {
            val request = CancelBookingRequest(cancellationReason)
            val response = reservationApiService.cancelBooking(reservationId, request)
            
            if (response.isSuccessful) {
                reservationDao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED.name)
                Result.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()
                Result.Error("Cancellation failed: ${response.code()} - $errorBody")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Get all bookings with filters
    suspend fun getAllBookings(
        page: Int = 1,
        pageSize: Int = 10,
        search: String? = null,
        status: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ): Result<List<Reservation>> {
        return try {
            val response = reservationApiService.getAllBookings(
                page, pageSize, search, status, fromDate, toDate
            )
            
            if (response.isSuccessful) {
                val paginatedResponse = response.body()
                val reservations = paginatedResponse?.data?.map { it.toEntity().toDomain() } ?: emptyList()
                Result.Success(reservations)
            } else {
                Result.Error("Failed to get bookings: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    fun observeUserReservations(userId: String): Flow<List<Reservation>> {
        return reservationDao.observeReservationsByUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getUpcomingReservations(userId: String): List<Reservation> {
        return reservationDao.getUpcomingReservations(userId).map { it.toDomain() }
    }
    
    suspend fun getPastReservations(userId: String): List<Reservation> {
        return reservationDao.getPastReservations(userId).map { it.toDomain() }
    }
    
    suspend fun getPendingReservationsCount(userId: String): Int {
        return reservationDao.getPendingReservationsCount(userId)
    }
    
    suspend fun getApprovedReservationsCount(userId: String): Int {
        return reservationDao.getApprovedReservationsCount(userId)
    }

}