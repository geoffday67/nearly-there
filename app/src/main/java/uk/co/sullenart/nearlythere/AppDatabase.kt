package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import uk.co.sullenart.nearlythere.data.DestinationDao
import uk.co.sullenart.nearlythere.data.StationDao
import uk.co.sullenart.nearlythere.model.Destination
import uk.co.sullenart.nearlythere.model.Station

@Database(entities = [Destination::class, Station::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
    abstract fun stationDao(): StationDao
}
