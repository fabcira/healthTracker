/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker.sensors.location_recognition

import android.Manifest
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import it.torino.tracker.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationTracker(context: Context) {
    private val _tag = this::class.java.simpleName
    private var lastRecordedLocation: Location? = null
    // warning: it appears that LocationRequest.create is deprecated in Nov 22
    // you must use a LocationRequest.Builder @todo
    private val locationRequest: LocationRequest
    private val fusedLocationClient: FusedLocationProviderClient?
    var currentLocation: Location? = null
    private val locationTracker: LocationTracker

    /**
     * a temporary list where we park the locations so to store into the database only every now and then
     * -- this saves considerable battery
     */
    var locationsDataList: MutableList<LocationData> = mutableListOf<LocationData>()

    companion object {
        const val DETECTION_FREQUENCY_IN_MSECS = 10000

        /** this is the delay in returning the locations, used to avoid awakening Android
         * for just one location. Let's pack all the locations in the past minute and return
         * them all at once
         */
        const val DETECTION_DELAY_IN_MSECS = 45000

        /**
         * do not return locations less distant than 10m
         */
        const val SMALLEST_DISPLACEMENT = 10

        /**
         * max size of {@link locDataList}
         */
        private const val STANDARD_MAX_SIZE = 40
        private var MAX_SIZE = STANDARD_MAX_SIZE

    }

    init {
        val filter = IntentFilter()
        filter.addAction(_tag)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
            DETECTION_FREQUENCY_IN_MSECS.toLong())
            .apply {
                setMinUpdateDistanceMeters(SMALLEST_DISPLACEMENT.toFloat())
                setGranularity(Granularity.GRANULARITY_FINE)
                setWaitForAccurateLocation(true)
            }.build()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationTracker = this
    }

    ////////////////////////////////////////////////////////////////////////
    //                              API CALLS
    ////////////////////////////////////////////////////////////////////////
    /**
     * API call from teh Tracker Service to start the location tracker
     */
    fun startLocationTracking(context: Context?) {
        if (ActivityCompat.checkSelfPermission(
                context!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Looper.myLooper() == null) Looper.prepare()
            Looper.myLooper()?.let {
                fusedLocationClient!!.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    it
                )
                Log.i(_tag, "Location tracking started")
            }
        }
    }

    /**
     * API call to stop location tracking
     */
    fun stopLocationTracking() {
        Log.i(_tag, "Stopping location tracking")
        fusedLocationClient!!.flushLocations()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun flushLocations(context: Context?) {
        Log.i(_tag, "flushing locations..")
        fusedLocationClient?.flushLocations()
        if (context != null) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.i(_tag, "saving to db!")
                saveToDBCoroutine(context)
            }
        }
    }

    private suspend fun saveToDBCoroutine(context: Context) {
        delay(1000L)
        InsertLocationDataAsync(context, locationsDataList)
        locationsDataList = mutableListOf()
        Log.i(_tag, "saved to db!")
    }
    fun keepFlushingToDB(context: Context, flush: Boolean) {
        MAX_SIZE = if (flush) {
            flushLocations(context)
            0
        } else
            STANDARD_MAX_SIZE
        Log.i(_tag, "flushing locations? $flush")
    }

    ////////////////////////////////////////////////////////////////////////
    //                  private functions
    ////////////////////////////////////////////////////////////////////////

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            currentLocation = locationResult.lastLocation
            if (currentLocation != null)
                insertLocationFromData(context, currentLocation!!)
        }
    }


    /**
     * it creates the current location and it stores it in the temporary list
     * if the temp list is overflowing, it will store all the locations into the DB
     * @param context the calling context
     * @param location teh Android location structure
     */
    fun insertLocationFromData(context: Context?, location: Location) {
        Log.i(
            _tag,
            "Location: " + Utils.millisecondsToString(location.time, "dd/MM/yyyy HH:mm:ss")
                .toString() + " " + location
        )
        val locData = LocationData(
            location.time,
            location.latitude,
            location.longitude,
            location.accuracy.toDouble(),
            location.altitude
        )
        insertLocationIntoDB(context, locData)
        lastRecordedLocation = when {
            lastRecordedLocation == null -> location
            lastRecordedLocation!!.time < location.time -> location
            else -> lastRecordedLocation
        }
    }

    /**
     * it inserts the locations into a temporary list. When the list is overflowing, then it
     * stores the values into the database
     */
    private fun insertLocationIntoDB(context: Context?, locData: LocationData) {
        locationsDataList.add(locData)
        if (context != null && locationsDataList.size > MAX_SIZE) {
            InsertLocationDataAsync(
                context,
                locationsDataList
            )
            locationsDataList = mutableListOf<LocationData>()
        }
    }
}