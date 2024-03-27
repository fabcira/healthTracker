package it.torino.tracker.tracker.sensors.accelerometer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AccelerometerData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    var x: Float,
    var y: Float,
    var z: Float
)
