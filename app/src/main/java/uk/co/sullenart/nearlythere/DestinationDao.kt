package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import io.reactivex.Flowable

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destination")
    fun getAllDestinations(): Flowable<List<Destination>>

    @Insert(onConflict = REPLACE)
    fun addDestination(destination: Destination)

    @Query("DELETE FROM destination")
    fun clear()
}