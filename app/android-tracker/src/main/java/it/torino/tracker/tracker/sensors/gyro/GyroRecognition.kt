

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

class GyroRecognition(
    val context: Context
): SensorEventListener {
    private var fileName = "${Globals.FILE_GYRO}${TrackerService.timeStamp}.csv"
    private var counter = 0
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
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val alpha = 0.8f // Adjust this value based on the level of filtering desired
    private var lastX = 0f


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val stamp = event.timestamp
            val clone = event.values.clone()
            val timeStamp = Utils.fromEventTimeToEpoch(stamp)
            dataBatch.add(SensorData(timeStamp, clone))
            if (counter % batchSize == 0) {
                coroutineScope.launch {
                    processDataAndWriteToFile(context, dataBatch, fileName)
                }
                counter = 0
            }
            counter++
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // nothing to do
    }


    fun startMonitoring() {
        fileName = "${Globals.FILE_GYRO}${TrackerService.timeStamp}.csv"
        sensorManager.registerListener(this, gyroSensor, sensorDelay, maxReportLatencyUs)
    }


    fun stopTracking() {
        val thisElem= this
        coroutineScope.launch {
            delay(3000)
            sensorManager.unregisterListener(thisElem)
            processDataAndWriteToFile(context, dataBatch, fileName)
        }
        Log.i("GYRO", "finished saving")
    }
}