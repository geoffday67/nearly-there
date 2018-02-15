package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataModule(private val context: Context) {
    @Singleton
    @Provides
    fun provideRoomDatabase() = Room.databaseBuilder(context, AppDatabase::class.java, "nearly-there")
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideDestinationDao(database: AppDatabase) = database.destinationDao()
}