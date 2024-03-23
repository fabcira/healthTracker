/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna 
 * All rights Reserved
 */

package it.torino.tracker.retrieval

import android.content.Context
import android.util.Log
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.DetectedActivity.IN_VEHICLE
import com.google.android.gms.location.DetectedActivity.WALKING
import it.torino.tracker.retrieval.data.MobilityElementData
import it.torino.tracker.retrieval.data.MobilityElementData.Companion.INVALID_VALUE
import it.torino.tracker.retrieval.data.SummaryData
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.tracker.sensors.location_recognition.LocationUtilities
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.PreferencesStore
import it.torino.tracker.utils.Utils


//
// Created by Fabio Ciravegna on 29/05/2022.
// Copyright (c) 2022 The University of Sheffield. All rights reserved.
//
class TripsComputation(val context: Context,
                       val chart: MutableList<MobilityElementData>,
                       val startTime: Long,
                       val endTime: Long,
) {
    private val TAG: String = this::class.simpleName!!
    var trips: MutableList<TripData>

    init {
        trips = assignTrips()
        recoverLostActivities(context)
        trips = correctTrips()
        trips = correctSuspiciousTrips(chart)
        trips = compactConsecutiveTrips()
        finaliseLocations()
    }


    /**
     * it creates the trips using the chart of activities
     */
    private fun assignTrips(): MutableList<TripData> {
        val TEMP_TIME = 0
        Log.i(TAG, "Assigning trips")
        // now assign the final activities
        val tripsList: MutableList<TripData> = mutableListOf()
        var currentTrip = TripData(TEMP_TIME, TEMP_TIME, DetectedActivity.STILL, this.chart)
        for ((index, element) in chart.withIndex()) {
            // just in case the tag was not balanced in the end we allow also element.activityIn != INVALID_VALUE
            if (element.activityOut != INVALID_VALUE || element.activityIn != INVALID_VALUE) {
                currentTrip.endTime = index

                // to be doubly sure - we started with element INVALID_TIME as STILL, it may be incorrect
                if (currentTrip.startTime == TEMP_TIME && element.activityOut != INVALID_VALUE)
                    currentTrip.activityType = element.activityOut

                currentTrip.finalise(true)
                tripsList.add(currentTrip)
                Log.i(TAG, "Added ${currentTrip.toString(chart)}")
                currentTrip = TripData(index, TEMP_TIME, element.activityIn, this.chart)
            }
        }
        if (tripsList.size > 0 && currentTrip.endTime == TEMP_TIME && currentTrip.startTime != chart.size - 1) {
            currentTrip.endTime = chart.size - 1
            currentTrip.setNumberOfSteps()
            tripsList.add(currentTrip)
            currentTrip.finalise(true)
            Log.i(TAG, "Added ${currentTrip.toString(chart)}")
        }
        return tripsList
    }

    /**
     * once we have the final trips, we remove the ones that are pointless (e.g. stills of 1 minutes
     * or vehicles that are too short, etc.
     *
     */
    private fun correctTrips(): MutableList<TripData> {
        Log.i(TAG, "Correcting trips")
        // if the first trip in the day is a still, assign start to midnight
        if (trips.size > 0 && trips[0].activityType == DetectedActivity.STILL) {
            // in general if the first trip is still, then we start the time from midnight of that still.
            // However we have a corner case: the start of the still is == to the end of the still.
            // It is a corner case, not clear why it happens. It happens when the start tag for the still is missing
            // still is followed by a - say - walk that starts on the same minute
            // i.e. item 0 is </STILL> <WALK>
            // if we change the start time of element zero, then we risk that the following activity starts from midnight
            // i.e. walk starts from midnight
            // so we add an element to chart
            if (chart[0].activityOut != INVALID_VALUE) {
                incrementAllTripIndexByOne(trips)
                val element = MobilityElementData(Utils.midnightinMsecs(chart[0].timeInMSecs))
                element.activityIn = chart[0].activityOut
                chart.add(0,element)
            }
            chart[0].timeInMSecs = Utils.midnightinMsecs(chart[0].timeInMSecs)
            trips[0].startTime = 0
        }
        val finalTrips: MutableList<TripData> = mutableListOf()
        for ((index, trip) in trips.withIndex()) {
            if (trip.activityType == DetectedActivity.STILL
                && (trip.getDuration(chart) < MobilityResultComputation.SHORT_ACTIVITY_DURATION)
            ) {
                Log.i(TAG, "correctTrips: STILL activity too short ${trip.toString(chart)}")
                // assign the longest between the activity before and after
                val previousTripDuration = if (index > 0) trips[index - 1].getDuration(chart) else 0
                val nextTripDuration =
                    if (index < trips.size - 1) trips[index + 1].getDuration(chart) else 0
                trip.activityType =
                    if (previousTripDuration > nextTripDuration && previousTripDuration > trip.getDuration(
                            chart
                        )
                    )
                        trips[index - 1].activityType
                    else if (nextTripDuration > trip.getDuration(chart))
                        trips[index + 1].activityType
                    else trip.activityType
                trip.tagIfSuspicious()

                // check three elements, discover bike veh bike sequences and normalise veh to bikes
            } else if (index > 0 && index < trips.size - 1
                && trips[index - 1].activityType == DetectedActivity.ON_BICYCLE
                && trip.activityType == DetectedActivity.IN_VEHICLE
                && trips[index - 1].activityType == DetectedActivity.ON_BICYCLE
                && (trips[index - 1].getDuration(chart) + trips[index + 1].getDuration(chart) > trip.getDuration(
                    chart
                ))
            ) {
                Log.i(
                    TAG,
                    "correctTrips: changing type of ${trip.toString(chart)} to ${
                        ActivityData.getActivityTypeString(DetectedActivity.ON_BICYCLE)
                    }"
                )
                trip.activityType = DetectedActivity.ON_BICYCLE
                Log.i(TAG, "correctTrips: adding  ${trip.toString(chart)}")
                finalTrips.add(trip)
                trip.tagIfSuspicious()

                // check veh - short walk at high speed - veh
                // we are probably walking on a train or a plane
            } else if (index > 0 && index < trips.size - 1
                && trips[index - 1].activityType == DetectedActivity.IN_VEHICLE
                && (trip.activityType in listOf(
                    DetectedActivity.WALKING,
                    DetectedActivity.RUNNING,
                    DetectedActivity.ON_FOOT
                ))
                && (trip.getDuration(chart) < MobilityResultComputation.SHORT_ACTIVITY_DURATION)
                && trips[index - 1].activityType == DetectedActivity.IN_VEHICLE
                && (trip.getSpeedInMPerSecs() > 11)
            ) {
                Log.i(
                    TAG,
                    "correctTrips: changing type of ${trip.toString(chart)} to ${
                        ActivityData.getActivityTypeString(DetectedActivity.IN_VEHICLE)
                    }"
                )
                trip.activityType = DetectedActivity.IN_VEHICLE
                Log.i(TAG, "correctTrips: adding  ${trip.toString(chart)}")
                finalTrips.add(trip)
                trip.tagIfSuspicious()
            } else {
                Log.i(TAG, "correctTrips: adding  ${trip.toString(chart)}")
                finalTrips.add(trip)
            }
        }
        return finalTrips
    }

    /**
     * if we add an element to the chart in position 0 then we have to inrement all the start/end
     *
     * @param trips
     */
    private fun incrementAllTripIndexByOne(trips: MutableList<TripData>) {
        for (trip in trips){
            trip.startTime++
            trip.endTime++
        }

    }

    /**
     * it identifies car trips not identified as such (typically identified as stills that move
     * in location substantially. In that case it assigns the elements internal to the activity
     * as of type VEHICLE as default -  it will be corrected later if there is biking around
     * @param context the calling context
     * @param trips the current list of trips
     */
    private fun recoverLostActivities(context: Context) {
        Log.i(TAG, "Recovering lost activities")
        val userPreferences = PreferencesStore()
        val useGlobalSED = userPreferences.getBooleanPreference(
            context,
            Globals.COMPACTING_LOCATIONS_GENERAL,
            false
        )
        val finalTrips = mutableListOf<TripData>()
        for (trip in trips) {
            if ((trip.activityType == DetectedActivity.STILL || trip.activityType == MobilityElementData.INVALID_VALUE)
                && trip.getDuration(chart) > MobilityResultComputation.SHORT_ACTIVITY_DURATION * 2
            ) {
                val locationUtilities = LocationUtilities()
                if (trip.hasAverageHighCadence(chart) || trip.steps>1000) {
                    trip.activityType = WALKING
                    // we should check if the trip takes the entire duration or not
                    // in principle we could stay still for two hours and then travel for 10 minutes
                    // we do not do it for now but this is definitely a @todo
                    // @todo trimTripDuration should trim the sides of the trip and return potentially three
                    // trips - still - veh - still if the two stills are long enough to be a real
                    // activity -

                    // this is to be fixed. See 4.07.2022 data for example: note that as the A/R sensor did not work, there
                    // are walking and car trips in the same STILL so first you should extract the walking and then try to extract the vehicles
                    val subtrips = trip.trimTripDuration(WALKING)
                    finalTrips.addAll(subtrips)
                } else {
                    val movementFound = locationUtilities.isLinearTrajectoryInActivity(
                        trip.locations, useGlobalSED,
                        1500
                    )
                    if (movementFound) {
                        val subtrips = trip.trimTripDuration(IN_VEHICLE)
                        finalTrips.addAll(subtrips)
                    } else
                        finalTrips.add(trip)
                }
            } else
                finalTrips.add(trip)
        }
        trips = finalTrips
    }


    /**
     * it checks the suspicious trips and in case it changes the activity type
     * for example if a car trip has a radius of gyration <200m and there is no other driving during the day
     * then it is suspicious
     */
    private fun correctSuspiciousTrips(chart: MutableList<MobilityElementData>): MutableList<TripData> {
        val summaryData = SummaryData(trips, chart)
        for (trip in trips) {
            if (trip.reliable) continue
            if (trip.activityType == DetectedActivity.IN_VEHICLE && (
                        summaryData.vehicleMsecs < trip.getDuration(chart) * 2
                                // there are cases to consider that are long (>15 mins) veh trips but no locations
//                                || (trip.getDuration(chart) > 5 * MobilityResultComputation.SHORT_ACTIVITY_DURATION &&
                                || trip.distanceInMeters < 100)
            ) {
                if (trip.getCadence() > 20 || trip.steps > 100) {
                    if (!trip.isSuspicious(trip.activityType, trip.radiusInMeters)
                        && summaryData.cyclingMsecs > trip.getDuration(chart)
                    ) {
                        Log.i(TAG, "Changing suspicious type from VEHICLE to BIKE")
                        trip.activityType = DetectedActivity.ON_BICYCLE
                    } else if (trip.getCadence() > 20 || trip.steps > 100) {
                        Log.i(TAG, "Changing suspicious type from VEHICLE to WALKING")
                        trip.activityType = DetectedActivity.ON_FOOT
                    } else {
                        Log.i(TAG, "Changing suspicious type from VEHICLE to STILL")
                        trip.activityType = DetectedActivity.STILL
                    }
                } else {
                    Log.i(TAG, "Changing suspicious type from VEHICLE to STILL")
                    trip.activityType = DetectedActivity.STILL
                }
            }
            if (trip.activityType == DetectedActivity.ON_BICYCLE
                && summaryData.cyclingMsecs < trip.getDuration(chart) * 2
            ) {
                if (trip.getCadence() > 20 || trip.steps > 100) {
                    Log.i(TAG, "Changing suspicious type from BIKE to WALKING")
                    trip.activityType = DetectedActivity.ON_FOOT
                } else {
                    Log.i(TAG, "Changing suspicious type from BIKE to STILL")
                    trip.activityType = DetectedActivity.STILL
                }
            }
        }
        return trips
    }


    /**
     * it takes sequences of walking walking walking and turns them into just one overarching walking
     */
    private fun compactConsecutiveTrips(): MutableList<TripData> {
        val finalTrips: MutableList<TripData> = mutableListOf()
        if (trips.size > 0) finalTrips.add(trips[0])
        var prevTrip: TripData? = null
        for (index in 1 until trips.size) {
            val currentTrip = trips[index]
            if (prevTrip == null)
                trips[index - 1].also { prevTrip = it }
            val compacted: Boolean = prevTrip!!.compactIfPossible(currentTrip)
            if (!compacted) {
                finalTrips.add(currentTrip)
                prevTrip = currentTrip
            }
        }
        return finalTrips
    }

    /**
     * it considers the locations of each trip and normalises some of them
     * for example a still can have a couple of stay points just because they were a bit far away
     * it is the time to create just one location for the stay point
     */
    private fun finaliseLocations() {
        Log.i(TAG, "Finalising locations")
        val userPreferences = PreferencesStore()
        val useStayPoints = userPreferences.getBooleanPreference(
            TrackerService.currentTracker,
            Globals.STAY_POINTS, false
        )
        if (useStayPoints!!) {
            for (tripData in trips) {
                if (tripData.activityType == DetectedActivity.STILL) {
                    val allLocations: MutableList<LocationData> = mutableListOf()
                    for (location in tripData.locations) {
                        allLocations.add(location)
                        if (location.locationsSupportingCentroid.size > 0)
                            allLocations.addAll(location.locationsSupportingCentroid)
                    }
                    val locationUtilities = LocationUtilities()
                    val centroid = locationUtilities.createCentroid(allLocations)
                    tripData.locations.clear()
                    tripData.locations.add(centroid)
                }
            }
        }
    }

}