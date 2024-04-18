/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker

import android.content.Context
import android.util.Log
import it.torino.tracker.data_upload.UserRegistration
import it.torino.tracker.restarter.RestartTrackerBroadcastReceiver
import it.torino.tracker.restarter.TrackerRestarter
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.PreferencesStore
import it.torino.tracker.view_model.MyViewModel

class TrackerManager private constructor(private val activity: Context) {

    private var useAccelerometer: Boolean = false
    private var useBodySensors: Boolean? = false
    private var useLocationTracking: Boolean? = false
    private var useActivityRecognition: Boolean? = false
    private var useStepCounter: Boolean = false
    private var useMobilityModelling: Boolean = false
    private var sendData: Boolean = false

    companion object {

        @Volatile
        private var instance: TrackerManager? = null

        @Synchronized
        fun getInstance(activity: Context): TrackerManager {
            if (instance == null) {
                synchronized(TrackerManager::class.java) {
                    // double check locking
                    if (instance == null)
                        instance = TrackerManager(activity)
                }
            }
            instance?.initialize()
            return instance!!
        }

        val TAG = this::class.simpleName
    }

    private fun initialize() {

    }

    fun onResume(viewModel: MyViewModel, context: Context) {
        // it is necessary to restart the tracker in case it has stopped (also useful when the app
        // is installed and finally the main app is opened)
//        val i =  Intent()
//        i.action = RestartTrackerBroadcastReceiver.broadcastIntent
//        activity.sendBroadcast(i)
        RestartTrackerBroadcastReceiver.startTrackersAndUploaders(context)
        checkUserRegistration(viewModel)
    }

    fun onPause(viewModel: MyViewModel) {
        Log.i(TAG, "flushing all data")
        viewModel.keepFlushingToDB(false)
    }

    /**
     * API: all implementations must request this after setting their preferences on which modules
     * to use
     *
     */
    fun setUpTracker(useStepCounter: Boolean, useActivityRecognition: Boolean, useLocationTracking: Boolean,
                     useBodySensors: Boolean, useAccelerometer: Boolean, useMobilityModelling: Boolean,
                     sendData: Boolean) {
        this.useActivityRecognition = useActivityRecognition
        this.useLocationTracking = useLocationTracking
        this.useBodySensors = useBodySensors
        this.useAccelerometer = useAccelerometer
        this.useStepCounter = useStepCounter
        this.useMobilityModelling = useMobilityModelling
        this.sendData = sendData
        savePreferences(useStepCounter, useActivityRecognition, useLocationTracking, useBodySensors,
            useAccelerometer, useMobilityModelling, sendData)

    }

    /**
     * it sets the behaviour of the tracker and the data sending (if any)
     *
     * @param useStepCounter
     * @param useActivityRecognition
     * @param useLocationTracking
     * @param useBodySensors
     * @param sendData
     */
    private fun savePreferences(useStepCounter: Boolean, useActivityRecognition: Boolean,
                                useLocationTracking: Boolean, useBodySensors: Boolean, useAccelerometer: Boolean,
                                useMobilityModelling: Boolean, sendData: Boolean) {
        val preference = PreferencesStore()
        preference.setBooleanPreference(activity, Globals.USE_ACTIVITY_RECOGNITION, useActivityRecognition)
        preference.setBooleanPreference(activity, Globals.USE_LOCATION_TRACKING, useLocationTracking)
        preference.setBooleanPreference(activity, Globals.USE_STEP_COUNTER, useStepCounter)
        preference.setBooleanPreference(activity, Globals.USE_HEART_RATE_MONITOR, useBodySensors)
        preference.setBooleanPreference(activity, Globals.USE_ACCELEROMETER, useAccelerometer)
        preference.setBooleanPreference(activity, Globals.USE_MOBILITY_MODELLING, useMobilityModelling)
        preference.setBooleanPreference(activity, Globals.SEND_DATA_TO_SERVER, sendData)
    }



    fun startTracker() {
        // we have to force restart because it will have started without permission and it
        // is being blocked
        val trackerRestarter = TrackerRestarter()
        trackerRestarter.startTrackerAndDataUpload(activity)
    }


    /**
     * if the user is not registered with the server, it registers it
     */
    fun checkUserRegistration(viewModel: MyViewModel) {
        val userPreferences = PreferencesStore()
        val userId = userPreferences.getStringPreference(activity, Globals.USER_ID, "")
//        userPreferences.setStringPreference(activity, Globals.USER_ID, "")
        if (userId == "") {
            Log.i(TAG, "Registering user...")
            UserRegistration(activity, viewModel)
        } else Log.i(TAG, "User already registered with id $userId")
    }

    fun saveUserRegistrationId(userId: String) {
        val userPreferences = PreferencesStore()
        userPreferences.setStringPreference(activity, Globals.USER_ID, userId)
    }
}
