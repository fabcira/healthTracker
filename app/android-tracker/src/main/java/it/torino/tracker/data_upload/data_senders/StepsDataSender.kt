/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import it.torino.tracker.data_upload.HttpsServer
import it.torino.tracker.data_upload.dts_data.StepsDataDTS
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import it.torino.tracker.utils.Globals
import org.json.JSONObject

class StepsDataSender(val context: Context) {
    private val TAG: String = javaClass.simpleName
    private val repositoryInstance: it.torino.tracker.Repository? = it.torino.tracker.Repository.getInstance(context)
    private var stepsToSend: List<StepsData?>? = null

    /**
     * it identifies the steps that need sending and sends them. If successul it updates their flag
     *
     * @param context
     * @param userID
     */
    fun sendStepData(userID: String) {
        val dataToSend: JSONObject = prepareStepData(userID) ?: return
        val url: String = Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_INSERT_STEPS_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            val stepsIds = stepsToSend!!.map { it!!.id }
            repositoryInstance?.dBStepsDao?.updateSentFlag(stepsIds)
        }
    }

    /**
     * it prepares the steps to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server
     */
    private fun prepareStepData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID, userID)
        stepsToSend = collectStepsFromDatabase()
        if (stepsToSend != null && stepsToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${stepsToSend!!.size} steps")
            val stepsDTSList: MutableList<StepsDataDTS> = mutableListOf()
            for (step in stepsToSend!!)
                if (step != null)
                    stepsDTSList.add(StepsDataDTS(step))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(Globals.STEPS_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(stepsDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No Steps to send")
        }
        return null
    }

    private fun collectStepsFromDatabase(): List<StepsData?>? {
        return repositoryInstance?.dBStepsDao?.getUnsentSteps(600)!!
    }
}