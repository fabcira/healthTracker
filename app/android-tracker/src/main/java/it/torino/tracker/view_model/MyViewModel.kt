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
    }

    fun setRelevantLocations (locations: List<LocationData>){
        relevantLocations.value= locations
    }
    fun setActivitiesDataList(currentActivities: List<ActivityData>?) {
        try {
            this.activitiesDataList.value = currentActivities
        } catch (error: Exception) {
            Log.i("XXX", error.message!!)
        }
    }

    fun setLocationsDataList(currentLocations: List<LocationData>?) {
        this.locationsDataList.value = currentLocations
    }

    fun setActivityChart(chart: MobilityResultComputation?) {
        this.mobilityChart?.value = chart
    }

    fun setTripsList(tripsList: List<TripData>?) {
        this.tripsList?.value = tripsList
    }

    fun setStepsDataList(currentSteps: List<StepsData>?) {
        this.stepsDataList.value = currentSteps
    }

    fun setHeartRates(heartRatesList: List<HeartRateData>?) {
        this.heartRates.value = heartRatesList
    }

    fun keepFlushingToDB(flush: Boolean) {
        TrackerService.currentTracker?.keepFlushingToDB(flush)
    }
//settingsViewModel.useStepCounting.value,
//                    settingsViewModel.useActivityRecognition.value,
//                    settingsViewModel.useLocationServices.value, settingsViewModel.useStepCounting.value
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

    private fun activity_trackingis_on(): Boolean {
        return (useStepCounting.value!! || useLocationServices.value!! || useActivityRecognition.value!!)
    }

    private fun getEndTime(midnight: Long): Long {
        return if (!isToday(midnight))
            midnight + MSECS_IN_A_DAY-1
        else
            System.currentTimeMillis()
    }

    fun startTracker(context: Context) {
        keepFlushingToDB(false)
        if (isActive.value == true)
            TrackerManager.getInstance(application).onResume(this, context)
    }


    fun onPause() {
        TrackerManager.getInstance(application).onPause(this)
    }

    fun setCurrentDateTime(newTime: Long){
        currentDateTime.value=newTime
    }
    fun setCurrentTripIndex(index: Int) {
        currentTripIndex.value=index
    }

    /**
     * it stops and restarts the tracker
     */
    fun restartTracker() {
        TrackerManager.getInstance(application).restartTracker()
        isActive.value= true
    }

    fun stopTracker() {
        TrackerManager.getInstance(application).stopTracker()
        isActive.value= false
    }

    fun setActive(value: Boolean){
        isActive.value = value
    }

}