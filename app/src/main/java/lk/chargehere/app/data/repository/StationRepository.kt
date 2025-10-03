package lk.chargehere.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import lk.chargehere.app.data.local.dao.StationDao
import lk.chargehere.app.data.mapper.*
import lk.chargehere.app.data.remote.api.StationApiService
import lk.chargehere.app.data.remote.dto.*
import lk.chargehere.app.domain.model.Station
import lk.chargehere.app.utils.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StationRepository @Inject constructor(
    private val stationApiService: StationApiService,
    private val stationDao: StationDao
) {
    
    // Get nearby stations using GetNearbyChargingStations
    suspend fun getNearbyStations(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<Station>> {
        return try {
            android.util.Log.d("StationRepository", "Fetching nearby stations from API - lat: $latitude, lng: $longitude, radius: $radiusKm")
            val response = stationApiService.getNearbyStations(latitude, longitude, radiusKm)

            if (response.isSuccessful) {
                val stationDtos = response.body()?.data ?: emptyList()
                android.util.Log.d("StationRepository", "Received ${stationDtos.size} nearby stations from API")

                val stations = stationDtos.map { it.toEntity() }
                stationDao.insertStations(stations)
                android.util.Log.d("StationRepository", "Saved ${stations.size} nearby stations to database")

                Result.Success(stations.map { it.toDomain() })
            } else {
                android.util.Log.w("StationRepository", "Nearby stations API call failed with code: ${response.code()}")
                // Return cached data if available, otherwise empty
                val cachedStations = stationDao.getAllStations().map { it.toDomain() }
                Result.Success(cachedStations)
            }
        } catch (e: Exception) {
            android.util.Log.e("StationRepository", "Network error while fetching nearby stations: ${e.message}", e)
            // Return cached data on network error
            val cachedStations = stationDao.getAllStations().map { it.toDomain() }
            Result.Success(cachedStations)
        }
    }
    
    // Get all stations with pagination and search using GetAllChargingStations
    suspend fun getAllStations(
        page: Int = 1,
        pageSize: Int = 50,
        search: String? = null
    ): Result<List<Station>> {
        return try {
            android.util.Log.d("StationRepository", "Fetching stations from API - page: $page, pageSize: $pageSize")
            val response = stationApiService.getAllStations(page, pageSize, search)

            if (response.isSuccessful) {
                val paginatedResponse = response.body()
                val stationEntities = paginatedResponse?.data?.map { it.toEntity() } ?: emptyList()

                android.util.Log.d("StationRepository", "Received ${stationEntities.size} stations from API")

                // Save to database for offline access
                if (stationEntities.isNotEmpty()) {
                    stationDao.insertStations(stationEntities)
                    android.util.Log.d("StationRepository", "Saved ${stationEntities.size} stations to database")
                }

                val stations = stationEntities.map { it.toDomain() }
                Result.Success(stations)
            } else {
                // Fallback to cached data
                val cachedStations = stationDao.getAllStations().map { it.toDomain() }
                Result.Success(cachedStations)
            }
        } catch (e: Exception) {
            // Fallback to cached data on network error
            val cachedStations = stationDao.getAllStations().map { it.toDomain() }
            Result.Success(cachedStations)
        }
    }
    
    // Get charging station by ID using GetChargingStationById
    suspend fun getChargingStationById(stationId: String): Result<Station> {
        return try {
            android.util.Log.d("StationRepository", "Fetching station detail from API for ID: $stationId")
            android.util.Log.d("StationRepository", "API Call: GET /api/v1/charging-stations/$stationId")

            val response = stationApiService.getChargingStationById(stationId)

            if (response.isSuccessful) {
                val stationDto = response.body()
                if (stationDto != null) {
                    android.util.Log.d("StationRepository", "Successfully loaded station detail from API")
                    android.util.Log.d("StationRepository", "Station has ${stationDto.operatingHours?.size ?: 0} operating hours")

                    // Save to database (entity doesn't include operating hours)
                    val stationEntity = stationDto.toEntity()
                    stationDao.insertStations(listOf(stationEntity))
                    android.util.Log.d("StationRepository", "Saved station to database")

                    // Convert DTO directly to domain to preserve operating hours
                    Result.Success(stationDto.toDomain())
                } else {
                    android.util.Log.w("StationRepository", "API returned successful but null station body")
                    Result.Error("Station not found")
                }
            } else {
                android.util.Log.e("StationRepository", "Failed to get station: ${response.code()}")
                Result.Error("Failed to get station: ${response.code()}")
            }
        } catch (e: Exception) {
            android.util.Log.e("StationRepository", "Network error while fetching station detail: ${e.message}", e)
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Get charging station statistics using GetChargingStationStats
    suspend fun getChargingStationStats(): Result<ChargingStationStatsResponse> {
        return try {
            val response = stationApiService.getChargingStationStats()
            
            if (response.isSuccessful) {
                val stats = response.body()
                if (stats != null) {
                    Result.Success(stats)
                } else {
                    Result.Error("Stats fetch failed: No data received")
                }
            } else {
                Result.Error("Stats fetch failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Get top busiest stations using GetTopBusiestStations
    suspend fun getTopBusiestStations(limit: Int = 5): Result<List<BusiestStationDto>> {
        return try {
            val response = stationApiService.getTopBusiestStations(limit)
            
            if (response.isSuccessful) {
                val stations = response.body()
                if (stations != null) {
                    Result.Success(stations)
                } else {
                    Result.Error("Failed to get busiest stations: No data received")
                }
            } else {
                Result.Error("Failed to get busiest stations: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Network error: ${e.message}")
        }
    }
    
    // Search stations (uses getAllStations with search parameter)
    suspend fun searchStations(query: String, limit: Int = 50): Result<List<Station>> {
        return getAllStations(1, limit, query)
    }
    
    fun observeAllStations(): Flow<List<Station>> {
        return stationDao.observeAllStations().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getCachedStations(): List<Station> {
        return stationDao.getAllStations().map { it.toDomain() }
    }
    
    suspend fun getReservableStations(): List<Station> {
        return stationDao.getReservableStations().map { it.toDomain() }
    }
}