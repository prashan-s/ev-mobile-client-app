package lk.chargehere.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import lk.chargehere.app.data.local.entities.ReservationEntity

@Dao
interface ReservationDao {
    
    @Query("SELECT * FROM reservations ORDER BY start_timestamp DESC")
    suspend fun getAllReservations(): List<ReservationEntity>
    
    @Query("SELECT * FROM reservations WHERE user_id = :userId ORDER BY start_timestamp DESC")
    suspend fun getReservationsByUser(userId: String): List<ReservationEntity>
    
    @Query("SELECT * FROM reservations WHERE user_id = :userId ORDER BY start_timestamp DESC")
    fun observeReservationsByUser(userId: String): Flow<List<ReservationEntity>>
    
    @Query("SELECT * FROM reservations WHERE reservation_id = :reservationId")
    suspend fun getReservationById(reservationId: String): ReservationEntity?
    
    @Query("SELECT * FROM reservations WHERE user_id = :userId AND status IN ('PENDING', 'APPROVED', 'CONFIRMED', 'IN_PROGRESS') ORDER BY start_timestamp ASC")
    suspend fun getUpcomingReservations(userId: String): List<ReservationEntity>
    
    @Query("SELECT * FROM reservations WHERE user_id = :userId AND status IN ('COMPLETED', 'CANCELLED') ORDER BY start_timestamp DESC")
    suspend fun getPastReservations(userId: String): List<ReservationEntity>
    
    @Query("SELECT COUNT(*) FROM reservations WHERE user_id = :userId AND status = 'PENDING'")
    suspend fun getPendingReservationsCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM reservations WHERE user_id = :userId AND status = 'APPROVED'")
    suspend fun getApprovedReservationsCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)
    
    @Update
    suspend fun updateReservation(reservation: ReservationEntity)
    
    @Query("UPDATE reservations SET status = :status, updated_at = :timestamp WHERE reservation_id = :reservationId")
    suspend fun updateReservationStatus(
        reservationId: String, 
        status: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("DELETE FROM reservations WHERE reservation_id = :reservationId")
    suspend fun deleteReservation(reservationId: String)
    
    @Query("DELETE FROM reservations WHERE user_id = :userId")
    suspend fun deleteUserReservations(userId: String)
}