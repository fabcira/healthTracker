package it.torino.tracker.utils


data class Preferences(
    val trackerIsActive: Boolean,
    val useStepCounter: Boolean,
    val useActivityRecognition: Boolean,
    val useLocationTracking: Boolean,
    val useBodySensors: Boolean,
    val useAccelerometer: Boolean,
    val useGyro: Boolean,
    val useMagnetometer: Boolean,
    val useMobilityModelling: Boolean,
    val sendData: Boolean
)