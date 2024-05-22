/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.restarter

import android.content.Context
import android.util.Log
import androidx.work.*
import it.torino.tracker.data_upload.data_senders.DataUploadWorker
import it.torino.tracker.tracker.TackerRestarterWorker
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.view_model.MyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TRACKER_WORK_NAME = "tracker_work"


class TrackerRestarter {
    private val dataUploadingWorkName = "general work manager"
    private val sensorWorkName = "tracker work manager"
    private val TAG = this::class.simpleName


    companion object{
        private val _emergencyDataUploadInterval = 15L
        private val _normalDataUploadInterval = 45L
        private val _dataUploadInterval = _emergencyDataUploadInterval
    }
    fun startTrackerAndDataUpload(context: Context) {
        val viewModel= MyViewModel(context)
        viewModel.setActive(true)
        startTrackerProper(context)
        startDataUploader(context, true)
    }

    /**
     * it sets the data uplaod every 12 hours if charging
     * @param context
     */
    fun startDataUploader(context: Context, requiresCharging: Boolean) {
        Log.i(TAG, "Setting the constraints for the work manager")
        val constraints = Constraints.Builder()
            .setRequiresCharging(requiresCharging)
            .build()
        // do it every 4 hours but do it only in the last 15 mins of the 4 hours (flexInterval)
        // https://medium.com/androiddevelopers/workmanager-periodicity-ff35185ff006
        // why 2 hours? Because we must get the moment when the phone is connected to the mains
        // if we put 12 hours we may miss the window of connection
        // in any case 2 hours do not cost anything because if not connected, it does not do anything
        // -- note: I have removed the last 15 minutes because it makes testing impossible
        // (you have to wait for 1.45 hours!)

        val work = PeriodicWorkRequestBuilder<DataUploadWorker>(
            _dataUploadInterval, TimeUnit.MINUTES,
            _dataUploadInterval, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        val workManager = WorkManager.getInstance(context)
        Log.i(TAG, "Enqueueing period work for the work manager")
        workManager.enqueueUniquePeriodicWork(
            dataUploadingWorkName,
            // if it is pending, do not enqueue
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            work
        )

    }

    /**
     * it starts the tracker one off
     * @param context
     */
    private fun startTrackerProper(context: Context) {
        TrackerService.timeStamp= getCurrentDateTimeString()
        Log.i(TAG, "Setting constraints for the tracker work manager")
        val constraints = Constraints.Builder()
            .build()
        val request = OneTimeWorkRequestBuilder<TackerRestarterWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(TRACKER_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }





    /**
     * used to restart the tracker when necessary (e.g. when I change the preferences)
     * @param context
     */
    fun restartTrackerProper(context: Context) {
        TrackerService.currentTracker?.stopSelf()
        // Delay the cancellation of the WorkManager task
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000) // Delay for 2 seconds to give time to call the onDestroy
            WorkManager.getInstance(context).cancelUniqueWork(TRACKER_WORK_NAME)
            startTrackerProper(context)
        }
    }

    /**
     * used to restart the tracker when necessary (e.g. when I change the preferences)
     * @param context
     */
    fun stopTrackerProper(context: Context) {
        TrackerService.currentTracker?.stopSelf()
        // Delay the cancellation of the WorkManager task
        CoroutineScope(Dispatchers.IO).launch {
            delay(2000) // Delay for 2 seconds to give time to call the onDestroy
            WorkManager.getInstance(context).cancelUniqueWork(TRACKER_WORK_NAME)
        }
    }
    fun getCurrentDateTimeString(): String {
        val date = Date()
        val formatter = SimpleDateFormat("yyyy_MM_dd-HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }
}