/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.data_upload.dts_data

import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData

/**
 * the activity data type
 */
class ActivityDataDTS(activityData: ActivityData) {
    val id: Int = activityData.id
    val timeInMsecs: Long = activityData.timeInMsecs
    val type: Int = activityData.type
    val transitionType: Int = activityData.transitionType

    /**
     * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "ActivityDataDTS(id=$id, timeInMsecs=$timeInMsecs, type=$type, transitionType=$transitionType)"
    }


}
