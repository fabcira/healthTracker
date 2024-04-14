/*
 *  
Copyright (c) 2023. 
This code was developed by Fabio Ciravegna, The University of Sheffield. 
All rights reserved. 
No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.utils.Globals

/**
 * This class is used to automatically build the payload for the server api
 */
class ActivitiesRequest {

    @SerializedName("_id")
    var userId = Globals.EMPTY

    @SerializedName("activities")
    val activities = ArrayList<ActivityRequest>()

    class ActivityRequest(dbActivity: ActivityData) {

        @SerializedName("id")
        val id: Int = dbActivity.id

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbActivity.timeInMsecs

        @SerializedName("type")
        val type: Int = dbActivity.type

        @SerializedName("transitionType")
        val transitionType: Int = dbActivity.transitionType
    }
}