package it.torino.mobin.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.navigation.NavHostController
import it.torino.mobin.R
import it.torino.mobin.getNextNavigationRouteDuringOnboarding
import it.torino.mobin.utils.PreferencesManager
import it.torino.tracker.view_model.MyViewModel
import it.torino.tracker.TrackerManager

fun finaliseOnboarding(
    context: Context,
    viewModel: MyViewModel,
    navController: NavHostController,
    preferencesManager: PreferencesManager
){
    onboardingFinalised(context, preferencesManager)
    val trackerManager = TrackerManager.getInstance(context)
    trackerManager.setUpTracker(
        useStepCounter = true,
        useActivityRecognition = true,
        useLocationTracking = true,
        useBodySensors = false,
        useAccelerometer = false,
        useGyro = false,
        useMagnetometer = false,
        useMobilityModelling = true,
        sendData = true
    )
    viewModel.setActive(false)
    val nextDestination = getNextNavigationRouteDuringOnboarding(context, preferencesManager)
    navController.navigate(nextDestination) {
        navController.popBackStack()
    }
}


fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}


fun onboardingFinalised(context: Context, preferencesManager: PreferencesManager) {
    val myPreferenceKey = context.getString(R.string.onboarding_finalised)
    preferencesManager.setBoolean(myPreferenceKey, true)
}