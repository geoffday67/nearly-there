package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Destination::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
}
