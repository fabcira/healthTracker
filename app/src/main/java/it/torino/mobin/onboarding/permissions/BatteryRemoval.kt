package it.torino.mobin.onboarding.permissions

import android.content.Context
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import it.torino.mobin.R
import it.torino.mobin.getNextNavigationRouteDuringOnboarding
import it.torino.mobin.onboarding.openAppSettings
import it.torino.mobin.ui.theme.MediumPadding
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.ui.theme.SpacerHeight
import it.torino.mobin.utils.CustomButton
import it.torino.mobin.utils.LocalPreferencesManager
import it.torino.mobin.utils.PreferencesManager


@Composable
fun BatteryPermissionsRemoval(navController: NavHostController, preferencesManager: PreferencesManager) {
    val context = LocalContext.current
    val myPreferenceKey = LocalContext.current.getString(R.string.permission_removal_unticked_key)
    var settingsOpened by remember {mutableStateOf(false)}

    ConstraintLayout(modifier = Modifier.fillMaxHeight().padding(MediumPadding)) {
        val (text1, text2, button1, button2) = createRefs()
        Text(context.getString(R.string.battery_permissions_removal),
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacerHeight)
                .constrainAs(text1) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
        )
        Text(context.getString(R.string.battery_permissions_removal_description),
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(text2) {
                    top.linkTo(text1.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
        )
        CustomButton(modifier=Modifier.padding(top= SpacerHeight)
            .constrainAs(button1) {
                bottom.linkTo(button2.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            context.getString(R.string.open_battery_permissions) ,
            contentColour = if (!settingsOpened) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            containerColour = if (!settingsOpened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            consecutiveButtons = true
        ) {
            preferencesManager.setBoolean(myPreferenceKey, true)
            openAppSettings(context)
            settingsOpened = true
        }
        CustomButton(modifier=Modifier
            .constrainAs(button2) {
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, context.getString(R.string.next),
            contentColour = if (settingsOpened) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            containerColour = if (settingsOpened) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
            consecutiveButtons = true

        ) {
            val nextDestination = getNextNavigationRouteDuringOnboarding(context, preferencesManager)
            navController.navigate(nextDestination) {
                navController.popBackStack()
            }
        }

    }
}

fun batteryPermissionsRemovalRemoved(context: Context, preferencesManager: PreferencesManager): Boolean {
    val myPreferenceKey = context.getString(R.string.permission_removal_unticked_key)
// Check and react to the permission state
    return preferencesManager.getBoolean(myPreferenceKey)
}

@Preview
@Composable
private fun Preview() {
    MobinTheme {
        val navController: NavHostController = rememberNavController()
        val preferencesManager = LocalPreferencesManager.current

        BatteryOptimisation(navController, preferencesManager)
    }
}