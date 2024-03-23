/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.retrieval

import android.content.Context
import android.util.Log
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.DetectedActivity.*
import it.torino.tracker.retrieval.data.MobilityElementData
import it.torino.tracker.retrieval.data.MobilityElementData.Companion.INVALID_VALUE
import it.torino.tracker.retrieval.data.SummaryData
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.sensors.SensingData
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.tracker.sensors.step_counting.StepCounter
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.Utils
import kotlin.math.min

class MobilityResultComputation(
    val context: Context,
    val startTime: Long,
    val endTime: Long,
    val steps: MutableList<StepsData>,
    val locations: MutableList<LocationData>,
    val activities: MutableList<ActivityData>
) {
    var chart: MutableList<MobilityElementData> = mutableListOf()
    var trips: MutableList<TripData> = mutableListOf()
    var summaryData = SummaryData(mutableListOf(), chart)
    private val TAG: String = this::class.simpleName!!

    companion object {
        const val SHORT_ACTIVITY_DURATION: Long = 120000
        private const val POSSIBLY_EXPAND: Int = 0
        private const val EXPAND: Int = 1
        private const val STOP_EXPANDING: Int = -1
    }

    /**
     * it creates the mobility chart, it normalises the values, then recognises trips
     * and creates summaries for the data provided
     */
    fun computeResults() {
        val boundariesList = mergeAllBoundaries(steps, locations, activities)
        for (boundary in boundariesList) {
            val element = MobilityElementData(boundary.timeInMsecs)
            when (boundary) {
                is StepsData -> {
                    element.steps = boundary.steps
                    element.cadence = boundary.cadence
                }
                is LocationData -> {
                    element.location = boundary
                    element.speed = boundary.speed
                    element.distance = boundary.distance
                    element.longitude = boundary.longitude
                    element.latitude = boundary.latitude
                }
                is ActivityData -> {
                    if (boundary.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                        element.activityOut = boundary.type
                    } else {
                        element.activityIn = boundary.type
                    }
                }
            }
            chart.add(element)
        }
        if (chart.size > 0) {
            // if it is today, add a final still activity until now, otherwise add one until midnight
            val element = chart[chart.size - 1]
            val element2 = if (Utils.isToday(element.timeInMSecs))
                MobilityElementData(System.currentTimeMillis())
            else
                MobilityElementData(Utils.midnightinMsecs(element.timeInMSecs) + Globals.MSECS_IN_A_DAY - 1)
            element2.activityOut = STILL
            chart.add(element2)
            normaliseChartValues()
        }
        // create trips and summaries
        if (chart.size == 0) {          // default trip from now until midnight
            val activity1 = MobilityElementData(startTime)
            activity1.activityIn = STILL
            val activity2 = MobilityElementData(endTime)
            activity2.activityOut = STILL
            chart = mutableListOf()
            chart.add(activity1)
            chart.add(activity2)
        }
        val tripsComputation = TripsComputation(context, chart, startTime, endTime)
        trips = tripsComputation.trips
        compileSummary()
    }


    /**
     * * it merges the steps, activities, etc. lists  and sorts them by timeInMsecs
     * this will allow to create the activities chart
     *
     * @param steps the list of steps
     * @param locations the list of locations
     * @param activities the list of activities
     * @return a list of all the objects sorted by timeInMsecs (which in case of the range objects
     * like activities represents the endTime
     */
    private fun mergeAllBoundaries(
        steps: MutableList<StepsData>,
        locations: MutableList<LocationData>,
        activities: MutableList<ActivityData>
    ): MutableList<SensingData> {
        Log.i(TAG, "Merging boundaries")
        val finalList: MutableList<SensingData> = mutableListOf()
        finalList.addAll(steps)
        finalList.addAll(locations)
        finalList.addAll(activities)
        // for those which have ranges, we sort by endTime, which is in timeInMsecs
        finalList.sortBy { it.timeInMsecs }
        return finalList
    }


    /**
     * it fixes imprecision and other issues such as non closed activities
     * or imprecise activities boundaries, etc.
     * @return the modified chart list
     */
    private fun normaliseChartValues() {
        Log.i(TAG, "Normalising chart values")
        normaliseSteps(chart)
        chart = compressSameSecondElements()
        if (chart.size > 0) {
            closeOpenActivities()
            distributeValuesAcrossChart()
            adjustMovingActivitiesBoundaries()
            recogniseWalkingBurstsInStills()
            splitUnreasonableActivities()
            removeOpenClose()

        }
        Log.i(TAG, "Finished building chart")
    }

    /**
     * sometimes A/R gives an activity and immediately closes it within a frqctoin of a second
     * -- remove them
     *
     */
    private fun removeOpenClose() {
        for (chartElement in chart) {
            if (chartElement.activityIn != INVALID_VALUE && chartElement.activityIn == chartElement.activityOut) {
                chartElement.activityIn = INVALID_VALUE
                chartElement.activityOut = INVALID_VALUE
            }
        }

    }


    /**
     *  it distributes the steps to the elements that are intervening between two steps detection
     *  moreover the steps so far were absolute numbers (e.g. 3824). Now it normalises them
     *  into delta (e.g. 24). In doing that it also considers if the phone was rebooted
     *  and hence if the steps go to 0. The delta is preserved wihtout introducing a negative number
     *  (e.g. -8224)
     * @param chart the activity chart
     */
    private fun normaliseSteps(chart: MutableList<MobilityElementData>) {
        var prevSteps: Int = INVALID_VALUE
        for (chartElement in chart) {
            if (chartElement.steps != INVALID_VALUE) {
                if (prevSteps == INVALID_VALUE) prevSteps = chartElement.steps
                val temp = chartElement.steps
                chartElement.steps = if (chartElement.steps >= prevSteps)
                    chartElement.steps - prevSteps
                else // we have rebooted the phone so the steps were reset - take the number of steps at face value
                    chartElement.steps
                prevSteps = temp
            }
        }
    }

    /**
     * TODO to be implemented: it shoudl recognise when a still activity has an unrecognised
     * burst of walking of at least 5 minutes that was not recognised.
     * it happens on some phones esp. those where a/r does not work
     * @param finalChart
     */
    private fun recogniseWalkingBurstsInStills() {
        // to be implemented
        Log.i(TAG, "recogniseWalkingBurstsInStills is to be implemented")
    }

    /**
     * some activities must be broken e.g. if an open moving activity has a sudden
     * no data for more than 3 minutes then it was probably an unrecorded break
     * in that case we just break the activity and continue later with the same activity
     * we had cases where bike trips were stopped and restarted after one hour without data
     */
    private fun splitUnreasonableActivities() {
        var openActivity = STILL
        for ((index, chartElement) in chart.withIndex()) {
            if ((index > 0 && openActivity != STILL && chartElement.activityOut == INVALID_VALUE && chartElement.activityIn == INVALID_VALUE)
                && chartElement.timeInMSecs - chart[index - 1].timeInMSecs > 3 * SHORT_ACTIVITY_DURATION
            ) {
                chartElement.activityIn = openActivity
                chart[index - 1].activityOut = openActivity
            }
            if (chartElement.activityIn != INVALID_VALUE)
                openActivity = chartElement.activityIn
        }
    }


    /**
     * if two elements were created at the same time we merge them
     * this is useful for the activities so to have start of new activity and end of previous on the same element
     * @return the modified list top be reassigned back to the chart
     */
    private fun compressSameSecondElements(): MutableList<MobilityElementData> {
        val finalChart: MutableList<MobilityElementData> = mutableListOf()
        val INV_DOUB = INVALID_VALUE.toDouble()
        val COMPRESSION_THRESHOLD = (StepCounter.WAITING_TIME_IN_MSECS * 1.5).toLong()
        if (chart.size > 0)
            finalChart.add(chart[0])
        var considerPrevElement = true
        for (index in 1 until chart.size - 1) {
            val prevElement = chart[index - 1]
            val element = chart[index]
            val nextElement = chart[index + 1]
            // if they are in the same second then copy the valid fields
            if (considerPrevElement && Utils.getTimeInSeconds(element.timeInMSecs)
                == Utils.getTimeInSeconds(prevElement.timeInMSecs)
            ) {
                prevElement.copyValidFields(element)
                considerPrevElement = false
            } else {
                // do not consider locations -- if the element has both locations and  steps,
                // then we previously compressed this element with the precedent, so we just skip it
                // next element that had the previous element copied into
                if (element.latitude != INV_DOUB && element.steps == INVALID_VALUE) {
                    val prevTimeDiff: Long =
                        if (prevElement.latitude == INV_DOUB) Math.abs(element.timeInMSecs - prevElement.timeInMSecs) else COMPRESSION_THRESHOLD
                    val nextTimeDiff: Long =
                        if (nextElement.latitude == INV_DOUB) Math.abs(nextElement.timeInMSecs - element.timeInMSecs) else COMPRESSION_THRESHOLD
                    // if the prev is a location and/or next is a location, if the distance in time
                    // is less than 15 secs, then associate the location to the activity
                    // @todo we should probably not do that for activities that are not walking/running - in 15 secs by car you are far
                    if (min(
                            prevTimeDiff,
                            nextTimeDiff
                        ) < COMPRESSION_THRESHOLD
                    ) {
                        considerPrevElement = if (prevTimeDiff < nextTimeDiff) {
                            prevElement.copyValidFields(element)
                            false
                        } else {
                            nextElement.copyValidFields(element)
                            false

                        }
                    } else {
                        finalChart.add(element)
                        considerPrevElement = true
                    }
                } else {
                    finalChart.add(element)
                    considerPrevElement = true
                }
            }
        }
        if (chart.size > 0) finalChart.add(chart[chart.size - 1])
        return finalChart
    }

    /**
     * it adjusts the start/end of activities so to make sure that walking ends when the
     * steps stop rather than when the activity recogniser says so
     */
    private fun adjustMovingActivitiesBoundaries() {
        Log.i(TAG, "Adjusting activities boundaries")
        for ((index, chartElement) in chart.withIndex()) {
            //we should first try to move right because if for some reasons we move right
            // unreasonably according to the opening tag, then the tag will be found again later
            //as we are going from left to right and end tag moves right
            if (chartElement.activityOut != INVALID_VALUE) {
                // move the current tags right until  allowedToExpand tells us to stop
                // if allowed the tags are moved in the function allowedToExpand
                var taggedElement = chartElement
                for (ix in index + 1 until chart.size) {
                    val targetElement = chart[ix]
                    when (allowedToExpand(taggedElement, targetElement, false)) {
                        STOP_EXPANDING -> break
                        EXPAND -> {
                            moveTagsToTargetElement(taggedElement, targetElement)
                            taggedElement = targetElement
                        }
                        POSSIBLY_EXPAND -> {
                            Log.i(TAG, "Possibly expand - keep cycling")
                        }
                    }
                }
            }

            if (chartElement.activityIn != INVALID_VALUE) {
                // move the current tags left until  allowedToExpand tells us to stop
                // if allowed the tags are moved in the function allowedToExpand
                var taggedElement = chartElement
                for (ix in index - 1 downTo 0) {
                    val targetElement = chart[ix]
                    when (allowedToExpand(taggedElement, targetElement, true)) {
                        STOP_EXPANDING -> break
                        POSSIBLY_EXPAND -> {
                            Log.i(TAG, "Possibly expand - keep cycling")
                        }
                        EXPAND -> {
                            moveTagsToTargetElement(taggedElement, targetElement)
                            taggedElement = targetElement
                        }
                        //just keep cycling for POSSIBLY
                    }
                }
            }
        }
        // remove consecutive open close / close open of same activities (e.g. walking/still + still/walking and noralise to walking
        // as the still is irrelevant
        var openActivity: Int = STILL
        var prevElement: MobilityElementData? = null
        for (element in chart) {
            if (prevElement != null && prevElement.activityIn != INVALID_VALUE) {
                // by construction there is also an out activity
                // we avoid the case of walking exit still in followed immediately by walking in still out
                if (prevElement.activityIn == element.activityOut
                    && prevElement.activityOut == element.activityIn
                    // do not remove long STILL activities without input (no locs and no steps)
                    && (element.timeInMSecs - prevElement.timeInMSecs < 60000)
                ) {
                    // we remove both
                    prevElement.activityIn = INVALID_VALUE
                    element.activityOut = INVALID_VALUE
                    prevElement.activityOut = INVALID_VALUE
                    element.activityIn = INVALID_VALUE
                }
                openActivity = prevElement.activityIn
            }
            prevElement = element
        }
        // we now close the last open activity that by construction is not closed
        if (chart.size > 0)
            chart[chart.size - 1].activityOut = openActivity
    }

    /**
     * this function is the very core of the tag adjustments, i.e. where the rules are.
     * it checks if an element is allowed to expand based on its type and the characteristics of the current element
     * if it is allowed to expand, it moves the tags
     * @param currentElement the element that has the tag to move
     * @param targetElement the potential target
     * @return true if the element is compatible with expansion
     */
    private fun allowedToExpand(
        currentElement: MobilityElementData,
        targetElement: MobilityElementData,
        directionLeft: Boolean
    ): Int {
        // do not move if the target in/out are already taken
        if ((currentElement.assignedActivity == INVALID_VALUE
                    && currentElement.activityIn == INVALID_VALUE
                    && currentElement.activityOut == INVALID_VALUE)
            || (targetElement.assignedActivity != INVALID_VALUE
                    || targetElement.activityOut != INVALID_VALUE
                    || targetElement.activityIn != INVALID_VALUE)
        )
            return STOP_EXPANDING

        val timeDifferenceInSecs =
            kotlin.math.abs(targetElement.timeInMSecs - currentElement.timeInMSecs) / 1000

        if (timeDifferenceInSecs > SHORT_ACTIVITY_DURATION / 1000)
            return STOP_EXPANDING

        // move left if movement and there is a still to the left
        if ((directionLeft
                    && (currentElement.activityIn in listOf(WALKING, RUNNING, ON_FOOT, ON_BICYCLE))
                    && (currentElement.activityOut in listOf(STILL, IN_VEHICLE)))
            || (!directionLeft
                    && (currentElement.activityOut in listOf(WALKING, RUNNING, ON_FOOT, ON_BICYCLE))
                    && (currentElement.activityIn in listOf(STILL, IN_VEHICLE))
                    )
        ) {

            // the element is not compatible but we keep an open mind until it is too distant in time
            if (targetElement.cadence < 10) {
                Log.i(
                    TAG,
                    "Expanding walking: we may continue as current activity is with low cadence but <60 sec"
                )
                return POSSIBLY_EXPAND
            }
            // we can expand
            Log.i(TAG, "Expanding walking: current element is compatible")
            return EXPAND


        } else if (directionLeft && (currentElement.activityIn == IN_VEHICLE
                    && currentElement.activityOut == STILL)
            || (!directionLeft && currentElement.activityOut == IN_VEHICLE
                    && currentElement.activityIn == STILL)
        ) {
            // the element is not compatible but we keep an open mind until it is too distant in time
            if (targetElement.cadence > 10 || targetElement.speed < 0.1) {
                Log.i(TAG, "Expanding vehicle inappropriate element but time <60 sec")
                return POSSIBLY_EXPAND
            }
            Log.i(TAG, "Expanding vehicle - performed!")
            return EXPAND

            // we walk and then vehicle or STILL kicks in. As it takes time to recognise the start, we will have
            // a few seconds with no steps still allocated to the walking section. We have to remove them
            // STILL has started
        } else if (directionLeft
            && (currentElement.activityIn in listOf(STILL, IN_VEHICLE))
            && (currentElement.activityOut in listOf(WALKING, RUNNING, ON_FOOT))
        ) {
            // only cadence is important for walking- no cadence - > expand
            if (targetElement.cadence > 50)
                return STOP_EXPANDING
            // the element is not compatible but we keep an open mind until it is too distant in time
            return if (targetElement.cadence > 20) {
                Log.i(TAG, "Expanding STILL inappropriate element but time <60 sec")
                POSSIBLY_EXPAND
            } else {
                Log.i(TAG, "Expanding STILL - performed!")
                EXPAND
            }
        }
        return STOP_EXPANDING
    }

    /**
     * it moves the tags from tagged Element to target element
     * @param taggedElement the element where the tags are
     * @param targetElement the element where the tags should go
     */
    private fun moveTagsToTargetElement(
        taggedElement: MobilityElementData,
        targetElement: MobilityElementData
    ) {
        targetElement.activityIn = taggedElement.activityIn
        targetElement.activityOut = taggedElement.activityOut
        taggedElement.activityIn = INVALID_VALUE
        taggedElement.activityOut = INVALID_VALUE
    }


    /**
     * some values (e.g. cadence and speed) are to be distributed to the elements intervening
     * locations and steps as they keep constant across time
     * @param chart the activity chart
     */
    private fun distributeValuesAcrossChart() {
        Log.i(TAG, "Distributing values across chart")
        var currentCadence = INVALID_VALUE
        val invalidDouble = INVALID_VALUE.toDouble()
        var currentDistance = invalidDouble
        var currentspeed = invalidDouble
        for (element in chart.reversed()) {
            if (element.cadence == INVALID_VALUE)
                element.cadence = currentCadence
            else
                currentCadence = element.cadence
            if (element.speed == invalidDouble) {
                element.speed = currentspeed
                element.distance = currentDistance
            } else {
                currentspeed = element.speed
                currentDistance = element.distance
            }
        }
    }

    /**
     * many activities are open but never closed
     * @param chart the activity chart
     */
    private fun closeOpenActivities() {
        Log.i(TAG, "Closing open activities")
        // we must always start with a still
        if (chart.size == 0)
            return
        // unsurprisingly the following introduces lots of issues when activities
        // were started before midnight. Do not insert this as default
        //            if (chart[0].activityIn == INVALID_VALUE)
        //                chart[0].activityIn = STILL

        var openActivity: MobilityElementData = chart[0]
        for (element in chart) {
            // the open activity was not closed
            if (element != openActivity && element.activityIn != INVALID_VALUE) {
                element.activityOut = openActivity.activityIn
                // if we discover that we have closed and reopened the same activity,
                // remove both and keep the same open activity
                if (element.activityOut == element.activityIn) {
                    element.activityOut = INVALID_VALUE
                    element.activityIn = INVALID_VALUE
                } else {
                    openActivity = element
                }
            }
        }
    }


    /**
     * it collects teh summary information from the trips and makes them available to
     * the MobilityChart
     */
    fun compileSummary() {
        summaryData = SummaryData(trips, chart)
    }


}
