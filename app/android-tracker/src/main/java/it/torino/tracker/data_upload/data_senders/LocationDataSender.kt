/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import it.torino.tracker.data_upload.HttpsServer
import it.torino.tracker.data_upload.dts_data.LocationDataDTS
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.utils.Globals
import org.json.JSONObject

class LocationDataSender(val context: Context) {
    private val TAG: String = javaClass.simpleName
    private val repositoryInstance: it.torino.tracker.Repository? = it.torino.tracker.Repository.getInstance(context)
    private var locationsToSend: List<LocationData?>? = null

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param context
     * @param userID
     */
    fun sendLocationData(userID: String) {
        val dataToSend: JSONObject = prepareLocationData(userID) ?: return
        val url: String =
            Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_INSERT_LOCATIONS_URL
        val httpServer = HttpsServer()
        Log.i(TAG, "Calling send to server")
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        if (returnedJSONObject != null) {
            Log.i(TAG, "Sent ok: ${locationsToSend?.size}")
            if (locationsToSend != null) {
                val locationsIds = locationsToSend?.map { it?.id }
                Log.i(TAG, "marking ${locationsIds?.size} locations as sent")
                repositoryInstance?.dBLocationDao?.updateSentFlag(locationsIds)
            } else {
                Log.i(TAG, "Sending failed: ${locationsToSend?.size} locations were not sent (1)")
            }
        } else {
            Log.i(TAG, "Sending failed: ${locationsToSend?.size} locations were not sent (2)")
        }
    }

    /**
     * it prepares the locations to be sent to the server     *
     * @param context
     * @param userID
     * @return it returns a json object to send to the server or null
     */
    private fun prepareLocationData(userID: String): JSONObject? {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID, userID)
        locationsToSend = collectLocationsFromDatabase()
        if (locationsToSend != null && locationsToSend!!.isNotEmpty()) {
            Log.i(TAG, "Sending ${locationsToSend!!.size} locations")
            val locationsDTSList: MutableList<LocationDataDTS> = mutableListOf()
            for (location in locationsToSend!!)
                if (location != null) {
                    locationsDTSList.add(LocationDataDTS(location))
                }
            val dataSenderUtils = DataSenderUtils()
            Log.i(TAG, "about to send: ${dataSenderUtils.getJSONArrayOfObjects(locationsDTSList)}")
            dataObject.put(Globals.LOCATIONS_ON_SERVER, dataSenderUtils.getJSONArrayOfObjects(locationsDTSList))
            return dataObject
        } else {
            Log.i(TAG, "No locations to send")
        }
        return null
    }

    private fun collectLocationsFromDatabase(): List<LocationData?>? {
        return repositoryInstance?.dBLocationDao?.getUnsentLocations(600)!!
    }
}