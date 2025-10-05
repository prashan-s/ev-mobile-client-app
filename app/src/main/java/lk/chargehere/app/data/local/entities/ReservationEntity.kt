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
    
    @ColumnInfo(name = "end_timestamp")
    val endTimestamp: Long? = null,

    @ColumnInfo(name = "physical_slot")
    val physicalSlot: Int? = null,

    @ColumnInfo(name = "booking_number")
    val bookingNumber: String? = null,

    @ColumnInfo(name = "qr_payload")
    val qrPayload: String? = null,
    
    @ColumnInfo(name = "station_name")
    val stationName: String? = null,

    @ColumnInfo(name = "station_code")
    val stationCode: String? = null,

    @ColumnInfo(name = "price_per_hour")
    val pricePerHour: Double? = null,

    @ColumnInfo(name = "station_type")
    val stationType: String? = null,

    @ColumnInfo(name = "station_city")
    val stationCity: String? = null,

    @ColumnInfo(name = "station_latitude")
    val stationLatitude: Double? = null,

    @ColumnInfo(name = "station_longitude")
    val stationLongitude: Double? = null,

    @ColumnInfo(name = "reservation_iso")
    val reservationIso: String? = null,

    @ColumnInfo(name = "booking_date_iso")
    val bookingDateIso: String? = null,

    @ColumnInfo(name = "ev_owner_name")
    val evOwnerName: String? = null,

    @ColumnInfo(name = "cancellation_reason")
    val cancellationReason: String? = null,

    @ColumnInfo(name = "cancelled_by")
    val cancelledBy: String? = null,

    @ColumnInfo(name = "cancelled_by_role")
    val cancelledByRole: String? = null,

    @ColumnInfo(name = "cancelled_at")
    val cancelledAt: Long? = null,

    @ColumnInfo(name = "can_be_modified")
    val canBeModified: Boolean? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)