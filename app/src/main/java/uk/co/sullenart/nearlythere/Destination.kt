package uk.co.sullenart.nearlythere

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "destination")
data class Destination(
        var name: String,
        @PrimaryKey(autoGenerate = true) var id: Long = 0
) {
    override fun toString(): String = name
}