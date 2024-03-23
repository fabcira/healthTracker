/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import it.torino.tracker.Repository
import it.torino.tracker.data_upload.HttpsServer
import it.torino.tracker.data_upload.dts_data.ActivityDataDTS
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.utils.Globals
import org.json.JSONObject

class ActivityDataSender(val context: Context) {
    private val TAG: String? = this::class.simpleName
    private val repositoryInstance: Repository? = Repository.getInstance(context)
    private var activitiesToSend: List<ActivityData?>? = null

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param context
     * @param userID
     */
    fun sendActivityData(userID: String) {
        val dataToSend: JSONObject = prepareActivityData(userID) ?: return
        val url: String = Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_INSERT_ACTIVITIES_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            val activitiesIds = activitiesToSend!!.map { it!!.id }
            repositoryInstance?.dBActivityDao?.updateSentFlag(activitiesIds)
        }
    }

    /**
     * it prepares the activities to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server or null
     */
    private fun prepareActivityData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID, userID)
        activitiesToSend = collectActivitiesFromDatabase()
        if (activitiesToSend != null && activitiesToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${activitiesToSend!!.size} activities")
            val activityDTSList: MutableList<ActivityDataDTS> = mutableListOf()
            for (activity in activitiesToSend!!)
                if (activity != null) activityDTSList.add(ActivityDataDTS(activity))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(Globals.ACTIVITIES_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(activityDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No activities to send")
        }
        return null
    }


    private fun collectActivitiesFromDatabase(): List<ActivityData?>? {
        return repositoryInstance?.dBActivityDao?.getUnsentActivities(600)!!
    }

}