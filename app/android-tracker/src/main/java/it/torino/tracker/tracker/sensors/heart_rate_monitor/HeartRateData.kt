/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */
package it.torino.tracker.tracker.sensors.heart_rate_monitor

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import it.torino.tracker.tracker.sensors.SensingData
import it.torino.tracker.utils.Utils

@Entity(indices = [Index("timeInMsecs")])
class HeartRateData(
    override var timeInMsecs: Long,
    var heartRate: Int,
    var accuracy: Int
) : SensingData() {
    @PrimaryKey(autoGenerate = true)
    override var id = 0

    override fun toString(): String {
        return Utils.millisecondsToString(timeInMsecs, "HH:mm:ss")
            .toString() + ": " + heartRate + " (" + accuracy + ")"
    }

}