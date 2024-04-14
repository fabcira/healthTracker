/*
 *   Copyright (c) 2023. 
 *   This code was developed by Fabio Ciravegna, The University of Sheffield. 
 *   All rights reserved. 
 *   No part of this code can be used without the explicit written permission by the author
 */

package uk.ac.shef.tracker.core.serialization

import com.google.gson.annotations.SerializedName
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import it.torino.tracker.utils.Globals

/**
 * This class is used to automatically build the payload for the server api
 */
class StepsRequest {

    @SerializedName("_id")
    var userId = Globals.EMPTY

    @SerializedName("steps")
    val steps = ArrayList<StepRequest>()

    class StepRequest(dbStep: StepsData) {

        @SerializedName("id")
        val id: Int = dbStep.id

        @SerializedName("timeInMsecs")
        val timeInMsecs: Long = dbStep.timeInMsecs

        @SerializedName("steps")
        val steps: Int = dbStep.steps
    }
}