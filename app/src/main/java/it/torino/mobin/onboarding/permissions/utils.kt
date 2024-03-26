package it.torino.mobin.onboarding.permissions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import checkBackgroundLocationPermissionGranted
import checkForegroundLocationPermissionGranted

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun arePermissionsToBeRequested(): Boolean{
    val isForegroundLocationPermissionAsked =
        checkForegroundLocationPermissionGranted()
    val isBackgroundLocationPermissionAsked =
        checkBackgroundLocationPermissionGranted()
    val isActivityRecognitionPermissionGranted =
        activityRecognitionPermissionGranted()
    return !isForegroundLocationPermissionAsked || !isBackgroundLocationPermissionAsked
            || !isActivityRecognitionPermissionGranted
}
