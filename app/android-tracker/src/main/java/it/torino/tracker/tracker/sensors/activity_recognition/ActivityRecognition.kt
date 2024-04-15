/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker.sensors.activity_recognition

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.android.gms.location.*
import com.google.android.gms.location.ActivityRecognition
import it.torino.tracker.Repository
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ActivityRecognition(
    private var callingService: TrackerService,
    lifecycleScope: LifecycleCoroutineScope,
    repository: Repository?
) {
    private val _tag = this::class.java.simpleName
    private var activityTransitionsReceiver: ActivityTransitionsReceiver? = null
    var activityDataList: MutableList<ActivityData> = mutableListOf()

    /**
     *  The intent action triggered by the identification of a transition
     */
    private val TRANSITIONS_RECEIVER_ACTION =
        "it.torino.tracker.tracker.sensors.activity_recognition.TRANSITIONS_RECEIVER_ACTION"
    private var arPendingIntent: PendingIntent

    companion object {
        fun getTransitionTypeString(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTERING"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXITING"
                else -> "--"
            }
        }
        var activityMonitor: it.torino.tracker.tracker.sensors.activity_recognition.ActivityRecognition?=null
        private var STANDARD_MAX_SIZE = 10
        private var MAX_SIZE = STANDARD_MAX_SIZE
        private const val REQUEST_CODE = 0
    }

    init {
        activityMonitor= this
        Log.i("A/R", "starting A/R")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent(callingService, ActivityTransitionsReceiver::class.java)
            intent.setAction(TRANSITIONS_RECEIVER_ACTION)
            Log.i("A/R", "Trying to launch A/R")
            arPendingIntent = PendingIntent.getBroadcast(callingService, REQUEST_CODE, intent,
                PendingIntent.FLAG_MUTABLE )

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
            arPendingIntent = PendingIntent.getBroadcast(callingService, REQUEST_CODE, intent,
                PendingIntent.FLAG_IMMUTABLE)
        }
        else {
            val intent = Intent(TRANSITIONS_RECEIVER_ACTION)
            arPendingIntent = PendingIntent.getBroadcast(callingService, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

    }

    //////////////////////////////////////////////////////////////////////
    //                      api functions
    //////////////////////////////////////////////////////////////////////
    /**
     * API called by the Tracker Service to start A/R
     */
    fun startActivityRecognition(context: Context) {
        Log.i(_tag,"Starting A/R")
        activityTransitionsReceiver = registerARReceiver(callingService, activityTransitionsReceiver)
        setupActivityTransitions(context)
    }


    /**
     * called by the main service to stop activity recognition
     * @param context the calling context
     */
    fun stopActivityRecognition(context: Context) {
        Log.i("A/R", "stopping a/r")
        // Unregister the transitions:
        if (ActivityCompat.checkSelfPermission(
                callingService,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ActivityRecognition.getClient(context).removeActivityTransitionUpdates(arPendingIntent)
            .addOnFailureListener { e ->
                Log.e(
                    _tag,
                    "Transitions Updates could not be unregistered: $e"
                )
            }
            .addOnSuccessListener { Log.i(_tag, "Transitions Updates successfully registered.") }
        if (activityTransitionsReceiver != null) {
            context.unregisterReceiver(activityTransitionsReceiver)
            activityTransitionsReceiver = null
        }
        flushToDatabase()
    }

    /**
     * it writes the remaining activities to the database
     */
    /**
     * it writes the remaining activities to the database
     */
    /**
     * it writes the remaining activities to the database
     */
    fun flushToDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(_tag, "saving to db!")
            saveToDBCoroutine()
        }
    }

    private suspend fun saveToDBCoroutine() {
        delay(1000L)
        InsertActivityDataAsync(callingService, activityDataList)
        activityDataList = mutableListOf()
        Log.i(_tag, "saved to db!")
    }

    /**
     * it flushes the activities stored in activityDataList into the database
     */
    fun flush(context: Context) {
        InsertActivityDataAsync(context, activityDataList)
        activityDataList = mutableListOf()
    }

    fun keepFlushingToDB(context: Context, flush: Boolean) {
        MAX_SIZE = if (flush) {
            flush(context)
            0
        } else
            STANDARD_MAX_SIZE
        Log.i(_tag, "flushing activities? $flush")
    }


    //////////////////////////////////////////////////////////////////////
    //                     internal methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Sets up [ActivityTransitionRequest]'s for the sample app, and registers callbacks for them
     * with a custom [BroadcastReceiver]
     * ---  UNKNOWN and TILTING are unsupported in the Transition API
     */
    private fun setupActivityTransitions(context: Context) {
        val request = ActivityTransitionRequest(getTransitionsOfInterest())
        // registering for incoming transitions updates.
        val task = if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            Log.i(_tag, "Permissions allowed")
        }
        ActivityRecognition.getClient(context)
            .requestActivityTransitionUpdates(request, arPendingIntent)
//        task.addOnSuccessListener { Log.i(TAG, "Transitions  successfully registered.") }
//        task.addOnFailureListener { e: Error -> Log.e(TAG, "Error in Transition Registration: $e") }
    }

    /**
     * it lists all the transitions that are on interest. We are interested in any relevant activity
     */
    private fun getTransitionsOfInterest(): MutableList<ActivityTransition> {
        val transitionsList: MutableList<ActivityTransition> = ArrayList()
        val typesOfInterestList = listOf(
            DetectedActivity.ON_FOOT,
            DetectedActivity.WALKING,
            DetectedActivity.RUNNING,
            DetectedActivity.ON_BICYCLE,
            DetectedActivity.IN_VEHICLE,
            DetectedActivity.STILL
            // not supported:
            // DetectedActivity.UNKNOWN,
            // DetectedActivity.TILTING
        )
        for (type in typesOfInterestList) {
            transitionsList.add(
                ActivityTransition.Builder()
                    .setActivityType(type)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build()
            )
            transitionsList.add(
                ActivityTransition.Builder()
                    .setActivityType(type)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build()
            )
        }
        return transitionsList
    }


    /**
     * it registers the transitions receiver
     */
    private fun registerARReceiver(
        context: Context,
        activityTransitionsReceiver: ActivityTransitionsReceiver?
    ): ActivityTransitionsReceiver {
        if (activityTransitionsReceiver != null) {
            // remove it just in case
            try {
                context.unregisterReceiver(activityTransitionsReceiver)
            } catch (e: Exception) {
                Log.e(_tag, "error in registering the receiver: " + e.message)
            }
        }
        val newActivityTransitionsReceiver = ActivityTransitionsReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                newActivityTransitionsReceiver,
                IntentFilter(TRANSITIONS_RECEIVER_ACTION),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                newActivityTransitionsReceiver,
                IntentFilter(TRANSITIONS_RECEIVER_ACTION)
            )
        }

        return newActivityTransitionsReceiver
    }

    /**
     * A  BroadcastReceiver handling intents from the Transitions API
     */
     class ActivityTransitionsReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("A/R", "receiving A/R event")
            if (!TextUtils.equals(activityMonitor?.TRANSITIONS_RECEIVER_ACTION, intent.action)) {
                Log.i(
                    "A/R", "Unsupported action in ActivityTransitionsReceiver: "
                            + intent.action
                )
                return
            }
            if (ActivityTransitionResult.hasResult(intent)) {
                val transitionsResults = ActivityTransitionResult.extractResult(intent)
                val transitionsEvents = transitionsResults?.transitionEvents
                if (transitionsEvents != null) {
                    for (transitionEvent in transitionsEvents) {
                        // there is a discussion saying that callbacks are received for old activities when the receiver is registered
                        // https://stackoverflow.com/questions/50257943/the-first-transition-is-always-the-same-activity-recognition-api-activity-tr
                        // typically it is just the current open activity. Check that this is the case and you do not get wring activities
                        val transitionType = transitionEvent.transitionType
                        transitionEvent.elapsedRealTimeNanos
                        val eventTimeInMsecs =
                            Utils.fromEventTimeToEpoch(transitionEvent.getElapsedRealTimeNanos())
                        Log.i(
                            "A/R", "Transition: "
                                    + activityMonitor?.getActivityType(transitionEvent.activityType) + " (" + getTransitionTypeString(
                                transitionType
                            ) + ")" + "   "
                                    + SimpleDateFormat("HH:mm:ss", Locale.US)
                                .format(Date())
                        )
                        // insert the  activity into the db
                        val activityData =
                            ActivityData(
                                eventTimeInMsecs,
                                transitionEvent.activityType,
                                transitionType
                            )
                        activityMonitor.let {
                            activityMonitor?.activityDataList?.add(activityData)

                            if (activityMonitor!!.activityDataList.size > MAX_SIZE) {
                                activityMonitor?.flushToDatabase()
                            }
                            activityMonitor?.callingService?.currentActivity(activityData)
                        }
                    }
                }
            }
        }
    }

    private fun getActivityType(intActivity: Int): String {
        return when (intActivity) {
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }

}