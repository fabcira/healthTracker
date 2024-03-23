/*
 * Copyright (c) Developed by Fabio Ciravegna
 */
package it.torino.tracker.tracker.sensors.step_counting

import android.content.Context
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
    var stepsDataList: MutableList<StepsData> = mutableListOf()
    var sensorValuesList: MutableList<StepsData> = mutableListOf()
    val _tag = this::class.java.simpleName

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
        if (sensorManager != null) {
            stepCounterSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            if (stepCounterSensor != null) {
                Log.d(_tag, "Step Counter found on phone")
                stepCounterListener = getSensorListener()
            } else {
                Log.d(_tag, "Step Counter ot present on phone")
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////
    //           API functions
    ////////////////////////////////////////////////////////////////////////

    /**
     * API call for the Tracker Sensor to start the step counter
     */
    fun startStepCounting() {
        Log.i(_tag, "launching...")
        // if the sensor is null,then mSensorManager is null and we get a crash
        if (isStepCounterAvailable()) {
            Log.d("Standard StepCounter", "starting listener")
            registerListener()
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
            sensorManager!!.unregisterListener(stepCounterListener)
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
    private fun storeSteps(stepsData: StepsData) {
        Log.i(_tag, "Found ${stepsData.steps} steps at ${Utils.millisecondsToString(stepsData.timeInMsecs, "HH:mm:ss")}")
        stepsDataList.add(stepsData)
        if (context != null && stepsDataList.size > MAX_SIZE) {
            InsertStepsDataAsync(context, stepsDataList)
            stepsDataList = mutableListOf()
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
                    registerListener()
                    taskHandler.postDelayed(taskTimeOutRunnable, WAITING_TIME_IN_MSECS.toLong())
                }
                taskHandler.postDelayed(taskTimeOutRunnable, 1000)
            }
        }
    }

    private fun registerListener() {
        // the parameters are required in microseconds and we have them in milliseconds
        // so both are * 1000
        sensorManager!!.registerListener(
            stepCounterListener, stepCounterSensor,
            WAITING_TIME_IN_MSECS * 1000,
            2 * WAITING_TIME_IN_MSECS * 1000
        )
    }

    /**
     * constructor for the sensor listener for the step counter
     * @return the sensor listener
     */
    private fun getSensorListener(): SensorEventListener {
        return object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    sensorValuesList.add(StepsData(Utils.fromEventTimeToEpoch(event.timestamp), event.values[0].toInt()))
                    if (sensorValuesList.size > MAX_SENSOR_VALUE_LIST_SIZE) {
                        selectBestSensorValue(sensorValuesList)
                        sensorValuesList = mutableListOf()
                    }
                }
            }


            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
    }

    fun flush() {
        selectBestSensorValue(sensorValuesList)
        sensorValuesList = mutableListOf()
        try {
            sensorManager!!.flush(stepCounterListener)
        } catch (e: java.lang.Exception) {
            Log.i(_tag, "irrelevant catch")
        }
        if (context != null && stepsDataList.size > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.i(_tag, "saving to db!")
                saveToDBCoroutine(context!!)
            }
        }
    }

    private suspend fun saveToDBCoroutine(context: Context) {
        delay(1000L)
        InsertStepsDataAsync(context, stepsDataList)
        stepsDataList = mutableListOf()
        Log.i(_tag, "saved to db!")
    }

    /**
     * the sensor returns very quickly (after every step). We collect each step
     * and then every x collection, we select the best one
     * @param sensorValuesList
     */
    private fun selectBestSensorValue(sensorValuesList: MutableList<StepsData>) {
        if (sensorValuesList.size == 0) return
        if (sensorValuesList.size == 1) {
            storeSteps(sensorValuesList[0])
            return
        }
        sensorValuesList.sortBy { it.timeInMsecs }
        var firstTime = sensorValuesList[0].timeInMsecs
        val lastTime = sensorValuesList[sensorValuesList.size - 1].timeInMsecs
        if (lastTime - firstTime < WAITING_TIME_IN_MSECS * 1.1)
            storeSteps(sensorValuesList[sensorValuesList.size - 1])
        else {
            var found = false
            for (index in 1 until sensorValuesList.size) {
                val stepData = sensorValuesList[index]
                val time = stepData.timeInMsecs
                if (time - firstTime >= WAITING_TIME_IN_MSECS) {
                    storeSteps(stepData)
                    firstTime = time
                    found = true
                }
            }
            if (!found) {
                storeSteps(sensorValuesList[sensorValuesList.size-1])
            }
        }
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