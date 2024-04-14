/*
 * Copyright (c) Developed by Fabio Ciravegna
 */
package it.torino.tracker.tracker.sensors.step_counting

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import it.torino.tracker.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StepCounter internal constructor(
    private var context: Context?
) {
    private lateinit var taskTimeOutRunnable: Runnable
    private lateinit var taskHandler: Handler
    private var stepCounterListener: SensorEventListener? = null
    private var sensorManager: SensorManager? = null
    private var stepCounterSensor: Sensor? = null
    var sensorValuesList: MutableList<StepsData> = mutableListOf()
    val _tag : String = this::class.java.simpleName

    // Maximum report latency in microseconds (e.g., 10 minutes)
    // Adjust this value based on your batching requirements
    private val maxReportLatencyUs: Int = 1 * 60 * 1000000

    companion object {
        var WAITING_TIME_IN_MSECS = 10000
        private const val STANDARD_MAX_SIZE = 40
        private var MAX_SIZE = STANDARD_MAX_SIZE

        // get two groups of readings because apparently you may get more than
        // 2 readings a second. This does not happen to huawei when in the background as it
        // returns only every time the handler returns (i.e. every 20 secs)
        private val MAX_SENSOR_VALUE_LIST_SIZE: Int =
            if (Build.BRAND.lowercase() == "huawei") 3 else 2 * WAITING_TIME_IN_MSECS / 1000
    }

    init {
        sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager?.let {
            stepCounterSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            stepCounterSensor?.let {
                Log.d("SensorInfo", "Maximum Range: ${it.maximumRange}")
                Log.d("SensorInfo", "Min Delay: ${it.minDelay}")
                Log.d("SensorInfo", "Power: ${it.power}")
                Log.d("SensorInfo", "Resolution: ${it.resolution}")
                Log.d(_tag, "Step Counter found on phone")
                stepCounterListener = getSensorListener()
            }
            // else branch of let
        } ?: run {
            Log.d(_tag, "Step Counter not present on phone")
        }
    }
    ////////////////////////////////////////////////////////////////////////
    //           API functions
    ////////////////////////////////////////////////////////////////////////

    /**
     * API call for the Tracker Sensor to start the step counter
     */
    fun startStepCounting(context: Context) {
        Log.i(_tag, "launching...")
        // if the sensor is null,then mSensorManager is null and we get a crash
        if (isStepCounterAvailable()) {
            Log.d("Standard StepCounter", "starting listener")
            registerListener(context)
            huaweiHandler()
        }
    }


    /**
     * API call for the Tracker Sensor to stop the step counter
     */
    fun stopStepCounting() {
        Log.i(_tag, "Stopping step counter")
        flush()
        try {
            sensorManager?.flush(stepCounterListener)
            sensorManager?.unregisterListener(stepCounterListener)
        } catch (e: java.lang.Exception) {
            //  already unregistered
            Log.i(_tag, "irrelevant ")
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //           internal functions
    ////////////////////////////////////////////////////////////////////////
    /**
     * it stores the steps into the temp list (or in case of overflow into the DB)
     * and it sets the variable lastStepsDetected
     */
    private fun storeSteps(stepData: StepsData?) {
        stepData?.let {
            Log.i(
                _tag,
                "Found ${stepData.steps} steps at ${
                    Utils.millisecondsToString(
                        stepData.timeInMsecs,
                        "HH:mm:ss"
                    )
                }"
            )
            sensorValuesList.add(stepData)
            if (context != null && sensorValuesList.size > MAX_SIZE) {
                val selectedSteps = selectBestSensorValue(sensorValuesList)
                InsertStepsDataAsync(context, selectedSteps)
                sensorValuesList = mutableListOf()
            }
        }
    }

    /**
     * huawei needs regularly stopping the step counter and restarting it
     * otherwise it will not return any steps when i the background
     *
     */
    private fun huaweiHandler() {
        if (Build.BRAND.lowercase() == "huawei") {
            if (Looper.myLooper() == null) Looper.prepare()
            val looper = Looper.myLooper()
            looper.let {
                taskHandler = Handler(it!!)
                taskTimeOutRunnable = Runnable {
                    stopStepCounting()
                    registerListener(context!!)
                    taskHandler.postDelayed(taskTimeOutRunnable, WAITING_TIME_IN_MSECS.toLong())
                }
                taskHandler.postDelayed(taskTimeOutRunnable, 1000)
            }
        }
    }

    private fun registerListener(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // the parameters are required in microseconds and we have them in milliseconds
            // so both are * 1000
            sensorManager!!.registerListener(
                stepCounterListener, stepCounterSensor,
                WAITING_TIME_IN_MSECS * 1000,
//            maxReportLatencyUs
                0
            )
        } else Log.e(_tag, "STEP COUNTER CANNOT RUN: No permissions allowed")
    }

    /**
     * constructor for the sensor listener for the step counter
     * @return the sensor listener
     */
    private fun getSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                Log.i(_tag, "Sensor changed! $event")
                if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
                    Log.i(
                        _tag,
                        "Found ${event.values[0]} steps at ${
                            Utils.millisecondsToString(
                                event.timestamp,
                                "HH:mm:ss"
                            )
                        }"
                    )
                    val currentTime = Utils.fromEventTimeToEpoch(event.timestamp)
                    val stpData= StepsData(currentTime, event.values[0].toInt())
                    storeSteps(stpData)
                }
            }


            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    fun flush() {
        val stepsDataList = selectBestSensorValue(sensorValuesList)
        sensorValuesList = mutableListOf()
        try {
            sensorManager!!.flush(stepCounterListener)
        } catch (e: java.lang.Exception) {
            Log.i(_tag, "irrelevant catch")
        }
        if (context != null && stepsDataList.size > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.i(_tag, "saving to db!")
                saveToDBCoroutine(context!!, stepsDataList)
            }
        }
    }

    private suspend fun saveToDBCoroutine(context: Context, stepsDataList: MutableList<StepsData>) {
        delay(1000L)
        InsertStepsDataAsync(context, stepsDataList)
        Log.i(_tag, "saved to db!")
    }

    /**
     * the sensor returns very quickly (after every step). We collect each step
     * and then every x collection, we select the best one
     * @param sensorValuesList
     */
    private fun selectBestSensorValue(sensorValuesList: MutableList<StepsData>) : MutableList<StepsData>{
        if (sensorValuesList.size <= 1 ) return sensorValuesList
        val finalList : MutableList<StepsData> = mutableListOf()
        sensorValuesList.sortBy { it.timeInMsecs }

        // Initialize variables for tracking last selected time and step
        var lastSelectedTime = 0L

        // Iterate through sensor readings and select readings at the specified interval
        for (stepsData in sensorValuesList) {
            // Check if enough time has elapsed since the last selected stepsData
            if (stepsData.timeInMsecs - lastSelectedTime >= WAITING_TIME_IN_MSECS) {
                // Select the sensor stepsData
                finalList.add(stepsData)
                // Update last selected time and index
                lastSelectedTime = stepsData.timeInMsecs
            }
        }
        return finalList
    }

    fun keepFlushingToDB(flush: Boolean) {
        MAX_SIZE = if (flush) {
            flush()
            0
        } else
            STANDARD_MAX_SIZE
        Log.i(_tag, "flushing steps? $flush")

    }

    /**
     * it checks if the step counter is available
     */
    private fun isStepCounterAvailable(): Boolean {
        return stepCounterSensor != null
    }

}