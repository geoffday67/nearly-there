package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import uk.co.sullenart.nearlythere.data.DestinationDao
import uk.co.sullenart.nearlythere.data.StationDao
import javax.inject.Singleton

@Module
class MainModule(private val context: Context) {
    @Singleton
    @Provides
    fun provideRoomDatabase() = Room.databaseBuilder(context, AppDatabase::class.java, "nearly-there.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDestinationDao(database: AppDatabase) = database.destinationDao()

    @Provides
    fun provideDataManager(destinationDao: DestinationDao) = DataManager(destinationDao)

    @Provides
    fun provideStationDao(database: AppDatabase) = database.stationDao()

    @Provides
    fun provideStationManager(stationDao: StationDao) = StationManager(stationDao)

    @Provides
    @Singleton
    fun provideDestinationManager() = DestinationManager(context)

    @Provides
    @Singleton
    fun provideAlertManager() = AlertManager(context)
}