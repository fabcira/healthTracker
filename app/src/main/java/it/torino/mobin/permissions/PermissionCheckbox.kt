package it.torino.mobin.permissions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus

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
