package it.torino.mobin.running.ui_elements
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import it.torino.mobin.R
import it.torino.mobin.utils.SettingsViewModel
import it.torino.tracker.view_model.MyViewModel

@Composable
fun SettingsScreen(navController: NavHostController, trackerViewModel: MyViewModel, viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))

        SettingItem(
            title = "Use Location Services",
            isChecked = viewModel.useLocationServices.value,
            onCheckedChange = { viewModel.setUseLocationServices(it) }
        )

        SettingItem(
            title = "Use Activity Recognition",
            isChecked = viewModel.useActivityRecognition.value,
            onCheckedChange = { viewModel.setUseActivityRecognition(it) }
        )

        SettingItem(
            title = "Use Step Counting",
            isChecked = viewModel.useStepCounting.value,
            onCheckedChange = { viewModel.setUseStepCounting(it) }
        )

        SettingItem(
            title = "Use Heart Rate Monitor",
            isChecked = viewModel.useHRMonitor.value,
            onCheckedChange = { viewModel.setUseHRMonitor(it) }
        )

        SettingItem(
            title = "Use Accelerometer",
            isChecked = viewModel.useAccelerometer.value,
            onCheckedChange = { viewModel.setUseAccelerometer(it) }
        )

        SettingItem(
            title = "Use Gyro",
            isChecked = viewModel.useGyro.value,
            onCheckedChange = { viewModel.setUseGyro(it) }
        )

        SettingItem(
            title = "Use Magnetometer",
            isChecked = viewModel.useMagnetometer.value,
            onCheckedChange = { viewModel.setUseMagnetometer(it) }
        )

//        Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

        Button(
            onClick = {
                viewModel.savePreferences()
                trackerViewModel.stopTracker()
                trackerViewModel.setCurrentDateTime(System.currentTimeMillis())
                trackerViewModel.computeResults()
                trackerViewModel.stopTracker()
                val appContext = context.applicationContext
                trackerViewModel.startTracker(appContext)
                navController.navigate("home")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(context.getString(R.string.next))
        }
    }
}

@Composable
fun SettingItem(title: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, modifier = Modifier.weight(1f))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
