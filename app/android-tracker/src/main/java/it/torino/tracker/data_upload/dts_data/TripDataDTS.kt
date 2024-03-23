/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.dts_data

import it.torino.tracker.retrieval.data.TripData

class TripDataDTS(tripData: TripData) {
    // TODO: add when inserting into db
    //  val id : Int = tripData.id
    var startTime: Long = tripData.chart[tripData.startTime].timeInMSecs
    var endTime: Long = tripData.chart[tripData.endTime].timeInMSecs
    var activityType: Int = tripData.activityType
    var radiusInMeters: Int = tripData.radiusInMeters
    var steps: Int = tripData.steps
    var distanceInMeters: Int = tripData.distanceInMeters

    /**
     * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "TripDataDTS(startTime=$startTime, endTime=$endTime, activityType=$activityType, radiusInMeters=$radiusInMeters, steps=$steps, distanceInMeters=$distanceInMeters)"
    }


}