

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.SensorData
import it.torino.tracker.utils.Utils
import it.torino.tracker.utils.processDataAndWriteToFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class GyroRecognition(
    val context: Context,
    private val lifecycleScope: CoroutineScope,
    timeStamp: String
): SensorEventListener {
    private val file_name = "${Globals.FILE_GYRO}$timeStamp.csv"
    private var counter = 0
    private val dataBatch = ConcurrentLinkedQueue<SensorData>()
    private val batchSize = 1000 // Number of events to collect before batch insert
    private val batchTimeMs = 60000L // Time in milliseconds to force batch insert
    private var lastInsertTime = System.currentTimeMillis()
    private val sensorDelay = SensorManager.SENSOR_DELAY_FASTEST
    private val maxReportLatencyUs = 5 * 60 * 1000000 // 5 minutes in microseconds

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(SENSOR_SERVICE) as SensorManager
    }
    val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val alpha = 0.8f // Adjust this value based on the level of filtering desired
    private var lastX = 0f


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val stamp = event.timestamp
            val clone = event.values.clone()
            val timeStamp = Utils.fromEventTimeToEpoch(stamp)
            dataBatch.add(SensorData(timeStamp, clone))
            if (counter%batchSize==0) {
                processDataAndWriteToFile(context, dataBatch, file_name)
                counter=0
            }
            counter++
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // nothing to do
    }

    private fun insertBatchAndClear() {
        lifecycleScope.launch {
            Log.i("ACC", "inserting ${dataBatch.size} readings")
//            repositoryInstance?.dBAccelerometerDao?.insertAll(ArrayList(dataBatch)) // Ensure DAO supports vararg or List insert
            // @todo save to file
            dataBatch.clear()
            lastInsertTime = System.currentTimeMillis()
        }
    }


    fun startMonitoring() {
        sensorManager.registerListener(this, gyroSensor, sensorDelay, maxReportLatencyUs)
    }


    fun stopTracking() {
        sensorManager.unregisterListener(this)
        processDataAndWriteToFile(context, dataBatch, file_name)
    }
}