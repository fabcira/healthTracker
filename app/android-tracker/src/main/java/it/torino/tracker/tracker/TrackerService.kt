/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */
package it.torino.tracker.tracker

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.*
import android.os.PowerManager.WakeLock
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import it.torino.tracker.Repository
import it.torino.tracker.tracker.sensors.accelerometer.AccelerometerRecognition
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityRecognition
import it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateMonitor
import it.torino.tracker.tracker.sensors.location_recognition.LocationTracker
import it.torino.tracker.tracker.sensors.location_recognition.LocationUtilities
import it.torino.tracker.tracker.sensors.significant_motion.SignificantMotionSensor
import it.torino.tracker.tracker.sensors.step_counting.StepCounter
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.PreferencesStore


class TrackerService : LifecycleService() {
    private var savedLocation: Location? = null
    private var isContDownTimerRunning: Boolean = false
    var cntdwntmr: CountDownTimer? = null
    var locationTracker: LocationTracker? = null
    var stepCounter: StepCounter? = null
    var heartMonitor: HeartRateMonitor? = null
    var accelerometer: AccelerometerRecognition? = null
    var activityRecognition: ActivityRecognition? = null
    private var significantMotionSensor: SignificantMotionSensor? = null

    private var currentTrackerNotification: TrackerNotification? = null
    private var wakeLock: WakeLock? = null
    private var repository: Repository? = null

    // https://developer.android.com/guide/topics/data/audit-access
    // from android 11 the sensors need an attribution tag declared in teh manifest and
    // used when accessing the sensor manager to create a sensor
    lateinit var attributionContext: Context
    private val TAG = this::class.java.simpleName

    companion object {
        var currentTracker: TrackerService? = null
        private const val NOTIFICATION_ID = 9974
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Creating the Tracker Service!! $this")
        currentTracker = this

        attributionContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            createAttributionContext("data_collection_attribution_tag")
        } else
            this
        //it initialises the sensor trackers and the repository before starting the foreground process
        // We do it in the onCreate so to avoid calling this every time the intent is re-delivered
        val preference = PreferencesStore()
        val useStepCounter = preference.getBooleanPreference(this, Globals.USE_STEP_COUNTER, true)
        val useActivityRecognition =
            preference.getBooleanPreference(this, Globals.USE_ACTIVITY_RECOGNITION, true)
        val useLocationTracking =
            preference.getBooleanPreference(this, Globals.USE_LOCATION_TRACKING, true)
        val useHRmonitoring =
            preference.getBooleanPreference(this, Globals.USE_HEART_RATE_MONITOR, true)
        val useAccelerometer =
            preference.getBooleanPreference(this, Globals.USE_ACCELEROMETER, true)

        repository = Repository.getInstance(this)

        if (locationTracker == null && useLocationTracking == true)
            locationTracker = LocationTracker(this, lifecycleScope, repository)
        if (activityRecognition == null && useActivityRecognition == true)
            activityRecognition = ActivityRecognition(this, lifecycleScope, repository)
        // for the step counter we need to set up the attributiontag context as a context
        if (stepCounter == null && useStepCounter == true)
            stepCounter = StepCounter(attributionContext)
        if (heartMonitor == null && useHRmonitoring == true)
        // for the hr monitor we need to set up the attributiontag context as a context
            heartMonitor = HeartRateMonitor(attributionContext, lifecycleScope, repository)
        if (accelerometer == null && useAccelerometer == true)
        // for the hr monitor we need to set up the attributiontag context as a context
            accelerometer = AccelerometerRecognition(attributionContext, lifecycleScope, repository)

