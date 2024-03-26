import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import it.torino.mobin.R
import it.torino.mobin.onboarding.permissions.setPrivacyPolicyShown
import it.torino.mobin.ui.theme.LargePadding
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.mobin.utils.CustomButton
import it.torino.mobin.utils.LocalPreferencesManager


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionsComposable(
    navController: NavHostController
) {
    val preferencesManager = LocalPreferencesManager.current
    val context = LocalContext.current
    val foregroundLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    val backgroundLocationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
    // State to track if the user has interacted with the foreground permission checkbox
    var foregroundPermissionRequested by remember { mutableStateOf(false) }
    // State to track if the user has interacted with the background permission checkbox
    var backgroundPermissionRequested by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxHeight()
            .padding(LargePadding)
    ) {
        val (title, card, check1, text, check2, button) = createRefs()

        Text(
            LocalContext.current.getString(R.string.location_services),
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center)
        )


        Column(
            modifier = Modifier
                .constrainAs(card) {
                    top.linkTo(title.bottom, margin = SpacerHeight)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                LocalContext.current.getString(R.string.onboarding_location_2),
                modifier = Modifier
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
        }


        val showForegroundRationale =
            (foregroundLocationPermissionState.status is PermissionStatus.Denied &&
                    (foregroundLocationPermissionState.status as PermissionStatus.Denied).shouldShowRationale)
        val showBackgroundRationale =
            (backgroundLocationPermissionState.status is PermissionStatus.Denied &&
                    (backgroundLocationPermissionState.status as PermissionStatus.Denied).shouldShowRationale)

        // Example CheckboxWithRationale, replace with your implementation

        // Foreground permission checkbox
        CheckboxWithRationale(
            Modifier
                .constrainAs(check1) {
                    top.linkTo(card.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            label = "Allow Foreground Location Access",
            permissionState = foregroundLocationPermissionState,
            showRationale = showForegroundRationale,
            onPermissionRequested = { value ->
                foregroundPermissionRequested = value
            }
        )

        if (foregroundPermissionRequested) {
            Text(
                LocalContext.current.getString(R.string.onboarding_location_4),
                modifier = Modifier
                    .constrainAs(text) {
                        top.linkTo(check1.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )

            CheckboxWithRationale(Modifier.constrainAs(check2) {
                top.linkTo(text.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
                label = "Allow Background Location Access",
                permissionState = backgroundLocationPermissionState,
                showRationale = showBackgroundRationale,
                onPermissionRequested = { value -> backgroundPermissionRequested = value }
            )
        }

        CustomButton(
            modifier = Modifier.constrainAs(button) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            LocalContext.current.getString(R.string.next),
            contentColour = if (backgroundPermissionRequested && foregroundPermissionRequested) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            containerColour = if (backgroundPermissionRequested && foregroundPermissionRequested) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            consecutiveButtons = false,
        ) {
            if (backgroundPermissionRequested && foregroundPermissionRequested) {
                navController.navigate("Activity Permissions") {
                    setPrivacyPolicyShown(context, preferencesManager)
                    navController.popBackStack()
                }
            }
        }
    }
}


@Composable
fun TestComp(
    navController: NavHostController
) {

    ConstraintLayout(
        modifier = Modifier
            .background(Color.White)
            .fillMaxHeight()
            .padding(LargePadding)
    ) {
        val (title, card, check1, text, check2, button) = createRefs()

        Text(
            LocalContext.current.getString(R.string.location_services),
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(title) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
        )


            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(MediumPadding)
                    .constrainAs(card) {
                        top.linkTo(title.bottom, margin = SpacerHeight)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Text(
                    LocalContext.current.getString(R.string.onboarding_location_2),
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

        Column(Modifier.constrainAs(check1) {
            top.linkTo(card.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = true,
                    onCheckedChange = { shouldGrant ->

                    }
                )
                Text(text = "AAA")
            }
        }
        Text(
            LocalContext.current.getString(R.string.onboarding_location_4),
            modifier = Modifier
                .constrainAs(text) {
                    top.linkTo(check1.bottom, margin = SpacerHeight)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .fillMaxWidth()
,            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(Modifier.constrainAs(check2) {
            top.linkTo(text.bottom)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
        }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = true,
                    onCheckedChange = { shouldGrant ->

                    }
                )
                Text(text = "AAA")
            }
        }


        CustomButton(
            modifier = Modifier.constrainAs(button) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            },
            LocalContext.current.getString(R.string.next),
            contentColour = MaterialTheme.colorScheme.onPrimary,
            containerColour = MaterialTheme.colorScheme.primary,
            consecutiveButtons = false,
        ) {
        }
    }
}


    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun checkBackgroundLocationPermissionGranted(): Boolean {
        val backgroundLocationPermissionState = rememberPermissionState(
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        // Check and react to the permission state
        val isBackgroundLocationGranted =
            backgroundLocationPermissionState.status is PermissionStatus.Granted

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
        modifier: Modifier,
        label: String,
        permissionState: PermissionState,
        showRationale: Boolean,
        onPermissionRequested: (Boolean) -> Unit
    ) {
        var userCheckedState by remember { mutableStateOf(permissionState.status is PermissionStatus.Granted) }

        Column (modifier= modifier) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = userCheckedState,
                    onCheckedChange = { shouldGrant ->
                        if (shouldGrant && !userCheckedState) {
                            userCheckedState = true
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CheckboxWithRationaleX(
    label: String,
    permissionState: PermissionState
) {
    var userCheckedState by remember { mutableStateOf(permissionState.status is PermissionStatus.Granted) }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = userCheckedState,
                onCheckedChange = { shouldGrant ->
                }
            )
            Text(text = label)
        }

    }
}

@Preview
@Composable
private fun PreviewThisJJJ() {
    val navController = rememberNavController()
    TestComp(navController)
}