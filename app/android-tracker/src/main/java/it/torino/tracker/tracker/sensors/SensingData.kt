/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.tracker.sensors

import it.torino.tracker.utils.Utils

abstract class SensingData : Comparable<SensingData?> {
    open var id = 0
    open var timeInMsecs: Long = 0
    var timeZone: Int = Utils.getTMZoneOffset(timeInMsecs)
    open var uploaded = false

    override fun compareTo(other: SensingData?): Int {
        return timeInMsecs.compareTo(other!!.timeInMsecs)
    }
}