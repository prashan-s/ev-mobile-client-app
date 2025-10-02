package lk.chargehere.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import lk.chargehere.app.data.local.ChargeHereDatabase
import lk.chargehere.app.data.local.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ChargeHereDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ChargeHereDatabase::class.java,
            ChargeHereDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // Recreate DB on schema change (for development)
        .build()
    }
    
    @Provides
    fun provideUserDao(database: ChargeHereDatabase): UserDao {
        return database.userDao()
    }
    
    @Provides
    fun provideStationDao(database: ChargeHereDatabase): StationDao {
        return database.stationDao()
    }
    
    @Provides
    fun provideReservationDao(database: ChargeHereDatabase): ReservationDao {
        return database.reservationDao()
    }
    
    @Provides
    fun provideOperatorSessionDao(database: ChargeHereDatabase): OperatorSessionDao {
        return database.operatorSessionDao()
    }
}