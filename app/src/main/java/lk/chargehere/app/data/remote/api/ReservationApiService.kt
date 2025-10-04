package lk.chargehere.app.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import lk.chargehere.app.data.remote.dto.*

interface ReservationApiService {
    
    // Create Booking - POST /api/v1/bookings
    @POST("/api/v1/bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<CreateBookingResponse>
    
    // Get Booking by ID - GET /api/v1/bookings/{id}
    @GET("/api/v1/bookings/{id}")
    suspend fun getBookingById(@Path("id") bookingId: String): Response<BookingDetailDto>
    
    // Get Bookings by EVOwner - GET /api/v1/bookings/evowner/{evOwnerNIC}
    @GET("/api/v1/bookings/evowner/{evOwnerNIC}")
    suspend fun getBookingsByEVOwner(@Path("evOwnerNIC") evOwnerNIC: String): Response<List<BookingDetailDto>>
    
    // Cancel Booking - POST /api/v1/bookings/{id}/cancel
    @POST("/api/v1/bookings/{id}/cancel")
    suspend fun cancelBooking(
        @Path("id") bookingId: String,
        @Body request: CancelBookingRequest
    ): Response<Unit>
    
    // Get All Bookings (with filters) - GET /api/v1/bookings
    @GET("/api/v1/bookings")
    suspend fun getAllBookings(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String = "desc"
    ): Response<PaginatedBookingsResponse>
}