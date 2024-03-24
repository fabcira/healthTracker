package it.torino.mobin.onboarding.permissions


import BackgroundLocationPermissions
import ForegroundLocationPermissions
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import checkBackgroundLocationPermissionGranted
import checkForegroundLocationPermissionGranted
import it.torino.mobin.MainActivity
import it.torino.mobin.onboarding.finaliseOnboarding
import it.torino.tracker.view_model.MyViewModel

@Composable
fun MainPermissionsComposable(activity: MainActivity, navController: NavHostController, viewModel: MyViewModel) {
        // State to track if the user has interacted with the foreground permission checkbox
        var foregroundPermissionRequested by remember { mutableStateOf(false) }
        // State to track if the user has interacted with the background permission checkbox
        var backgroundPermissionRequested by remember { mutableStateOf(false) }
        var activityRecognitionPermissionRequested by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Column {

            // Permission has not been granted
            // You may want to show rationale or request the permission
            ForegroundLocationPermissions(
                foregroundPermissionRequested
            ) { value -> foregroundPermissionRequested = value }
            if (foregroundPermissionRequested) {
                BackgroundLocationPermissions(
                    backgroundPermissionRequested
                ) { value -> backgroundPermissionRequested = value }
                if (backgroundPermissionRequested) {
                    ActivityRecognitionPermissions(
                        activityRecognitionPermissionRequested
                    ) { value -> activityRecognitionPermissionRequested = true
                        finaliseOnboarding(context, activity, viewModel)
                        navController.navigate("Battery_optimisation") {
                            navController.popBackStack()
                        }}
                }

            }
        }
}

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

