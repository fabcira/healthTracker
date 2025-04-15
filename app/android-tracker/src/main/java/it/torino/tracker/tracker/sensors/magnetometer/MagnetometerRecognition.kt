package it.torino.tracker.tracker.sensors.magnetometer

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.SensorData
import it.torino.tracker.utils.Utils
import it.torino.tracker.utils.processDataAndWriteToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class MagnetometerRecognition(
    val context: Context
): SensorEventListener {
    private var file_name = "${Globals.FILE_MAGNETOMETER}${TrackerService.timeStamp}.csv"
    private var counter =0
    private val dataBatch = ConcurrentLinkedQueue<SensorData>()
    private val batchSize = 1000 // Number of events to collect before batch insert
    private val batchTimeMs = 60000L // Time in milliseconds to force batch insert
    private var lastInsertTime = System.currentTimeMillis()
    private val sensorDelay = SensorManager.SENSOR_DELAY_FASTEST
    private val maxReportLatencyUs = 5 * 60 * 1000000 // 5 minutes in microseconds
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(SENSOR_SERVICE) as SensorManager
    }
    val magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            val stamp = event.timestamp
            val clone = event.values.clone()
            val timeStamp = Utils.fromEventTimeToEpoch(stamp)
            dataBatch.add(SensorData(timeStamp, clone))
            if (counter%batchSize==0) {
                coroutineScope.launch {
                    processDataAndWriteToFile(context, dataBatch, file_name)
                }
                counter=0
            }
            counter++
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // nothing to do
    }


    fun startMonitoring() {
        file_name = "${Globals.FILE_MAGNETOMETER}${TrackerService.timeStamp}.csv"
        sensorManager.registerListener(this, magnetometerSensor, sensorDelay, maxReportLatencyUs)
    }


    fun stopTracking() {
        Log.i("MAGNET", "Stopping Manetometer")
        val thisElem= this
        coroutineScope.launch {
            delay(3000)
            sensorManager.unregisterListener(thisElem)
            processDataAndWriteToFile(context, dataBatch, file_name)
        }
        Log.i("MAGNET", "finished saving")
    }
}