package it.torino.mobin.onboarding.permissions

import CheckboxWithRationale
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import it.torino.mobin.MainActivity
import it.torino.mobin.R
import it.torino.mobin.getNextNavigationRouteDuringOnboarding
import it.torino.mobin.onboarding.finaliseOnboarding
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.mobin.utils.PreferencesManager
import it.torino.tracker.view_model.MyViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivityRecognitionPermissions(activity: MainActivity, navController: NavHostController, viewModel: MyViewModel,
                                   preferencesManager: PreferencesManager) {
    val context = LocalContext.current

    val activityRecognitionPermissionState = rememberPermissionState(
        android.Manifest.permission.ACTIVITY_RECOGNITION
    )
    val showForegroundRationale =
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
        val textFields = getTexts()
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(textFields.size) { index ->
                textFields[index]
            }
        }
        // Foreground permission checkbox
        CheckboxWithRationale(
            modifier = Modifier,
            label = "Allow Activity Recognition Access",
            permissionState = activityRecognitionPermissionState,
            showRationale = showForegroundRationale,
            onPermissionRequested = { value ->
                if (value) {
                    finaliseOnboarding(context, activity, viewModel)
                    val nextDestination = getNextNavigationRouteDuringOnboarding(context, preferencesManager)
                    navController.navigate(nextDestination) {
                        navController.popBackStack()
                    }
                }
            }
        )
    }
}
@Composable
private fun getTexts(): List<Unit> {
    return listOf(
//        Text(
//            LocalContext.current.getString(R.string.onboarding_location_0),
//            modifier = Modifier.fillMaxWidth()
//                .padding(MediumPadding),
//            color = MaterialTheme.colorScheme.onBackground,
//            style = MaterialTheme.typography.bodyMedium,
//        ),
//        Text(
//            LocalContext.current.getString(R.string.onboarding_location_1),
//            modifier = Modifier.fillMaxWidth()
//                .padding(MediumPadding),
//            color = MaterialTheme.colorScheme.onBackground,
//            style = MaterialTheme.typography.bodyMedium,
//        ),
        Text(
            LocalContext.current.getString(R.string.A_R_description),
            modifier = Modifier.fillMaxWidth()
                .padding(MediumPadding),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
    )
}
fun activityRecognitionPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Before Android Q, the permission was not required.
    }
}
