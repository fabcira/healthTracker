/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.restarter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import it.torino.tracker.utils.Globals

class RestartTrackerBroadcastReceiver : BroadcastReceiver() {
    val TAG: String = this::class.java.simpleName
    companion object {
        const val broadcastIntent: String= "it.torino.core_engine.restarter.RestartTracker"

        fun getCodeVersion(): Long {
            try {
                return System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("RestartTrackerBroadcastReceiver", e.message!!)
            }
            return 0
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        FirebaseApp.initializeApp(context)
        Log.i(TAG, "Re-starter broadcast received!")
        if (intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.i(TAG, " package reinstalled")
            val versionNo = getCodeVersion()
            val sharedPref = context.getSharedPreferences(Globals.PREFERENCES_FILE, 0)
            if (sharedPref != null) {
                val editor = sharedPref.edit()
                editor.putLong(Globals.APP_VERSION_INSTALLATION_DATE, System.currentTimeMillis())
                editor.putLong(Globals.APP_VERSION_NO, versionNo)
                editor.apply()
            }
        }
        val permissionGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            val trackerRestarter = TrackerRestarter()
            Log.i(TAG, "starting tracker...")
            trackerRestarter.startTrackerAndDataUpload(context)
        } else {
            Log.i(TAG, "No permissions set yet - not starting tracker...")
        }
    }

}