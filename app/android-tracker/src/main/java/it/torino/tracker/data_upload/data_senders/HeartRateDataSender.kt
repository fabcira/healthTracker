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
import it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.Utils
import org.json.JSONObject

class HeartRateDataSender(val context: Context) {
    private val TAG: String? = this::class.simpleName
    private val repositoryInstance: Repository? = Repository.getInstance(context)
    private var heartRatesToSend: List<HeartRateData?>? = null

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param context
     * @param userID
     */
    fun sendHeartRateData(userID: String) {
        val dataToSend: JSONObject = prepareHeartRateData(userID) ?: return
        Log.i(TAG, "heart rates to send: ${dataToSend.length()}")
        val url: String =
            Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_INSERT_HEART_RATES_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            Log.i(TAG, "ok")
            if (heartRatesToSend != null) {
                Log.i(TAG, "first item to send date: ${Utils.millisecondsToString(heartRatesToSend?.get(0)?.timeInMsecs!!, "dd/MM/yyyy HH:mm:ss")}")
                val heartRatesIds = heartRatesToSend?.map { it?.id }
                repositoryInstance?.dBHeartRateDao?.updateSentFlag(heartRatesIds)
            }
        }
    }

    /**
     * it prepares the heartRates to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server or null
     */
    private fun prepareHeartRateData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID, userID)
        heartRatesToSend = collectHeartRatesFromDatabase()
        if (heartRatesToSend != null && heartRatesToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${heartRatesToSend!!.size} heartRates")
            val heartRatesDTSList: MutableList<HeartRateDataDTS> = mutableListOf()
            for (heartRateData in heartRatesToSend!!)
                if (heartRateData != null)
                    heartRatesDTSList.add(HeartRateDataDTS(heartRateData))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(Globals.HEART_RATES_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(heartRatesDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No heartRates to send")
        }
        return null
    }

    private fun collectHeartRatesFromDatabase(): List<HeartRateData?>? {
        return repositoryInstance?.dBHeartRateDao?.getUnsentHeartRateData(900)!!
    }
}