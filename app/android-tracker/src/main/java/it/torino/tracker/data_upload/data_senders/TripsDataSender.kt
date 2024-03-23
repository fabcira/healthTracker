/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload.data_senders

import android.content.Context
import android.util.Log
import it.torino.tracker.data_upload.HttpsServer
import it.torino.tracker.data_upload.dts_data.TripDataDTS
import it.torino.tracker.retrieval.ComputeDayDataAsync
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.PreferencesStore
import it.torino.tracker.utils.Utils
import org.json.JSONObject

class TripsDataSender(val context: Context) {
    private val TAG: String? = this::class.simpleName
    private val repositoryInstance: it.torino.tracker.Repository? = it.torino.tracker.Repository.getInstance(context)
    private var tripsToSend: List<TripData?>? = null

    fun sendAllTripsToServer(userID: String) {
        val firstSteps = repositoryInstance?.dBStepsDao?.getFirstSteps()
        val preference = PreferencesStore()
        var lastDayTripsSent = preference.getLongPreference(
            context,
            Globals.LAST_TRIPS_DAY_SENT,
            if (firstSteps != null) Utils.midnightinMsecs(firstSteps.timeInMsecs) else 0
        ) ?: return

        // take the next day
        lastDayTripsSent += Globals.MSECS_IN_A_DAY
        val currentMidnight = Utils.midnightinMsecs(System.currentTimeMillis())
        // we can only send the trips up to yesterday night
        while (lastDayTripsSent != 0L
            && lastDayTripsSent < currentMidnight
        ) {
            val dataComputer = ComputeDayDataAsync(
                context,
                null,
                lastDayTripsSent,
                lastDayTripsSent + Globals.MSECS_IN_A_DAY
            )
            dataComputer.computeResults()
            val tripsList = dataComputer.mobilityResultComputation.trips
            if (sendTripsData(tripsList, userID)) {
                preference.setLongPreference(
                    context,
                    Globals.LAST_TRIPS_DAY_SENT,
                    lastDayTripsSent
                )
                lastDayTripsSent += Globals.MSECS_IN_A_DAY
            } else // no connection to server
                break
        }
    }

    /**
     * it sends the activity data and if successful it marks them as sent
     * @param tripsToSend the list of tripsData
     * @param userID the user Id
     */
    private fun sendTripsData(tripsToSend: MutableList<TripData>, userID: String): Boolean {
        val dataObject = JSONObject()
        dataObject.put(Globals.USER_ID, userID)
        // @todo recompute data and send - not implemented yet
        // tripsToSend = collectTripsFromDatabase()
        return if (tripsToSend.size == 0) {
            Log.i(TAG, "No TRIPS to send")
            true
        } else {
            Log.i(TAG, "Sending ${tripsToSend.size} trips")
            val tripsDTSList: MutableList<TripDataDTS> = mutableListOf()
            for (trip in tripsToSend)
                tripsDTSList.add(TripDataDTS(trip))
            val dataSenderUtils = DataSenderUtils()
            dataObject.put(
                Globals.TRIPS_ON_SERVER,
                dataSenderUtils.getJSONArrayOfObjects(tripsDTSList)
            )
            sendToServer(dataObject)
        }
    }

    private fun sendToServer(dataToSend: JSONObject): Boolean {
        val url: String =
            Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_INSERT_TRIPS_URL
        val httpServer = HttpsServer()
        val returnedJSONObject: JSONObject? = httpServer.sendToServer(url, dataToSend)
        return (returnedJSONObject != null)
    }

}