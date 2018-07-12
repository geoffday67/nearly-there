package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Room
import android.content.Context
import com.huma.room_for_asset.RoomAsset
import dagger.Module
import dagger.Provides
import uk.co.sullenart.nearlythere.data.StationDao
import javax.inject.Singleton

@Module
class DataModule(private val context: Context) {
    @Singleton
    @Provides
    fun provideRoomDatabase() = Room.databaseBuilder(context, AppDatabase::class.java, "nearly-there.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDestinationDao(database: AppDatabase) = database.destinationDao()

    @Provides
    fun provideStationDao(database: AppDatabase) = database.stationDao()

    @Provides
    fun provideStationManager(stationDao: StationDao) = StationManager(stationDao)
}