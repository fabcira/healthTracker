package it.torino.mobin

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import it.torino.mobin.onboarding.permissions.BatteryOptimisation
import it.torino.mobin.onboarding.permissions.TermsAndConditions
import it.torino.mobin.onboarding.permissions.MainPermissionsComposable
import it.torino.mobin.onboarding.permissions.PrivacyPolicy
import it.torino.mobin.onboarding.permissions.arePermissionsToBeRequested
import it.torino.mobin.onboarding.permissions.privacyPolicyShown
import it.torino.mobin.onboarding.permissions.termsAndConditionsAccepted
import it.torino.mobin.ui.theme.MobinTheme
import it.torino.mobin.running.main_activity.MainContainer
import it.torino.mobin.utils.InterfaceViewModel
import it.torino.mobin.utils.InterfaceViewModelFactory
import it.torino.mobin.utils.LocalPreferencesManager
import it.torino.mobin.utils.PreferencesManager
import it.torino.tracker.view_model.MyViewModel
import it.torino.tracker.view_model.MyViewModelFactory

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = this
        setContent {
            MobinTheme {
                val context = LocalContext.current
                CompositionLocalProvider(LocalPreferencesManager provides PreferencesManager(context)) {
                    val interfaceViewModelFactory = InterfaceViewModelFactory(LocalContext.current)
                    val interfaceViewModel: InterfaceViewModel by viewModels { interfaceViewModelFactory }
                    val myViewModelFactory = MyViewModelFactory(LocalContext.current)
                    val viewModel: MyViewModel by viewModels { myViewModelFactory }

//                    viewModel.setCurrentDateTime(System.currentTimeMillis()-MSECS_IN_A_DAY)
                    val navController = rememberNavController()
                    var startDestination = "running"
                    var secondDestination = "Home"
                    if (!privacyPolicyShown(LocalPreferencesManager.current)) {
                        startDestination = "onboarding"
                        secondDestination= "Privacy_Policy"
                    } else if (!termsAndConditionsAccepted()) {
                        startDestination = "onboarding"
                        secondDestination= "T&Cs"
                    } else if (arePermissionsToBeRequested()) {
                        startDestination = "onboarding"
                        secondDestination = "Permissions"
                    }

                    NavHost(navController = navController, startDestination = startDestination) {
                        navigation(
                            startDestination = secondDestination,
                            route = "onboarding"
                        ) {
                            composable("Privacy_Policy"){
                                PrivacyPolicy(navController)
                            }
                            composable("T&Cs") {
                                TermsAndConditions(navController)
                            }
                            composable("Permissions") {
                                MainPermissionsComposable(activity, navController, viewModel)
                            }
                            composable("Battery_optimisation"){
                                BatteryOptimisation(activity, navController)
                            }
                        }
                        navigation(startDestination = secondDestination, route = "running") {
                            composable("Home") {
                                MainContainer(activity, viewModel, interfaceViewModel)
                            }
                        }
                    }

                }
            }
        }
    }
}

//@Composable
//fun SimulateRunning(goalSteps: Int) {
//    var progress by remember { mutableStateOf(0f) }
//    var progressString by remember { mutableStateOf("0 / $goalSteps steps") }
//    MobinTheme {
//        // Display UI based on current progress
//        CenteredContentCard(progress, progressString)
//
//        LaunchedEffect(Unit) {
//            for (i in 0..10000 step 1000) {
//                progress = (i.toFloat()/goalSteps)// Update progress state
//                progressString = "$i / $goalSteps steps"
//                delay(1000L) // Wait for a second
//                println(progress)
//            }
//        }
//    }
//}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    var progress by remember { mutableStateOf(0.5f) }

    MobinTheme {
        // Display UI based on current progress
//        CenteredContentCard(progress, "${10000-5000} Steps Remaining")
//        ScaffoldWithFABInBottomBarM3(progress, navController)
    }
}