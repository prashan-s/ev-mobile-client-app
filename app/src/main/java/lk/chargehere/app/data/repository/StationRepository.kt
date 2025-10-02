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
            val response = stationApiService.getNearbyStations(latitude, longitude, radiusKm)
            
            if (response.isSuccessful) {
                val stations = response.body()?.map { it.toEntity() } ?: emptyList()
                stationDao.insertStations(stations)
                Result.Success(stations.map { it.toDomain() })
            } else {
                // Return cached data if available, otherwise sample data
                val cachedStations = stationDao.getAllStations().map { it.toDomain() }
                if (cachedStations.isNotEmpty()) {
                    Result.Success(cachedStations)
                } else {
                    Result.Success(getSampleStations())
                }
            }
        } catch (e: Exception) {
            // Return cached data on network error, otherwise sample data
            val cachedStations = stationDao.getAllStations().map { it.toDomain() }
            if (cachedStations.isNotEmpty()) {
                Result.Success(cachedStations)
            } else {
                Result.Success(getSampleStations())
            }
        }
    }
    
    // Get all stations with pagination and search using GetAllChargingStations
    suspend fun getAllStations(
        page: Int = 1,
        pageSize: Int = 50,
        search: String? = null
    ): Result<List<Station>> {
        return try {
            val response = stationApiService.getAllStations(page, pageSize, search)

            if (response.isSuccessful) {
                val paginatedResponse = response.body()
                val stationEntities = paginatedResponse?.data?.map { it.toEntity() } ?: emptyList()

                // Save to database for offline access
                if (stationEntities.isNotEmpty()) {
                    stationDao.insertStations(stationEntities)
                }

                val stations = stationEntities.map { it.toDomain() }
                Result.Success(stations)
            } else {
                // Fallback to cached data or sample data
                val cachedStations = stationDao.getAllStations().map { it.toDomain() }
                if (cachedStations.isNotEmpty()) {
                    Result.Success(cachedStations)
                } else {
                    // Insert and return sample data for testing
                    val sampleStations = getSampleStations()
                    val sampleEntities = sampleStations.map { it.toEntity() }
                    stationDao.insertStations(sampleEntities)
                    Result.Success(sampleStations)
                }
            }
        } catch (e: Exception) {
            // Fallback to cached data or sample data on network error
            val cachedStations = stationDao.getAllStations().map { it.toDomain() }
            if (cachedStations.isNotEmpty()) {
                Result.Success(cachedStations)
            } else {
                // Insert and return sample data for testing
                val sampleStations = getSampleStations()
                val sampleEntities = sampleStations.map { it.toEntity() }
                stationDao.insertStations(sampleEntities)
                Result.Success(sampleStations)
            }
        }
    }
    
    // Get charging station by ID using GetChargingStationById
    suspend fun getChargingStationById(stationId: String): Result<Station> {
        return try {
            val response = stationApiService.getChargingStationById(stationId)
            
            if (response.isSuccessful) {
                val stationDto = response.body()
                if (stationDto != null) {
                    val stationEntity = stationDto.toEntity()
                    stationDao.insertStations(listOf(stationEntity))
                    Result.Success(stationEntity.toDomain())
                } else {
                    Result.Error("Station not found")
                }
            } else {
                Result.Error("Failed to get station: ${response.code()}")
            }
        } catch (e: Exception) {
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
    
    private fun getSampleStations(): List<Station> {
        return listOf(
            Station(
                id = "station_alpha",
                name = "Central Mall Charging Station",
                address = "123 Main Street, Downtown",
                latitude = 6.9271,
                longitude = 79.8612,
                maxPower = 150.0,
                isReservable = true,
                isAvailable = true
            ),
            Station(
                id = "station_beta", 
                name = "Office Complex Charger",
                address = "456 Business Ave, Business District",
                latitude = 6.9171,
                longitude = 79.8712,
                maxPower = 100.0,
                isReservable = true,
                isAvailable = false
            ),
            Station(
                id = "station_gamma",
                name = "Airport Parking Charger", 
                address = "Airport Road, Terminal 2",
                latitude = 6.9371,
                longitude = 79.8512,
                maxPower = 75.0,
                isReservable = true,
                isAvailable = true
            ),
            Station(
                id = "station_delta",
                name = "Shopping Center Fast Charge",
                address = "789 Commerce St, Mall District", 
                latitude = 6.9071,
                longitude = 79.8412,
                maxPower = 200.0,
                isReservable = false,
                isAvailable = true
            )
        )
    }
}