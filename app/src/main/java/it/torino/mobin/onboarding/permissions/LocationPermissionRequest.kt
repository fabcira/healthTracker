import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import it.torino.mobin.R
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.SpacerHeight
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ForegroundLocationPermissions(permissionRequested: Boolean,
                                  onCheckedChange: (Boolean) -> Unit) {
    val foregroundLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    Text(
        LocalContext.current.getString(R.string.location_services),
        modifier = Modifier
            .fillMaxWidth()
            .padding(SpacerHeight),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
    )
    val textFields = getTexts()
    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
        items(textFields.size) { index ->
            textFields[index]
        }
    }
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

@Composable
fun getTexts(): List<Unit> {
    return listOf(
        Text(
            LocalContext.current.getString(R.string.onboarding_location_0),
            modifier = Modifier.fillMaxWidth()
                .padding(MediumPadding),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        ),
//        Text(
//            LocalContext.current.getString(R.string.onboarding_location_1),
//            modifier = Modifier.fillMaxWidth()
//                .padding(MediumPadding),
//            color = MaterialTheme.colorScheme.onBackground,
//            style = MaterialTheme.typography.bodyMedium,
//        ),
        Text(
            LocalContext.current.getString(R.string.onboarding_location_2),
            modifier = Modifier.fillMaxWidth()
                .padding(MediumPadding),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
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
    var userCheckedState by remember { mutableStateOf(permissionState.status is PermissionStatus.Granted) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = userCheckedState,
                onCheckedChange = { shouldGrant ->
                    if (shouldGrant && !userCheckedState) {
                        onPermissionRequested(shouldGrant)
                        permissionState.launchPermissionRequest()
                    } else if (!shouldGrant) {
                        // Assuming you want to handle the revocation or denial of permission within the app logic
                        userCheckedState = false
                        onPermissionRequested(shouldGrant)
                    }
                }
            )
            Text(text = label)
        }


        if (showRationale && !userCheckedState) {
            // Display rationale dialog or UI component
            Text("Permission rationale...")
            // Ideally, you'd show a dialog with an affirmative action that, when clicked,
            // invokes permissionState.launchPermissionRequest()
        }
    }
}
