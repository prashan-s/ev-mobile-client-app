package lk.chargehere.app.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey
    @ColumnInfo(name = "station_id")
    val stationId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "address")
    val address: String? = null,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "max_kw")
    val maxKw: Double,

    @ColumnInfo(name = "charger_type")
    val chargerType: String = "AC",

    @ColumnInfo(name = "is_reservable")
    val isReservable: Boolean = true,

    @ColumnInfo(name = "is_available")
    val isAvailable: Boolean = true,

    @ColumnInfo(name = "distance_meters")
    val distanceMeters: Double? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)