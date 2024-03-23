/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker.sensors.activity_recognition

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.location.DetectedActivity
import it.torino.tracker.tracker.sensors.SensingData
import it.torino.tracker.utils.Utils.Companion.millisecondsToString

/**
 * the activity data type
 */
@Entity(indices = [Index("uploaded"), Index(value = ["timeInMsecs", "type"], unique = true)])
class ActivityData(override var timeInMsecs: Long, var type: Int, var transitionType: Int) : SensingData() {
    // IMPORTANT: if you add or change a field, also change the method copy fields
    @PrimaryKey(autoGenerate = true)
    @NonNull
    override var id: Int = 0
    override var uploaded = false


    companion object {
        fun getActivityTypeString(type: Int): String {
            return when (type) {
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                DetectedActivity.RUNNING -> "RUNNING"
                DetectedActivity.ON_BICYCLE -> "CYCLING"
                DetectedActivity.IN_VEHICLE -> "VEHICLE"
                DetectedActivity.ON_FOOT -> "ON FOOT"
                else -> "--"
            }
        }

        fun getTransitionType(type: Int): String {
            return ActivityRecognition.getTransitionTypeString(type)
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ActivityData
        return (timeInMsecs == that.timeInMsecs && type == that.type && transitionType == that.transitionType)
    }


    fun copy(): ActivityData {
        return ActivityData(timeInMsecs, type, transitionType)
    }

    override fun toString(): String {
        return millisecondsToString(timeInMsecs, "HH:mm:ss") + "  - " + getActivityTypeString(type) + " " +
                getTransitionType(transitionType)
    }

    /**
     * it copies an object field by field.
     *
     * @return
     */
    fun copyFields(otherActivity: ActivityData) {
        timeInMsecs = otherActivity.timeInMsecs
        type = otherActivity.type
        transitionType = otherActivity.transitionType
        timeZone = otherActivity.timeZone
        id = otherActivity.id
    }
}
