package it.torino.mobin.onboarding.permissions

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.torino.mobin.MainActivity
import it.torino.mobin.R
import it.torino.mobin.onboarding.batteryOptimisationRequest
import it.torino.mobin.onboarding.openAppSettings
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.ui.theme.SmallPadding
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.mobin.utils.CustomButton

@Composable
fun BatteryOptimisation(activity: ComponentActivity, navController: NavHostController) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth().padding(MediumPadding)) {
        var settingsOpened by remember {mutableStateOf(false)}
        Text(context.getString(R.string.battery_permissions),
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacerHeight),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
        )
        Text(context.getString(R.string.battery_settings_description),
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
        CustomButton(modifier=Modifier.padding(top= SpacerHeight), context.getString(R.string.open_battery_permissions) ,
            contentColour = if (!settingsOpened) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            containerColour = if (!settingsOpened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            consecutiveButtons = true
        ) {
             openAppSettings(context)
            settingsOpened = true
        }
        CustomButton(modifier=Modifier, context.getString(R.string.next),
            contentColour = if (settingsOpened) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            containerColour = if (settingsOpened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            consecutiveButtons = true

        ) {
            navController.navigate("Home") {
                    navController.popBackStack()
                }
        }

    }
}

@Preview
@Composable
private fun Preview() {
    MobinTheme {
        val activity = MainActivity()
        BatteryOptimisation(activity, NavHostController(activity))
    }
}