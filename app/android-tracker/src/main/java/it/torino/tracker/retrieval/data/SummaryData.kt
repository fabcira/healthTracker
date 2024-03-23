/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.retrieval.data

import android.util.Log
import com.google.android.gms.location.DetectedActivity
import it.torino.tracker.tracker.sensors.location_recognition.LocationData

class SummaryData(trips: MutableList<TripData>, chart: MutableList<MobilityElementData>) {

    private val TAG: String? = this::class.simpleName

    var steps: Int = 0
    var vehicleMsecs: Long = 0
    var walkingMsecs: Long = 0
    var runningMsecs: Long = 0
    var cyclingMsecs: Long = 0
    var stillMsecs: Long = 0
    var heartActivityMSecs: Long =0

    var vehicleDistance: Double = 0.0
    var walkingDistance: Double = 0.0
    var runningDistance: Double = 0.0
    var cyclingDistance: Double = 0.0

    var radius: Int = 0

    init {
        Log.i(TAG, "Creating day summary results")
        var baseLocation: LocationData? = null
        var index = 0
        while (baseLocation == null) {
            if (index == trips.size) break
            if (trips[index].locations.size > 0) {
                baseLocation = trips[index].locations[0]
            }
            index += 1
        }
        for (trip in trips) {
            steps += trip.steps
            heartActivityMSecs+= trip.heartActivityMSecs
            when (trip.activityType) {
                DetectedActivity.IN_VEHICLE -> {
                    vehicleMsecs += trip.getDuration(chart)
                    vehicleDistance += trip.distanceInMeters
                }
                DetectedActivity.ON_BICYCLE -> {
                    cyclingMsecs += trip.getDuration(chart)
                    cyclingDistance += trip.distanceInMeters
                }
                in listOf(
                    DetectedActivity.ON_FOOT,
                    DetectedActivity.WALKING
                ) -> {
                    walkingMsecs += trip.getDuration(chart)
                    walkingDistance += trip.distanceInMeters
                }
                DetectedActivity.RUNNING -> {
                    runningMsecs += trip.getDuration(chart)
                    runningDistance += trip.distanceInMeters
                }
                DetectedActivity.STILL -> {
                    stillMsecs += trip.getDuration(chart)
                }
            }
            radius = radius.coerceAtLeast(trip.getTripRadius(baseLocation))
        }

        walkingDistance /= 1000
        runningDistance /= 1000
        vehicleDistance /= 1000
        cyclingDistance /= 1000
    }
}
