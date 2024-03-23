/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker.sensors.location_recognition

import android.location.Location
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import it.torino.tracker.tracker.sensors.SensingData
import it.torino.tracker.utils.Utils
import kotlin.math.roundToInt

@Entity(indices = [Index("uploaded"), Index(value = arrayOf("timeInMsecs"), unique = true)])
class LocationData(
    override var timeInMsecs: Long, var latitude: Double, var longitude: Double, var accuracy: Double,
    var altitude: Double
) : SensingData() {
    @Ignore
    var distance: Double = 0.0

    /**
     * the goodness of a location in the simplification process
     */
    @Ignore
    var sed: Double = 0.0

    @PrimaryKey(autoGenerate = true)
    @NonNull
    override var id: Int = 0

    @Ignore
    var speed: Double = 0.0

    @Ignore
    var locationsSupportingCentroid: MutableList<LocationData> = mutableListOf()

    constructor(location: Location) : this(
        location.time, location.latitude,
        location.longitude, location.accuracy.toDouble(), location.altitude
    )

    override fun toString(): String {
        return Utils.millisecondsToString(timeInMsecs, "HH:mm:ss") +
                "- (" + latitude +
                "" + longitude +
                ") alt:" + altitude.roundToInt() +
                ", acc:" + accuracy.roundToInt()
    }

    fun copy(): LocationData {
        return LocationData(
            timeInMsecs,
            latitude,
            longitude,
            accuracy,
            altitude
        )
    }
}