        initCountDownToStoppingSensors()
    }


    /**
     * it starts the foreground process
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "starting the foreground service...")
        startWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Log.i(TAG, "starting foreground process")
                currentTrackerNotification = TrackerNotification(this, NOTIFICATION_ID, false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        currentTrackerNotification!!.notification!!,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(NOTIFICATION_ID, currentTrackerNotification!!.notification)
                }
                Log.i(TAG, "Starting foreground process successful!")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground process " + e.message)
            }
        }
        // just in case
        stopSensors()
        startTrackers()
        return START_REDELIVER_INTENT
    }

    /**
     * it acquires the wakelock
     */
    fun startWakeLock() {
        Log.i(TAG, "Acquiring the wakelock")
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        wakeLock?.acquire()
    }

    /**
     *  it starts the sensors
     */
    fun startTrackers() {
        Log.i(TAG, "Starting trackers...")
        try {
            Log.i(TAG, "starting step counting ${stepCounter != null}")
            stepCounter?.startStepCounting(this)
        } catch (e: Exception) {
            Log.e(TAG, "error starting the step counter: " + e.message)
        }
        try {
            Log.i(TAG, "starting A/R ${activityRecognition != null}")
            activityRecognition?.startActivityRecognition(this)
        } catch (e: Exception) {
            Log.e(TAG, "error starting  A/R: " + e.message)
        }
        try {
            Log.i(TAG, "starting location tracking ${locationTracker != null}")
            locationTracker?.startLocationTracking(this)
        } catch (e: Exception) {
            Log.e(TAG, "error starting the location tracker: " + e.message)
        }
        try {
            Log.i(TAG, "starting heart rate monitoring ${heartMonitor != null}")
            heartMonitor?.startMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "error starting the hr monitor: " + e.message)
        }
        try {
            Log.i(TAG, "starting accelerometer ${accelerometer != null}")
            accelerometer?.startMonitoring()
        } catch (e: Exception) {
            Log.e(TAG, "error starting the hr monitor: " + e.message)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }


    override fun onDestroy() {
        super.onDestroy()
        flushDataToDB()
        Log.i(TAG, "TrackerService OnDestroy")
        stopSensors()
        try {
            wakeLock?.release()
        } catch (e: Exception) {
            Log.i(TAG, "wakelock crashed - irrelevant")
        }
        currentTracker = null

    }

    /**
     * it stops all the sensors - it also flushes everything to the database
     */
    private fun stopSensors() {
        Log.i(TAG, "stopping sensors")
        try {
            stepCounter?.stopStepCounting()
        } catch (e: Exception) {
            Log.i(TAG, "stepcounter failed to stop" + e.message)
        }
        try {
            heartMonitor?.stopMonitor()
        } catch (e: Exception) {
            Log.i(TAG, "HR monitor did not stop" + e.message)
        }
        try {
            activityRecognition?.stopActivityRecognition(this)
        } catch (e: Exception) {
            Log.i(TAG, "A/R did not stop " + e.message)
        }
        try {
            locationTracker?.stopLocationTracking()
        } catch (e: Exception) {
            Log.i(TAG, "location tracker did not stop " + e.message)
        }
        try {
            accelerometer?.stopTracking()
        } catch (e: Exception) {
            Log.i(TAG, "accelerometer did not stop " + e.message)
        }
    }

    private fun initCountDownToStoppingSensors(){
        if (cntdwntmr == null){
            cntdwntmr = object : CountDownTimer(600000, 600000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    val locationUtils = LocationUtilities()
                    if (locationTracker != null && locationUtils.computeDistance(
                            locationTracker!!.currentLocation,
                            savedLocation
                        ) < 100
                    ) {
                        stopSensors()
                        flushDataToDB()
                        significantMotionSensor = SignificantMotionSensor(currentTracker)
                        significantMotionSensor?.startListener()
                        isContDownTimerRunning = false
                        savedLocation = null
                        Log.i(TAG, "removing the wakelock")
                        try {
                            wakeLock?.release()
                        } catch (e: Exception) {
                            Log.i(TAG, "wakelock crashed - irrelevant")
                        }

                        cntdwntmr?.cancel()
                    } else
                        cntdwntmr?.start()
                }
            }
        }
    }

    /**
     * signal sent by the AR telling wht activity has been recognised
     * currently used when we are entering still: we start a timer that will stop  tracking
     * if we do not move for a while
     * @param activityData the current activity
     *
     */
    fun currentActivity(activityData: ActivityData) {
        if (activityData.type == DetectedActivity.STILL && activityData.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            if (!isContDownTimerRunning) {
                isContDownTimerRunning = true
                cntdwntmr?.start()
            }
        } else if (activityData.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
            if (isContDownTimerRunning) {
                cntdwntmr?.cancel()
                isContDownTimerRunning = false
            }
        }
    }

    /**
     * it saves  unsaved data to the db
     */
    fun flushDataToDB() {
        locationTracker?.flushLocations(this)
        stepCounter?.flush()
        heartMonitor?.flush()
        activityRecognition?.flush(this)
    }

    /**
     * called by the main interface - it reduces the size of the temporary queues to 1
     * and flushes the db
     * @param flush if true it flushes
     */
    fun keepFlushingToDB(flush: Boolean) {
        activityRecognition?.keepFlushingToDB(this, flush)
        stepCounter?.keepFlushingToDB(flush)
        locationTracker?.keepFlushingToDB(this, flush)
        heartMonitor?.keepFlushingToDB(flush)
    }

}