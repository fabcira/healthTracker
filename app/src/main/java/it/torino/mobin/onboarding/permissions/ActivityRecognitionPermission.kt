package it.torino.mobin.onboarding.permissions

import CheckboxWithRationale
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivityRecognitionPermissions(permissionRequested: Boolean,
                                  onCheckedChange: (Boolean) -> Unit) {
    val activityRecognitionPermissionState = rememberPermissionState(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    val showForegroundRationale = permissionRequested &&
            (activityRecognitionPermissionState.status is PermissionStatus.Denied &&
                    (activityRecognitionPermissionState.status as PermissionStatus.Denied).shouldShowRationale)


    // Foreground permission checkbox
    CheckboxWithRationale(
        label = "Allow Activity Recognition Access",
        permissionState = activityRecognitionPermissionState,
        showRationale = showForegroundRationale,
        onPermissionRequested = onCheckedChange
    )
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun activityRecognitionPermissionGranted (): Boolean{
    val arPermissionState = rememberPermissionState(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    // Check and react to the permission state
    return arPermissionState.status is PermissionStatus.Granted
}
