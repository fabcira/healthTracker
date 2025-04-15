/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.retrieval.data

import com.google.android.gms.location.ActivityTransition
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.utils.Utils

class MobilityElementData(var timeInMSecs: Long) {
    companion object {
        const val INVALID_VALUE = -100
    }

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var location: LocationData? = null

    var steps: Int = INVALID_VALUE
    var cadence: Int = INVALID_VALUE

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var speed: Double = INVALID_VALUE.toDouble()
    var distance: Double = INVALID_VALUE.toDouble()
    var longitude: Double = INVALID_VALUE.toDouble()
    var latitude: Double = INVALID_VALUE.toDouble()

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var activityIn: Int = INVALID_VALUE
    var activityOut: Int = INVALID_VALUE

    // WARNING!! if you change the fields make sure that the function copyValidFields is updated
    var assignedActivity: Int = INVALID_VALUE

    override fun toString(): String {
        //return "MobilityElementData(timeInMSecs=${Utils.millisecondsToString(timeInMSecs, "HH:mm:ss")},  steps=$steps, cadence=$cadence, cadenceInLastNMinutes=$cadenceInLastNMinutes, speed=$speed, speedInLastNMinutes=$speedInLastNMinutes, activity=${ActivityData.getActivityTypeString(activity)}, activitiesInLastNMinutes=$activitiesInLastNMinutes, transitionType=${ActivityData.getTransitionType(transitionType)})"
        return "${
            Utils.millisecondsToString(
                timeInMSecs,
                "HH:mm:ss"
            )
        },  ${
            if (cadence == INVALID_VALUE) " , ," else "$steps, $cadence,"
        } ${
            if (activityOut == INVALID_VALUE) " ," else {
                " ${ActivityData.getActivityTypeString(activityOut)} " +
                        ActivityData.getTransitionType(ActivityTransition.ACTIVITY_TRANSITION_EXIT) + ", "
            }
        } ${
            if (activityIn == INVALID_VALUE) ", , " else {
                " ${ActivityData.getActivityTypeString(activityIn)} " +
                        ActivityData.getTransitionType(ActivityTransition.ACTIVITY_TRANSITION_ENTER) + ", "
            }
        } ${
            if (speed == INVALID_VALUE.toDouble()) ", , " else {
                "$speed, $distance,"
            }
        } ${
            if (assignedActivity == INVALID_VALUE) ", " else {
                "$ActivityData.getActivityTypeString(assignedActivity),"
            }
        }"
    }

    /**
     * it copies the valid fields from element if they are invalid in the current element     *
     * @param element
     */
    fun copyValidFields(element: MobilityElementData) {
        val INVALID_LONG = INVALID_VALUE.toLong()
        val INVALID_DOUBLE = INVALID_VALUE.toDouble()
        if (timeInMSecs == INVALID_LONG && element.timeInMSecs != INVALID_LONG)
            timeInMSecs = element.timeInMSecs
        if (steps == INVALID_VALUE && element.steps != INVALID_VALUE)
            steps = element.steps
        if (cadence == INVALID_VALUE && element.cadence != INVALID_VALUE)
            cadence = element.cadence
        if (speed == INVALID_DOUBLE && element.speed != INVALID_DOUBLE)
            speed = element.speed
        if (distance == INVALID_DOUBLE && element.distance != INVALID_DOUBLE)
            distance = element.distance
        if (latitude == INVALID_DOUBLE && element.latitude != INVALID_DOUBLE)
            latitude = element.latitude
        if (longitude == INVALID_DOUBLE && element.longitude != INVALID_DOUBLE)
            longitude = element.longitude
        if (activityIn == INVALID_VALUE && element.activityIn != INVALID_VALUE)
            activityIn = element.activityIn
        if (activityOut == INVALID_VALUE && element.activityOut != INVALID_VALUE)
            activityOut = element.activityOut
        if (location == null && element.location != null)
            location = element.location
    }
}