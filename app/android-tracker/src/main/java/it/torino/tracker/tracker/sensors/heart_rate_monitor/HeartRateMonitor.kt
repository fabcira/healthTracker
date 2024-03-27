/*
 * Copyright (c) 2019. This code has been developed by Fabio Ciravegna, The University of Sheffield. All rights reserved. No part of this code can be used without the explicit written permission by the author
 */
package it.torino.tracker.tracker.sensors.heart_rate_monitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import it.torino.tracker.Repository
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HeartRateMonitor(
    val context: Context,
    lifecycleScope: LifecycleCoroutineScope,
    val repository: Repository?
) : SensorEventListener {
    private lateinit var repeatJob: Job
    private val _tag = this::class.java.simpleName
    private var sensorManager: SensorManager? = null
    private var heartRateSensor: Sensor? = null
    private val heartRateMonitor: HeartRateMonitor = this
    private var monitoringStartTime: Long = System.currentTimeMillis()
    private var noContactTimes: Int
    var heartRateReadingStack: MutableList<HeartRateData> = mutableListOf()
    private var sensorActive = false

    companion object {

        /**
         * the size of the stack where we store the HR reading before sending them to the database
         * consider that each reading comes every second, so 80 means a db operation every 80 seconds
         * which is a lot. On the other hand you do not want to have a large memory footprint,
         * so leave it more or less this size
         */
        private const val STANDARD_BUFFER_SIZE = 100
        private var MAX_BUFFER_SIZE = STANDARD_BUFFER_SIZE
        private const val SAMPLING_RATE_IN_MICROSECONDS = 10000 * 1000
        private const val coolingOffPeriod: Long = 45000
        private const val monitoringPeriod: Long = Globals.MSECS_IN_A_MINUTE * 2
    }

    init {
        noContactTimes = 0
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    }


    //////////////////////////////////////////////////////////
    //               API METHODS
    //////////////////////////////////////////////////////////

    /**
     * API method to be called to stop the monitor and its restarter controller
     */
    fun stopMonitor() {
        try {
            try {
                if (repeatJob.isActive)
                    repeatJob.cancel()
            } catch (e: Exception) {
                Log.i(_tag, "stop counter failed to stop" + e.message)
            }
            flushToDatabase()
        } catch (e: Exception) {
            Log.i(_tag, "Irrelevant")
        }
    }

    /**
     * it takes everything in the temp storage and calculates the avg heart rate and accuracy. Then it stores a
     * meta heart rate sensor value into the DB, The array is then flushed
     */
    private fun flushHeartRateToDB() {
        Log.i(_tag, "Flushing hr values to database")
        if (TrackerService.currentTracker != null) {
            InsertHeartRateDataAsync(
                TrackerService.currentTracker!!,
                heartRateReadingStack
            )
            heartRateReadingStack = mutableListOf()
        }
    }

    /**
     * API method to be called to start the monitor and its restarter controller
     */

    fun startMonitoring() {
        try {
            repeatJob  = startRepeatingJob(1000L)
        } catch (e: Exception) {
            Log.i(_tag, "stop counter failed to stop" + e.message)
        }
    }

    private fun startRepeatingJob(timeInterval: Long): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                monitorHeartRate()
                sensorActive = !sensorActive
                delay(if (sensorActive) coolingOffPeriod else monitoringPeriod)
            }
        }
    }

    private fun monitorHeartRate() {
        if (sensorActive) {
            Log.i(_tag, " stopping HR - restart in ${coolingOffPeriod / 1000} seconds")
            stopSensing()
        } else {
            Log.i(_tag, " starting HR - stopping in ${monitoringPeriod / 1000} seconds")
            startSensing()
        }

    }


    /**
     * API Method
     * called when the interface is opened - it flushes all data
     * and the incoming data will be flushed to the DB immediately
     * @param flush
     */
    fun keepFlushingToDB(flush: Boolean) {
        MAX_BUFFER_SIZE = if (flush) {
            sensorManager?.flush(this)
            0
        } else
            STANDARD_BUFFER_SIZE
        Log.i(_tag, "flushing hr readings? $flush")
    }


    /**
     * API method to flush to the readings to the database
     */
    fun flush() {
        flushToDatabase()
    }

    //////////////////////////////////////////////////////////
    //               Internal METHODS
    //////////////////////////////////////////////////////////


    private fun startSensing() {
        monitoringStartTime = System.currentTimeMillis()
        heartRateReadingStack = mutableListOf()
        if (sensorManager != null) {
            sensorManager!!.registerListener(
                this, heartRateSensor, SAMPLING_RATE_IN_MICROSECONDS, SAMPLING_RATE_IN_MICROSECONDS
            )
            Log.i(_tag, "HR started")
        } else
            Log.i(_tag, "I could not start HR Monitor!!")
        noContactTimes = 0
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
            if (event.accuracy != SensorManager.SENSOR_STATUS_NO_CONTACT) {
                val heartRateData = HeartRateData(Utils.fromEventTimeToEpoch(event.timestamp),
                    event.values[0].toInt(), event.accuracy)
                heartRateReadingStack.add(heartRateData)
                if (heartRateReadingStack.size > MAX_BUFFER_SIZE) {
                    flushHeartRateToDB()
                    heartRateReadingStack = mutableListOf()
                }
                repository?.currentHeartRate?.value = event.values[0].toInt()
                Log.d(_tag, "reading found " + event.values[0].toInt() + "(" + event.accuracy + ")")
            } else {
                Log.d(_tag, "Accuracy is not very good  " + event.values[0] + "(" + event.accuracy + ")")
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}


    /**
     * it flushes the HR sensor and it writes the values to the db
     */
    private fun stopSensing() {
        Log.i(_tag, "Heart Rate Sensor: flushing")
        flushToDatabase()
        try {
            if (sensorManager != null)
                sensorManager?.unregisterListener(heartRateMonitor)
        } catch (e: Exception) {
            Log.i(
                _tag,
                "Error Stopping HR Monitor ${e.message}"
            )
        } catch (e: Error) {
            Log.i(_tag, "No idea")
        }
    }

    private fun flushToDatabase() {
        sensorManager?.flush(this)
        flushHeartRateToDB()
    }
}