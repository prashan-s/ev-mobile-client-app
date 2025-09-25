package lk.chargehere.app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey
    @ColumnInfo(name = "reservation_id")
    val reservationId: String,
    
    @ColumnInfo(name = "station_id")
    val stationId: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "status")
    val status: String, // PENDING, APPROVED, CANCELLED, COMPLETED
    
    @ColumnInfo(name = "start_timestamp")
    val startTimestamp: Long,
    
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    
    @ColumnInfo(name = "qr_payload")
    val qrPayload: String? = null,
    
    @ColumnInfo(name = "station_name")
    val stationName: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)