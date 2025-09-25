package lk.chargehere.app.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import lk.chargehere.app.data.local.dao.*
import lk.chargehere.app.data.local.entities.*

@Database(
    entities = [
        UserEntity::class,
        StationEntity::class,
        ReservationEntity::class,
        OperatorSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ChargeHereDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun stationDao(): StationDao
    abstract fun reservationDao(): ReservationDao
    abstract fun operatorSessionDao(): OperatorSessionDao
    
    companion object {
        const val DATABASE_NAME = "chargehere_database"
        
        @Volatile
        private var INSTANCE: ChargeHereDatabase? = null
        
        fun getDatabase(context: Context): ChargeHereDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChargeHereDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}