package it.torino.mobin.permissions

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import it.torino.mobin.MainActivity
import it.torino.mobin.finalisation.batteryOptimisationRequest
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.ui.theme.SmallPadding

@Composable
fun BatteryOptimisation(activity: ComponentActivity, navController: NavHostController) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            modifier = Modifier
                .height(32.dp)
                .weight(1.0f)
                .padding(SmallPadding)
                .background(MaterialTheme.colorScheme.primary),
            onClick = {
                batteryOptimisationRequest(activity)
            }) {
            Text("Open Battery Permissions", color=MaterialTheme.colorScheme.onPrimary)
        }

        Button(
            modifier = Modifier
                .height(32.dp)
                .weight(1.0f)
                .padding(SmallPadding)
                .background(MaterialTheme.colorScheme.surface),
            onClick = {
                navController.navigate("Home") {
                    navController.popBackStack()
                }
            })
             {
            Text("Done!", color=MaterialTheme.colorScheme.onSurface)
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