/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.dts_data

import it.torino.tracker.tracker.sensors.symptoms.SymptomsData

class SymptomsDataDTS(symptomsData: SymptomsData) {
    val symptomId: String = "${symptomsData.id}"
    val timeInMsecs: Long = symptomsData.timeInMsecs
    val symptomValue = symptomsData.type
    val symptomResponse = "none: v2"
    val description = "none: v2"

    /**
     * * it is necessary to define toString otherwise the obfuscator will remove the fields of the class
     *
     * @return
     */
    override fun toString(): String {
        return "SymptomsDataDTS(id=$symptomId, timeInMsecs=$timeInMsecs, type=$symptomValue)"
    }


}