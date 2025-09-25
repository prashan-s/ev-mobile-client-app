package lk.chargehere.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import lk.chargehere.app.data.local.entities.StationEntity

@Dao
interface StationDao {
    
    @Query("SELECT * FROM stations")
    suspend fun getAllStations(): List<StationEntity>
    
    @Query("SELECT * FROM stations")
    fun observeAllStations(): Flow<List<StationEntity>>
    
    @Query("SELECT * FROM stations WHERE station_id = :stationId")
    suspend fun getStationById(stationId: String): StationEntity?
    
    @Query("SELECT * FROM stations WHERE name LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%'")
    suspend fun searchStations(query: String): List<StationEntity>
    
    @Query("SELECT * FROM stations WHERE is_reservable = 1")
    suspend fun getReservableStations(): List<StationEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)
    
    @Update
    suspend fun updateStation(station: StationEntity)
    
    @Query("DELETE FROM stations")
    suspend fun deleteAllStations()
    
    @Query("DELETE FROM stations WHERE station_id = :stationId")
    suspend fun deleteStation(stationId: String)
}