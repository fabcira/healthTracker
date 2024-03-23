/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.tracker.sensors.symptoms

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import it.torino.tracker.tracker.sensors.SensingData
import it.torino.tracker.utils.Utils

@Entity(indices = [Index("timeInMsecs")])
class SymptomsData(
        override var timeInMsecs: Long,
        var type: Int
    ) : SensingData() {
    @PrimaryKey(autoGenerate = true)
    override var id = 0

    override fun toString(): String {
        return Utils.millisecondsToString(timeInMsecs, "HH:mm:ss")
            .toString() + ": " + type
    }
}
