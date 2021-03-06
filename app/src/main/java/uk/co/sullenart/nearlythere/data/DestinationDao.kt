package uk.co.sullenart.nearlythere.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import uk.co.sullenart.nearlythere.model.Destination

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destination")
    fun getAllDestinations(): Flowable<List<Destination>>

    @Insert(onConflict = REPLACE)
    fun addDestination(destination: Destination)

    @Query("DELETE FROM destination")
    fun clear()

    @Query("DELETE FROM destination WHERE id = :id")
    fun delete(id: Long)

    @Query("UPDATE destination SET active = :active WHERE id = :id")
    fun setActive(id: Long, active: Boolean)
}