/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */
package it.torino.tracker.view_model

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import it.torino.tracker.Repository
import it.torino.tracker.TrackerManager
import it.torino.tracker.retrieval.ComputeDayDataAsync
import it.torino.tracker.retrieval.MobilityResultComputation
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.TrackerService
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import it.torino.tracker.utils.Globals.Companion.MSECS_IN_A_DAY
import it.torino.tracker.utils.Preferences
import it.torino.tracker.utils.Utils
import it.torino.tracker.utils.Utils.Companion.isToday
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MyViewModel(val application: Context) : ViewModel() {
    private val preferences: Preferences = TrackerManager.getInstance(application).getPreferences()
    val isActive: MutableLiveData<Boolean> = MutableLiveData(preferences.trackerIsActive)
    val useLocationServices : MutableLiveData<Boolean> =  MutableLiveData(preferences.useLocationTracking)
    val useActivityRecognition : MutableLiveData<Boolean> =  MutableLiveData(preferences.useActivityRecognition)
    val useStepCounting : MutableLiveData<Boolean> = MutableLiveData(preferences.useStepCounter)
    val useAccelerometer : MutableLiveData<Boolean> = MutableLiveData(preferences.useAccelerometer)
    val useGyro : MutableLiveData<Boolean> = MutableLiveData(preferences.useGyro)
    val useMagnetometer : MutableLiveData<Boolean> = MutableLiveData(preferences.useMagnetometer)
    val useHRMonitor: MutableLiveData<Boolean> =  MutableLiveData(preferences.useBodySensors)


    val relevantLocations: MutableLiveData<List<LocationData>> = MutableLiveData()
    val currentTripIndex: MutableLiveData<Int> = MutableLiveData()
    val currentDateTime: MutableLiveData<Long> = MutableLiveData()
    var stepsDataList: MutableLiveData<List<StepsData>> = MutableLiveData()
    var heartRates: MutableLiveData<List<HeartRateData>> = MutableLiveData()
    var activitiesDataList: MutableLiveData<List<ActivityData>> = MutableLiveData()
    var locationsDataList: MutableLiveData<List<LocationData>> = MutableLiveData()

    var currentHeartRate: MutableLiveData<Int>?

    var mobilityChart: MutableLiveData<MobilityResultComputation>?
    var tripsList: MutableLiveData<List<TripData>>?

    companion object {
        var repository: Repository? = null
        val TAG = MyViewModel::class.simpleName
    }

    init {
        // creation and connection to the Repository
        repository =
            if (Repository.getInstance(application) == null)
                Repository(application)
            else Repository.getInstance(application)

        currentHeartRate = repository?.currentHeartRate
        mobilityChart = repository?.mobilityChart
        tripsList = repository?.tripsList
        currentDateTime.value = System.currentTimeMillis()
        relevantLocations.value = mutableListOf()

        TrackerService.timeStamp= TrackerManager.getInstance(application).getTimeStamp(application)
    }

    /**
     * Sets the list of relevant locations.
     *
     * @param locations The list of LocationData objects to be set as relevant.
     */
    fun setRelevantLocations (locations: List<LocationData>){
        relevantLocations.value= locations
    }


    /**
     * Updates the activities data list with the provided activities.
     *
     * @param currentActivities The list of ActivityData objects to update.
     * @throws Exception if an error occurs during the update.
     */
    fun setActivitiesDataList(currentActivities: List<ActivityData>?) {
        try {
            this.activitiesDataList.value = currentActivities
        } catch (error: Exception) {
            Log.i("XXX", error.message!!)
        }
    }


    /**
     * Updates the locations data list with the provided locations.
     *
     * @param currentLocations The list of LocationData objects to update.
     */
    fun setLocationsDataList(currentLocations: List<LocationData>?) {
        this.locationsDataList.value = currentLocations
    }

    /**
     * Updates the activity chart with the provided computation result.
     *
     * @param chart The MobilityResultComputation object to update.
     */
    fun setActivityChart(chart: MobilityResultComputation?) {
        this.mobilityChart?.value = chart
    }


    /**
     * Updates the trips list with the provided trip data.
     *
     * @param tripsList The list of TripData objects to update.
     */
    fun setTripsList(tripsList: List<TripData>?) {
        this.tripsList?.value = tripsList
    }

    /**
     * Updates the steps data list with the provided steps data.
     *
     * @param currentSteps The list of StepsData objects to update.
     */
    fun setStepsDataList(currentSteps: List<StepsData>?) {
        this.stepsDataList.value = currentSteps
    }

    /**
     * Updates the heart rates list with the provided heart rate data.
     *
     * @param heartRatesList The list of HeartRateData objects to update.
     */
    fun setHeartRates(heartRatesList: List<HeartRateData>?) {
        this.heartRates.value = heartRatesList
    }

    /**
     * Controls whether data is continuously flushed to the database.
     *
     * @param flush A boolean indicating whether to keep flushing data.
     */
    fun keepFlushingToDB(flush: Boolean) {
        TrackerService.currentTracker?.keepFlushingToDB(flush)
    }
//settingsViewModel.useStepCounting.value,
//                    settingsViewModel.useActivityRecognition.value,
//                    settingsViewModel.useLocationServices.value, settingsViewModel.useStepCounting.value

    /**
     * Computes daily tracking results for the current day.
     */
    fun computeResults() {
        if (activity_trackingis_on()){
            currentDateTime.value.let {
                Log.i(TAG, "Computing results: viewModel? $this")
                val midnight = Utils.midnightinMsecs(currentDateTime.value!!)
                val computeDayDataAsync = ComputeDayDataAsync(
                    application,
                    this,
                    midnight,
                    getEndTime(midnight),
                    true
                )
                computeDayDataAsync.computeResultsAsync(this)
            }
        }
    }

    /**
     * Checks if any activity tracking services are active.
     *
     * @return A boolean indicating if activity tracking is on.
     */
    private fun activity_trackingis_on(): Boolean {
        return (useStepCounting.value!! || useLocationServices.value!! || useActivityRecognition.value!!)
    }


    /**
     * Calculates the end time for the current day's tracking session.
     *
     * @param midnight The start time (midnight) of the current day in milliseconds.
     * @return The calculated end time in milliseconds.
     */
    private fun getEndTime(midnight: Long): Long {
        return if (!isToday(midnight))
            midnight + MSECS_IN_A_DAY-1
        else
            System.currentTimeMillis()
    }


    /**
     * Starts or resumes the tracker service.
     *
     * @param context The application context.
     */
    fun startTracker(context: Context) {
        keepFlushingToDB(false)
        if (isActive.value == false) {
            TrackerManager.getInstance(application).onResume(this, context)
            setActive(true)
        }
    }


    /**
     * Pauses the tracker service.
     */
    fun onPause() {
        TrackerManager.getInstance(application).onPause(this)
    }

    /**
     * Sets the current date and time value.
     *
     * @param newTime The new date and time in milliseconds.
     */
    fun setCurrentDateTime(newTime: Long){
        currentDateTime.value=newTime
    }


    /**
     * Sets the index of the current trip.
     *
     * @param index The index of the current trip.
     */
    fun setCurrentTripIndex(index: Int) {
        currentTripIndex.value=index
    }

    /**
     * it stops and restarts the tracker
     */
    fun restartTracker() {
        TrackerManager.getInstance(application).restartTracker()
        setActive(true)
    }


    /**
     * Stops the tracker service.
     */
    fun stopTracker() {
        TrackerManager.getInstance(application).stopTracker()
        setActive(false)

    }


    /**
     * Sets the active state of the tracker.
     *
     * @param value The new active state (true or false).
     */
     fun setActive(value: Boolean){
        if (value){
            val utils= Utils()
            TrackerManager.getInstance(application).setTimeStamp(application, utils.getCurrentDateTimeString())
        } else {
            TrackerService.timeStamp= TrackerManager.getInstance(application).getTimeStamp(application)
        }
        isActive.value = value
        TrackerManager.getInstance(application).setTrackerActive(application, value)
    }


    /**
     * Retrieves the current timestamp string.
     *
     * @param application The application context.
     * @return The current timestamp as a string.
     */
    fun getCurrentTimeStamp(application: Context): String {
        return TrackerManager.getInstance(application).getTimeStamp(application)
    }

}