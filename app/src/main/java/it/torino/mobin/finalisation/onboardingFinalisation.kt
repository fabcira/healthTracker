package it.torino.mobin.finalisation

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import it.torino.mobin.MainActivity
import it.torino.tracker.view_model.MyViewModel
import it.torino.tracker.TrackerManager

fun finaliseOnboarding(context: Context, activity: MainActivity, viewModel: MyViewModel){
    val trackerManager = TrackerManager.getInstance(activity)
    trackerManager.setUpTracker(
        useStepCounter = true,
        useActivityRecognition = true,
        useLocationTracking = true,
        useBodySensors = false,
        useMobilityModelling = true,
        sendData = true
    )
    trackerManager.checkUserRegistration(viewModel)
    viewModel.startTracker(context)
}

fun batteryOptimisationRequest(activity: ComponentActivity) {
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    activity.startActivity(intent)
}