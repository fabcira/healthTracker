/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import it.torino.tracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TackerRestarterWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    val TAG: String? = this::class.simpleName

    companion object {
        private const val NOTIFICATION_ID = 9972
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    Log.i(TAG, "Checking if current service is null: ${TrackerService.currentTracker}")
                    if (TrackerService.currentTracker == null) {
                        Log.i(TAG, "Launching the tracker from the job service")
                        val trackerServiceIntent = Intent(context, TrackerService::class.java)
                        TrackerNotification.notificationText = "do not close the app, please"
                        TrackerNotification.notificationIcon = R.drawable.ic_notification
                        Log.i(TAG, "Launching tracker")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(trackerServiceIntent)
                        } else {
                            context.startService(trackerServiceIntent)
                        }
                        Log.d(TAG, "started service")
                    }
                } catch (e: Exception) {
                    // nothing that we can do
                    Log.e(TAG, "Could not start Tracker at boot or regular restart")
                }
                Result.success()
            }
        } catch (e: Throwable) {
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        TrackerNotification.notificationText = "do not close the app, please"
        TrackerNotification.notificationIcon = R.drawable.ic_notification
        val notification = TrackerNotification(context, NOTIFICATION_ID, true)
        return ForegroundInfo(NOTIFICATION_ID, notification.notification!!)
    }
}
