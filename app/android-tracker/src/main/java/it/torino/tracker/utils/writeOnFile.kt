package it.torino.tracker.utils

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue

val scope = CoroutineScope(Dispatchers.IO)
val mutex = Mutex()


data class SensorData(
    val timestamp: Long,
    val values: FloatArray
)
/**
 * it writes the array of sensor readings into a file
 * @param context
 * @param queue
 * @param fileName
 */
fun processDataAndWriteToFile(context: Context, queue: ConcurrentLinkedQueue<SensorData>, fileName: String) {
//    Log.i("processDataAndWriteToFile", "writing ${queue.size} elements into file")
    scope.launch {
        mutex.withLock {
            val file = getWritableFile(context, fileName)
            try {
                FileWriter(file, true).use { writer ->
                    while (queue.isNotEmpty()) {
                        val data = queue.poll() // Remove and retrieve the head of the queue
                        if (data != null) {
                            writer.write("${data.timestamp},${data.values.joinToString(",")}\n")
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

/**
 * it opens a file in the writable directory
 * @param context
 * @param fileName
 * @return the File
 */

fun getWritableFile(context: Context, fileName: String): File {
    // Get the directory for the app's private files on external storage
    val directory = context.getExternalFilesDir(null)
    // Create a file object for the given file name
    return File(directory, fileName)
}