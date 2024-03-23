/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.tracker.sensors.step_counting

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import it.torino.tracker.tracker.sensors.SensingData
import it.torino.tracker.utils.Utils

@Entity(indices = [Index("timeInMsecs")])
open class StepsData(override var timeInMsecs: Long, val steps: Int) : SensingData() {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    override var id: Int = 0

    @Ignore
    var cadence: Int = 0

    override fun toString(): String {
        return Utils.millisecondsToString(timeInMsecs, "HH:mm:ss") +
                ", steps=" + steps + " (" + cadence + ")"
    }

    fun copy(): StepsData {
        return StepsData(timeInMsecs, steps)
    }

    /**
     * given a start time for the current step data, it returns the cadence
     *
     * @param a step element immediately preceding this in time
     * @return the cadence
     */
    fun computeCadence(previousStepsData: StepsData): Int {
        val stepdiff = steps - previousStepsData.steps
        var secdiff = (timeInMsecs - previousStepsData.timeInMsecs) / 1000.0
        // if the tw consecutive step counts are far away in time,
        // the time is not representative for cadence, so we just take the last few seconds
        // as time reference
        if (secdiff > StepCounter.WAITING_TIME_IN_MSECS * 1.5)
            secdiff = StepCounter.WAITING_TIME_IN_MSECS * 1.5
        cadence = (stepdiff / secdiff * 60.0).toInt()
        return cadence
    }


}
