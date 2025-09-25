package lk.chargehere.app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operator_sessions")
data class OperatorSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    
    @ColumnInfo(name = "reservation_id")
    val reservationId: String,
    
    @ColumnInfo(name = "operator_id")
    val operatorId: String,
    
    @ColumnInfo(name = "status")
    val status: String, // VALIDATED, IN_PROGRESS, CLOSED
    
    @ColumnInfo(name = "validation_timestamp")
    val validationTimestamp: Long? = null,
    
    @ColumnInfo(name = "close_timestamp")
    val closeTimestamp: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)