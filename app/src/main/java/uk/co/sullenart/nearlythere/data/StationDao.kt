package uk.co.sullenart.nearlythere.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import uk.co.sullenart.nearlythere.model.Station
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

@Dao
abstract class StationDao {
    @Query("SELECT * FROM station ORDER BY name")
    abstract fun getAllStationsByName(): Flowable<List<Station>>

    @Insert(onConflict = REPLACE)
    abstract fun addStation(station: Station)

    @Query("DELETE FROM station")
    abstract fun clear()

    @Query("SELECT COUNT(*) FROM station")
    abstract fun count(): Int

    @Transaction
    open fun loadStations(input: InputStream) {
        BufferedReader(InputStreamReader(input)).use {
            while (true) {
                val parts = it.readLine()?.split(',') ?: break
                addStation(Station(name = parts[0], latitude = parts[1].toDouble(), longitude = parts[2].toDouble()))
            }
        }
    }
}