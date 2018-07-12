package uk.co.sullenart.nearlythere.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "destination")
data class Destination(
        var name: String,
        var latitude: Double,
        var longitude: Double,
        var active: Boolean,
        @PrimaryKey(autoGenerate = true) var id: Long = 0
) : Serializable {
    override fun toString(): String = name
}
