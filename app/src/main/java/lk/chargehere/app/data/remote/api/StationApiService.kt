package lk.chargehere.app.data.remote.api

import retrofit2.Response
import retrofit2.http.*
import lk.chargehere.app.data.remote.dto.*

interface StationApiService {
    
    // Get Nearby Charging Stations - GET /api/v1/charging-stations/nearby
    @GET("/api/v1/charging-stations/nearby")
    suspend fun getNearbyStations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusKm") radiusKm: Double
    ): Response<NearbyStationsResponse>
    
    // Get All Charging Stations - GET /api/v1/charging-stations
    @GET("/api/v1/charging-stations")
    suspend fun getAllStations(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("search") search: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("sortOrder") sortOrder: String = "asc"
    ): Response<PaginatedStationsResponse>
    
    // Get Charging Station by ID - GET /api/v1/charging-stations/{id}
    @GET("/api/v1/charging-stations/{id}")
    suspend fun getChargingStationById(@Path("id") stationId: String): Response<StationDto>
    
    // Get Charging Station Stats - GET /api/v1/dashboard/charging-station-stats
    @GET("/api/v1/dashboard/charging-station-stats")
    suspend fun getChargingStationStats(): Response<ChargingStationStatsResponse>
    
    // Get Top Busiest Stations - GET /api/v1/dashboard/top-busiest-stations
    @GET("/api/v1/dashboard/top-busiest-stations")
    suspend fun getTopBusiestStations(
        @Query("limit") limit: Int = 5
    ): Response<List<BusiestStationDto>>
}