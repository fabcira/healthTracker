/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.retrieval.data

import android.util.Log
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.DetectedActivity.STILL
import it.torino.tracker.retrieval.MobilityResultComputation.Companion.SHORT_ACTIVITY_DURATION
import it.torino.tracker.retrieval.data.MobilityElementData.Companion.INVALID_VALUE
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.tracker.sensors.location_recognition.LocationUtilities
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.PreferencesStore
import it.torino.tracker.utils.Utils
import java.lang.Math.abs
import kotlin.math.max


//
// Created by Fabio Ciravegna on 07/03/2022.
// Copyright (c) 2022 The University of Sheffield. All rights reserved.
//
@Entity(indices = [Index("uploaded"), Index(value = arrayOf("startTime"), unique = true)])
class TripData(
    var startTime: Int,
    var endTime: Int,
    var activityType: Int,
    @Ignore
    val chart: MutableList<MobilityElementData>
) {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    var id: Int = 0
    var radiusInMeters: Int = 0
    var steps: Int = 0
    @Ignore
    var heartActivityMSecs: Long = 0L

    @Ignore
    var reliable: Boolean = true
    var distanceInMeters: Int = 0

    @Ignore
    var locations: MutableList<LocationData> = mutableListOf()

    @Ignore
    val subTrips: MutableList<TripData> = mutableListOf()

    var uploaded: Boolean = false

    /**
     * constructor for the database
     */
    constructor(
        startTime: Int,
        endTime: Int,
        activityType: Int,
        radiusInMeters: Int,
        steps: Int,
        distanceInMeters: Int,
        uploaded: Boolean
    ) :
            this(startTime, endTime, activityType, mutableListOf<MobilityElementData>()) {
        this.radiusInMeters = radiusInMeters
        this.steps = steps
        this.distanceInMeters = distanceInMeters
        this.uploaded = uploaded
    }


    companion object {
        const val MINIMUM_RADIUS_FOR_VEHICLES = 1500
        const val MINIMUM_RADIUS_FOR_BIKES = 300

        /**
         * on average if a trip has more than 40 you can think that is a reasonable guess
         * for a missed walk marked as STILL
         */
        const val HIGH_AVERAGE_CADENCE = 40
        val TAG: String? = this::class.simpleName
    }

    /**
     * called when all the information has been set. remember to call it after compacting as well
     *
     */
    fun finalise(computeLocations: Boolean) {
        setNumberOfSteps()
        if (computeLocations) {
            locations = this.getTripLocations()
            cleanLocations()
        }
        getDistanceFromLocations()
        getMovementRadius()
        tagIfSuspicious()
    }


    /**
     * @return the cadence for the trip
     */
    fun getCadence(): Int {
        return (steps / getDuration(chart) * 60.0).toInt()
    }

    fun setNumberOfSteps() {
        // the steps are NOT cumulative
        steps = 0
        heartActivityMSecs=0
        for (index in startTime..endTime) {
            if (chart[index].steps != INVALID_VALUE) {
                steps += chart[index].steps
                if (((activityType==7 || activityType==8) && chart[index].cadence>100)
                    || (activityType==1 && chart[index].cadence>40))
                    heartActivityMSecs+=if (index==0) 0L else chart[index].timeInMSecs-chart[index-1].timeInMSecs
            }
        }
    }

    fun toString(chart: MutableList<MobilityElementData>): String {
        return "TripData(start=${Utils.millisecondsToString(getStartTime(chart), "HH:mm:ss")}, end=${Utils.millisecondsToString(getEndTime(chart), "HH:mm:ss")}, activityType=${ActivityData.getActivityTypeString(activityType)})"
    }

    fun getStartTime(chart: MutableList<MobilityElementData>): Long {
        return chart[startTime].timeInMSecs
    }

    fun getEndTime(chart: MutableList<MobilityElementData>): Long {
        return chart[endTime].timeInMSecs
    }

    fun getDuration(chart: MutableList<MobilityElementData>): Long {
        val startTime = chart[startTime].timeInMSecs
        val endTime = chart[endTime].timeInMSecs
        return endTime - startTime
    }

    /**
     * it checks if it is possible to compact two consecutive activities
     * @param followingTrip
     * @return true/false - it adds the following trip to the subTrips field if compacted
     */
    fun compactIfPossible(followingTrip: TripData): Boolean {
        val diff =
            abs(chart[endTime].timeInMSecs - followingTrip.chart[followingTrip.startTime].timeInMSecs)
        if (compatibleActivityType(followingTrip)
            && diff < SHORT_ACTIVITY_DURATION
        ) {
            subTrips.add(followingTrip)
            // select the best type for the conglomerate before changing the endtime
            // but after adding to the sub-trips list
            selectBestType()
            endTime = followingTrip.endTime
            locations.addAll(followingTrip.locations)
            cleanLocations()
            // recompute the stats and locations
            finalise(false)
            return true
        }
        return false
    }

    /**
     * it considers a trip that has been fused with others. It selects the best type given the duration
     * -- mostly used to decide if we are running or walking in a mixed activity
     */
    private fun selectBestType() {
        if (subTrips.size > 0) {
            // decides the type based on the sub-trips duration
            val activityDurations: HashMap<Int, Long> = hashMapOf()
            activityDurations[activityType] = getDuration(chart)
            for (subTrip in subTrips) {
                // skip the subtrips that we have already incorporated and consider only the one that
                // has just been added
                if (subTrip.startTime > endTime) {
                    activityDurations[subTrip.activityType] =
                        if (activityDurations[subTrip.activityType] != null)
                            max(
                                subTrip.getDuration(chart),
                                activityDurations[subTrip.activityType]!!
                            )
                        else subTrip.getDuration(chart)
                }
            }
            val maxByIndex = activityDurations.maxByOrNull { it.value }
            maxByIndex?.let {
                activityType = it.key
            }
        }
    }

    /**
     * walking, running `nd on_foot are compatible activities as they can be easily misclassified
     * @param otherTrip the trip to check compatibility with
     * @return
     */
    private fun compatibleActivityType(otherTrip: TripData): Boolean {
        return (activityType == otherTrip.activityType
                // compact on foot with whatever walking or running
                || ((activityType in listOf(DetectedActivity.WALKING, DetectedActivity.ON_FOOT))
                && (otherTrip.activityType in listOf(DetectedActivity.WALKING, DetectedActivity.ON_FOOT)))
                || ((activityType in listOf(DetectedActivity.RUNNING, DetectedActivity.ON_FOOT))
                && (otherTrip.activityType in listOf(DetectedActivity.RUNNING, DetectedActivity.ON_FOOT)))

                // if type is not identical do not compact a long walk followed by a long run: they are distinct activities
                || ((getDuration(chart) < 2 * SHORT_ACTIVITY_DURATION || otherTrip.getDuration(chart) < 2 * SHORT_ACTIVITY_DURATION)
                && (activityType in listOf(DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_FOOT))
                && (otherTrip.activityType in listOf(DetectedActivity.WALKING, DetectedActivity.RUNNING, DetectedActivity.ON_FOOT))))
    }

    override fun toString(): String {
        return "TripData(${Utils.millisecondsToString(chart[startTime].timeInMSecs, "HH:mm:ss")}" +
                "-${Utils.millisecondsToString(chart[endTime].timeInMSecs, "HH:mm:ss")} " +
                "type=$activityType , steps=$steps)"
    }

    private fun getTripSteps(): MutableList<StepsData> {
        val steps: MutableList<StepsData> = mutableListOf()
        for (index in startTime until endTime) {
            val chartElement = chart[index]
            if (chartElement.steps > 0) {
                steps.add(StepsData(chartElement.timeInMSecs, chartElement.steps))
            }
        }
        return steps
    }

    /**
     * it extracts the locations from the chart associated to the trip
     * @param trip
     * @return
     */
    private fun getTripLocations(): MutableList<LocationData> {
        var locations: MutableList<LocationData> = mutableListOf()
        // find initial location - this is necessary because for e.g. vehicles it takes a time before it
        // kicks in, so we want to get the last location before leaving
        for (index in startTime - 1 downTo 0) {
            val chartElement = chart[index]
            if (chartElement.location != null) {
                locations.add(chartElement.location!!)
                break
            }
        }
        // find locations internal to trip
        for (index in startTime until endTime) {
            val chartElement = chart[index]
            if (chartElement.location != null)
                locations.add(chartElement.location!!)
        }
        // do not add a final location, as if close enough
        // it has been already added to the last element

        val userPreferences = PreferencesStore()
        val useTripsSED = userPreferences.getBooleanPreference(
            TrackerService.currentTracker,
            Globals.COMPACTING_LOCATIONS_TRIPS,
            false
        )
        if (useTripsSED!!) {
            val locUtils = LocationUtilities()
            locations = locUtils.simplifyLocationsListUsingSED(locations)
        }
        return locations
    }

    /**
     * it looks for pikes in teh location list and normalises the spikes taking the average of the
     * surrounding locations
     */
    private fun cleanLocations() {
        if (locations.size == 0) return
        for (index in 1 until locations.size - 1) {
            val loc1 = locations[index - 1]
            val loc2 = locations[index]
            val loc3 = locations[index + 1]
            val locationUtils = LocationUtilities()
            val dist13 = locationUtils.computeDistance(loc1, loc3)
            val dist12 = locationUtils.computeDistance(loc1, loc2)
            val dist23 = locationUtils.computeDistance(loc2, loc3)

            // if there is a spike take the average of the two surroounding the spike
            if (dist13 < dist12 && dist13 < dist23) {
                loc2.latitude = (loc1.latitude + loc3.latitude) / 2
                loc2.longitude = (loc1.longitude + loc3.longitude) / 2
                loc2.altitude = (loc1.altitude + loc3.altitude) / 2
            }
        }
    }

    /**
     * it computes the distance in meters covered in the trip using the (cleaned) locations
     *
     */
    private fun getDistanceFromLocations() {
        distanceInMeters = 0
        for (index in 1 until locations.size) {
            val loc1 = locations[index - 1]
            val loc2 = locations[index]
            val locationUtils = LocationUtilities()
            distanceInMeters += locationUtils.computeDistance(loc1, loc2).toInt()
        }
        Log.i(TAG, "Distance in meters: $distanceInMeters")
    }

    /**
     *
     *
     */
    private fun getMovementRadius() {
        radiusInMeters = 0
        val locationUtils = LocationUtilities()
        for ((index1, loc1) in locations.withIndex()) {
            for (index2 in index1 until locations.size) {
                val loc2 = locations[index2]
                if (loc1 != loc2)
                    radiusInMeters =
                        max(radiusInMeters, locationUtils.computeDistance(loc1, loc2).toInt())
            }
        }
    }

    /**
     * a car trip is suspicious if it does not move in location and so is a bike trip
     * maybe it is just a matter of the shopping moved around indoor or shopping at the supermarket
     * using a trolley (which looks like cycling)
     *
     */
    fun tagIfSuspicious() {
        if (isSuspicious(activityType, radiusInMeters))
            reliable = false
    }

    fun isSuspicious(activityType: Int, radiusInMeters: Int): Boolean {
        if (activityType == DetectedActivity.IN_VEHICLE && radiusInMeters < MINIMUM_RADIUS_FOR_VEHICLES) {
            return true
        } else if (activityType == DetectedActivity.ON_BICYCLE && radiusInMeters < MINIMUM_RADIUS_FOR_BIKES) {
            return true
        }
        return false
    }

    /**
     * it returns the speed in m/seconds
     * in case you wonder:
     * 10km/h = 2.77 m/sec
     * 20km/h = 5.55 m/sec
     * 30km/h = 8.33 m/sec
     * 40km/h = 11.1 m/sec
     * 50km/h = 13.88 m/sec
     *...
     */
    fun getSpeedInMPerSecs(): Long {
        val duration = getDuration(chart)
        return if (duration>0)
            (distanceInMeters / (duration / 1000.0)).toLong()
        else 0
    }

    /**
     * it checks the average cadence of a trip and a 40 steps/minute average means that there is
     * some serious cadence. No need to
     * @param chart
     * @return true or false
     */
    fun hasAverageHighCadence(chart: MutableList<MobilityElementData>): Boolean {
        val durationInMinutes = getDuration(chart) / Globals.MSECS_IN_A_MINUTE
        val isHighCadence = steps / durationInMinutes > HIGH_AVERAGE_CADENCE
        return isHighCadence && (durationInMinutes > SHORT_ACTIVITY_DURATION * 2 / Globals.MSECS_IN_A_MINUTE)
    }

    /**
     * it checks if there are considerable high cadence in the chart for more than SHORT_ACTIVITY_DURATION*2
     * 2todo however this should remove the walking from the still rather than allocating the entire thing
     * to walking - if you have a non recognised walk followed by 6 hours of still, you assign the 6 hours to walking!!
     * @param chart
     * @return true or false
     */
    fun hasNoticeableWalkingBurst(chart: MutableList<MobilityElementData>): Boolean {
        var highCadenceDurationInSecs = 0L
        var base = startTime
        for (index in startTime..endTime) {
            if (chart[index].steps != INVALID_VALUE) {
                val differenceInMSeconds = (chart[index].timeInMSecs - chart[base].timeInMSecs)
                // avoid unreasonably short bursts
                if (differenceInMSeconds > 5000) {
                    if (chart[index].cadence > HIGH_AVERAGE_CADENCE) {
                        highCadenceDurationInSecs += differenceInMSeconds
                    }
                }
                base = index
            }
        }
        if (highCadenceDurationInSecs > SHORT_ACTIVITY_DURATION * 2)
            return true
        return false
    }

    /**
     * it gets the maximum distance from any location to the base point
     * @param baseLocation the first location in the day
     * @return the distance
     */
    fun getTripRadius(baseLocation: LocationData?): Int {
        if (baseLocation == null) return 0
        val locationUtils = LocationUtilities()
        var maxDistance = 0
        // do not use low accuracy locations for the trip radius
        for (locationData in locationUtils.removeLowAccuracyLocations(locations)) {
            maxDistance = max(maxDistance, locationUtils.computeDistance(locationData, baseLocation).toInt())
        }
        return maxDistance / 2
    }

    /**
     * TODOused when we correct a trip's type: if we change vehicle to still, we need to check if
     * for example we did not have 2 hours of still and 10 minutes of vehile. In that case we will
     * return a still and a vehicle trips
     *
     * @param newActivityType
     * @return
     */
    fun trimTripDuration(newActivityType: Int): List<TripData> {
        val finalList = mutableListOf<TripData>()
        if (newActivityType == DetectedActivity.IN_VEHICLE && activityType == STILL) {
            val locationUtils = LocationUtilities()
            val tripsList = locationUtils.findLocationMovementInLocationsList(locations, 100, newActivityType, chart)
            var prevTripData: TripData? = null
            if (tripsList.size > 1) {
                var stTime = startTime
                for (tripData in tripsList) {
                    tripData.steps = 0
                    if (tripData.locations.size > 0) {
                        for (index in stTime..endTime) {
                            if (chart[index].timeInMSecs == tripData.locations[0].timeInMsecs) {
                                if (tripData.activityType == STILL)
                                    tripData.startTime = stTime
                                else
                                    tripData.startTime = index

                                // if the last one is a still it must end at the end of the current trip
                                // as it may not have locations, this may have been lost
                                if (prevTripData?.activityType == STILL)
                                    prevTripData.endTime = index
                            }
                            if (chart[index].timeInMSecs == tripData.locations[tripData.locations.size - 1].timeInMsecs) {
                                tripData.endTime = index
                                break
                            }
                        }
                        stTime = tripData.endTime + 1
                        prevTripData = tripData
                        finalList.add(tripData)
                        if (stTime >= chart.size)
                            break
                    }
                    // if there are no locations then we skip the subtrip
                }
            } else finalList.add(this)
        } else if (newActivityType in listOf<Int>(DetectedActivity.WALKING, DetectedActivity.RUNNING)) {
            // this is to be fixed. See 17.11.2022 data for example: note that as the A/R sensor did not work, there
            // is walking and that is recognised correctly but the start time is all wrong
            //finalList.add(this)
            finalList.addAll(findWalkingMovementInStepsList(getTripSteps(), newActivityType, chart))
        } else
            finalList.add(this)
        // if the last one is a still it must end at the end of the current trip
        // as it may not have locations, this may have been lost
        if (finalList.size > 1 && finalList[finalList.size - 1].activityType == STILL)
            finalList[finalList.size - 1].endTime = endTime
        return finalList
    }

    private fun findWalkingMovementInStepsList(
        steps: MutableList<StepsData>,
        activityType: Int,
        chart: MutableList<MobilityElementData>
    ): MutableList<TripData> {
        val tripsList = mutableListOf<TripData>()
        var prevSteps: StepsData? = null
        val initialStartTime = if (steps.size > 0)
            getChartIndexOfSteps(steps.get(0), chart) else -1
        var newTrip = TripData(initialStartTime, -1, STILL, chart)
        for (step in steps) {
            prevSteps?.let {
                // val distance = computeDistance(prevLocation, location)
                val differenceInTime = step.timeInMsecs - prevSteps!!.timeInMsecs
                if (differenceInTime <= SHORT_ACTIVITY_DURATION) {
                    newTrip.activityType = activityType
                } else {
                    val stepTime = getChartIndexOfSteps(step, chart)
                    newTrip.endTime = stepTime
                    // have we walked enough? If not we set it to still
                    tripsList.add(newTrip)
                    newTrip.finalise(true)
                    if (newTrip.getDuration(chart) < 60000 || newTrip.steps<200)
                        newTrip.activityType=STILL
                    newTrip = TripData(stepTime, -1, STILL, chart)
                }
            }
            prevSteps = step
        }
        // add the last one as not added yet
        if (tripsList.size > 0) {
            val locationTime = getChartIndexOfSteps(prevSteps!!, chart)
            newTrip.endTime = locationTime
            tripsList.add(newTrip)
            newTrip.finalise(true)
            if (newTrip.getDuration(chart) < 60000 || newTrip.steps<200)
                newTrip.activityType=STILL
        }
        return tripsList
    }

    /**
     * given a step, it finds the element in the chart referring to that loation, i.e. the element that
     * has the time immediately > than the location time
     * @param step the step data
     * @param chart the chart to look the position in
     * @return the index of the chart element with the time immediately > than the step time
     */
    private fun getChartIndexOfSteps(
        step: StepsData,
        chart: MutableList<MobilityElementData>
    ): Int {
        for (index in 0 until chart.size) {
            if (chart[index].timeInMSecs >= step.timeInMsecs)
                return index
        }
        return chart.size - 1
    }

    fun getStartTimeInMsecs(): Long {
        return chart[startTime].timeInMSecs
    }

    fun getEndTimeInMsecs(): Long {
        return chart[endTime].timeInMSecs
    }

}