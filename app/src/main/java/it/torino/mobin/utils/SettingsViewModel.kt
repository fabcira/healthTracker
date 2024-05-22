package it.torino.mobin.utils

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import it.torino.tracker.TrackerManager
import it.torino.tracker.utils.Preferences

class SettingsViewModel(private val applicationContext: Context) : ViewModel() {
    private val preferences: Preferences = TrackerManager.getInstance(applicationContext).getPreferences()
    val useLocationServices = mutableStateOf(preferences.useLocationTracking)
    val useActivityRecognition = mutableStateOf(preferences.useActivityRecognition)
    val useStepCounting = mutableStateOf(preferences.useStepCounter)
    val useAccelerometer = mutableStateOf(preferences.useAccelerometer)
    val useGyro = mutableStateOf(preferences.useGyro)
    val useMagnetometer = mutableStateOf(preferences.useMagnetometer)
    val useHRMonitor=  mutableStateOf(preferences.useBodySensors)

    fun setUseLocationServices(value: Boolean) {
        useLocationServices.value = value
    }

    fun setUseActivityRecognition(value: Boolean) {
        useActivityRecognition.value = value
    }

    fun setUseStepCounting(value: Boolean) {
        useStepCounting.value = value
    }


    fun setUseHRMonitor(value: Boolean) {
        useHRMonitor.value = value
    }

    fun setUseAccelerometer(value: Boolean) {
        useAccelerometer.value = value
    }

    fun setUseGyro(value: Boolean) {
        useGyro.value = value
    }

    fun setUseMagnetometer(value: Boolean) {
        useMagnetometer.value = value
    }

    fun savePreferences() {
        TrackerManager.getInstance(applicationContext).savePreferences(
            useStepCounter = useStepCounting.value,
            useActivityRecognition = useActivityRecognition.value,
            useLocationTracking = useLocationServices.value,
            useBodySensors = useHRMonitor.value,
            useAccelerometer = useAccelerometer.value,
            useGyro = useGyro.value,
            useMagnetometer = useMagnetometer.value,
            useMobilityModelling = false,  // Set this according to your app's requirements
            sendData = false  // Set this according to your app's requirements
        )
    }

}

