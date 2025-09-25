package lk.chargehere.app.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import lk.chargehere.app.data.local.entities.OperatorSessionEntity

@Dao
interface OperatorSessionDao {
    
    @Query("SELECT * FROM operator_sessions WHERE session_id = :sessionId")
    suspend fun getSessionById(sessionId: String): OperatorSessionEntity?
    
    @Query("SELECT * FROM operator_sessions WHERE reservation_id = :reservationId")
    suspend fun getSessionByReservation(reservationId: String): OperatorSessionEntity?
    
    @Query("SELECT * FROM operator_sessions WHERE operator_id = :operatorId ORDER BY created_at DESC")
    suspend fun getSessionsByOperator(operatorId: String): List<OperatorSessionEntity>
    
    @Query("SELECT * FROM operator_sessions WHERE operator_id = :operatorId ORDER BY created_at DESC")
    fun observeSessionsByOperator(operatorId: String): Flow<List<OperatorSessionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: OperatorSessionEntity)
    
    @Update
    suspend fun updateSession(session: OperatorSessionEntity)
    
    @Query("UPDATE operator_sessions SET status = :status, updated_at = :timestamp WHERE session_id = :sessionId")
    suspend fun updateSessionStatus(
        sessionId: String, 
        status: String, 
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Query("UPDATE operator_sessions SET close_timestamp = :timestamp, status = 'CLOSED', updated_at = :timestamp WHERE session_id = :sessionId")
    suspend fun closeSession(sessionId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE operator_sessions SET close_timestamp = :timestamp, status = 'CLOSED', updated_at = :timestamp WHERE reservation_id = :reservationId")
    suspend fun closeSessionByReservation(reservationId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM operator_sessions WHERE session_id = :sessionId")
    suspend fun deleteSession(sessionId: String)
}