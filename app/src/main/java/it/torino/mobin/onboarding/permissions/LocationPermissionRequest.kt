import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState

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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckboxWithRationale(
    label: String,
    permissionState: PermissionState,
    showRationale: Boolean,
    onPermissionRequested: (Boolean) -> Unit
) {
    val isChecked = permissionState.status is PermissionStatus.Granted

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { shouldGrant ->
                    if (shouldGrant && !isChecked) {
                        onPermissionRequested(shouldGrant)
                        permissionState.launchPermissionRequest()
                    }
                }
            )
            Text(text = label)
        }
        if (showRationale && !isChecked) {
            // Display rationale dialog or UI component
            Text("Permission rationale...")
            // Ideally, you'd show a dialog with an affirmative action that, when clicked,
            // invokes permissionState.launchPermissionRequest()
        }
    }
}
