package it.torino.tracker.tracker.sensors.accelerometer

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewModelScope
import it.torino.tracker.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.math.sqrt

class AccelerometerRecognition(
    val context: Context,
    private val lifecycleScope: CoroutineScope,
    val repository: Repository?
): SensorEventListener {

    private val dataBatch = mutableListOf<AccelerometerData>()
    private val batchSize = 100 // Number of events to collect before batch insert
    private val batchTimeMs = 60000L // Time in milliseconds to force batch insert
    private var lastInsertTime = System.currentTimeMillis()
    private val repositoryInstance: Repository? = Repository.getInstance(context)
    private val sensorDelay = SensorManager.SENSOR_DELAY_FASTEST
    private val maxReportLatencyUs = 5 * 60 * 1000000 // 5 minutes in microseconds

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val alpha = 0.8f // Adjust this value based on the level of filtering desired
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            synchronized(dataBatch) {
                if (lastX == 0f && lastY == 0f && lastZ == 0f) {
                    val accData = AccelerometerData(
                        timestamp = System.currentTimeMillis(),
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2]
                    )
                    Log.i("Accelerometer", "first accelerometer data $accData")
                    dataBatch.add(accData)
                    lastX = accData.x
                    lastY = accData.y
                    lastZ = accData.z
                } else {
                    val deltaX = Math.abs(event.values[0] - lastX)
                    val deltaY = Math.abs(event.values[1] - lastY)
                    val deltaZ = Math.abs(event.values[2] - lastZ)

// Calculate the magnitude of the change
                    val changeMagnitude = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

// Threshold for detecting movement
                    val movementThreshold = 0.2f // Adjust based on sensitivity desired

                    if (changeMagnitude > movementThreshold) {
                        val accData = AccelerometerData(
                            timestamp = System.currentTimeMillis(),
                            x = event.values[0],
                            y = event.values[1],
                            z = event.values[2]
                        )
                        Log.i("Accelerometer", "first accelerometer data $accData")
                        dataBatch.add(accData)
                        if (dataBatch.size >= batchSize || System.currentTimeMillis() - lastInsertTime >= batchTimeMs) {
                            insertBatchAndClear()
                        }
                        lastX = accData.x
                        lastY = accData.y
                        lastZ = accData.z
                    }
                }
            }
        }
    }



    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // nothing to do
    }

    private fun insertBatchAndClear() {
        lifecycleScope.launch {
            Log.i("ACC", "inserting ${dataBatch.size} readings")
            repositoryInstance?.dBAccelerometerDao?.insertAll(ArrayList(dataBatch)) // Ensure DAO supports vararg or List insert
            dataBatch.clear()
            lastInsertTime = System.currentTimeMillis()
        }
    }


    fun startMonitoring() {
        sensorManager.registerListener(this, accelerometer, sensorDelay, maxReportLatencyUs)
    }


    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }
}