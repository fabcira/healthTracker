/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.utils.Globals

/**
 * This class is used to automatically build the payload for the server api
 */
class TripsRequest(trip: TripData) {

    @SerializedName("_id")
    var userId = Globals.EMPTY

    @SerializedName("trips")
    val trips = ArrayList<TripRequest>()

    class TripRequest(dbTrip: TripData) {

        @SerializedName("id")
        val id: Int = dbTrip.id

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbTrip.getStartTime(dbTrip.chart)

        @SerializedName("startTime")
        val startTime: Long = dbTrip.getStartTime(dbTrip.chart)

        @SerializedName("endTime")
        val endTime: Long = dbTrip.getEndTime(dbTrip.chart)

        @SerializedName("activityType")
        val activityType: Int = dbTrip.activityType

        @SerializedName("radiusInMeters")
        val radiusInMeters: Int = dbTrip.radiusInMeters

        @SerializedName("distanceInMeters")
        val distanceInMeters: Int = dbTrip.distanceInMeters

        @SerializedName("steps")
        val steps: Int = dbTrip.steps
    }
}