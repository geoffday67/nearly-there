package uk.co.sullenart.nearlythere.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "station")
data class Station(
        @PrimaryKey(autoGenerate = true) var id: Long = 0,

        val name: String,
        val latitude: Double,
        val longitude: Double
)