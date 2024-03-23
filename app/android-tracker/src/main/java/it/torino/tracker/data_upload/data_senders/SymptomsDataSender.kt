/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import it.torino.tracker.Repository
import it.torino.tracker.data_upload.HttpsServer
import it.torino.tracker.data_upload.dts_data.HeartRateDataDTS
import it.torino.tracker.data_upload.dts_data.SymptomsDataDTS
import it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import it.torino.tracker.tracker.sensors.symptoms.SymptomsData
import it.torino.tracker.utils.Globals
import org.json.JSONObject

class SymptomsDataSender(val context: Context) {
    private val TAG: String? = this::class.simpleName
    private val repositoryInstance: Repository? = Repository.getInstance(context)
    private var symptomsToSend: List<SymptomsData?>? = null

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param context
     * @param userID
     */
    fun sendSymptomsData(userID: String) {
        val dataToSend: JSONObject = prepareSymptomsData(userID) ?: return
        val url: String =
            Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_INSERT_SYMPTOMS_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            Log.i(TAG, "ok")
            if (symptomsToSend != null) {
                val symptomsIds = symptomsToSend?.map { it?.id }
                repositoryInstance?.dBSymptomsDao?.updateSentFlag(symptomsIds)
            }
        }
    }

    /**
     * it prepares the heartRates to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server or null
     */
    private fun prepareSymptomsData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.SYMPTOMS_USER_ID, userID)
        symptomsToSend = collectSymptomsFromDatabase()
        if (symptomsToSend != null && symptomsToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${symptomsToSend!!.size} heartRates")
            val symptomsDTSList: MutableList<SymptomsDataDTS> = mutableListOf()
            for (symptom in symptomsToSend!!)
                if (symptom != null)
                    symptomsDTSList.add(SymptomsDataDTS(symptom))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(Globals.SYMPTOMS_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(symptomsDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No heartRates to send")
        }
        return null
    }

    private fun collectSymptomsFromDatabase(): List<SymptomsData?>? {
        return repositoryInstance?.dBSymptomsDao?.getUnsentSymptomsData(900)!!
    }
}