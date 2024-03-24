package it.torino.mobin.onboarding.permissions

import CheckboxWithRationale
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import it.torino.mobin.R
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.SpacerHeight

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


    Column(modifier = Modifier.fillMaxSize()
        .padding(MediumPadding)) {
        Text(
            LocalContext.current.getString(R.string.physical_activity),
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacerHeight),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
        )
        // Foreground permission checkbox
        CheckboxWithRationale(
            label = "Allow Activity Recognition Access",
            permissionState = activityRecognitionPermissionState,
            showRationale = showForegroundRationale,
            onPermissionRequested = onCheckedChange
        )
    }
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
