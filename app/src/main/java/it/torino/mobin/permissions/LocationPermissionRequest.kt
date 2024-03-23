import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import it.torino.mobin.permissions.CheckboxWithRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ForegroundLocationPermissions(permissionRequested: Boolean,
                                  onCheckedChange: (Boolean) -> Unit) {
    val foregroundLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val showForegroundRationale = permissionRequested &&
            (foregroundLocationPermissionState.status is PermissionStatus.Denied &&
                    (foregroundLocationPermissionState.status as PermissionStatus.Denied).shouldShowRationale)

    // Foreground permission checkbox
    CheckboxWithRationale(
        label = "Allow Foreground Location Access",
        permissionState = foregroundLocationPermissionState,
        showRationale = showForegroundRationale,
        onPermissionRequested = onCheckedChange
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BackgroundLocationPermissions(permissionRequested: Boolean,
                                  onCheckedChange: (Boolean) -> Unit) {
    val foregroundLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (foregroundLocationPermissionState.status is PermissionStatus.Granted) {
        val backgroundLocationPermissionState = rememberPermissionState(
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        val showBackgroundRationale = permissionRequested &&
                (backgroundLocationPermissionState.status is PermissionStatus.Denied &&
                        (backgroundLocationPermissionState.status as PermissionStatus.Denied).shouldShowRationale)

        CheckboxWithRationale(
            label = "Allow Background Location Access",
            permissionState = backgroundLocationPermissionState,
            showRationale = showBackgroundRationale,
            onPermissionRequested = onCheckedChange
        )
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun checkBackgroundLocationPermissionGranted(): Boolean {
    val backgroundLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    // Check and react to the permission state
    val isBackgroundLocationGranted = backgroundLocationPermissionState.status is PermissionStatus.Granted

    return isBackgroundLocationGranted

}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun checkForegroundLocationPermissionGranted(): Boolean {
    val fineLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    // Check and react to the permission state
    val isFineLocationGranted = fineLocationPermissionState.status is PermissionStatus.Granted

    return isFineLocationGranted

}