/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.data_upload.dts_data

import it.torino.tracker.tracker.sensors.location_recognition.LocationData

class LocationDataDTS(locationData: LocationData) {
    val id: Int = locationData.id
    val timeInMsecs: Long = locationData.timeInMsecs
    val latitude: Double = locationData.latitude
    val longitude: Double = locationData.longitude
    val accuracy: Double = locationData.accuracy
    val altitude: Double = locationData.altitude

    /**
     * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "LocationDataDTS(id=$id, timeInMsecs=$timeInMsecs, latitude=$latitude, longitude=$longitude, accuracy=$accuracy, altitude=$altitude)"
    }


}
