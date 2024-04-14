/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.utils.Globals

/**
 * This class is used to automatically build the payload for the server api
 */
class LocationsRequest {

    @SerializedName("_id")
    var userId = Globals.EMPTY

    @SerializedName("locations")
    val locations = ArrayList<LocationRequest>()

    class LocationRequest(dbLocation: LocationData) {

        @SerializedName("id")
        val id: Int = dbLocation.id

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbLocation.timeInMsecs

        @SerializedName("latitude")
        val latitude: Double = dbLocation.latitude

        @SerializedName("longitude")
        val longitude: Double = dbLocation.longitude

        @SerializedName("altitude")
        val altitude: Double = dbLocation.altitude

        @SerializedName("accuracy")
        val accuracy: Double = dbLocation.accuracy
    }
}