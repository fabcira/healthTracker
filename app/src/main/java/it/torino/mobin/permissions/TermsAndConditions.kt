package it.torino.mobin.permissions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import it.torino.mobin.R
import it.torino.mobin.utils.LocalPreferencesManager
import it.torino.mobin.ui.theme.MediumPadding

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun TermsAndConditions(navController: NavHostController) {
    val preferencesManager = LocalPreferencesManager.current
    val myPreferenceKey = LocalContext.current.getString(R.string.my_preference_key)
    val isPreferenceEnabled = preferencesManager.getBoolean(myPreferenceKey)

    if (isPreferenceEnabled) {
        var target = "Home"
        if (arePermissionsToBeRequested())
            target= "Permissions"
        navController.navigate(target) {
            navController.popBackStack()
        }
    }

    Column {
        Text(text = stringResource(id = R.string.onboarding_description_first_install),
            modifier = Modifier.padding(MediumPadding))
        Text(text = stringResource(id = R.string.onboarding_description_first_install),
            modifier = Modifier.padding(MediumPadding))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isPreferenceEnabled,
                onCheckedChange = { checkStatus ->
                    preferencesManager.setBoolean(myPreferenceKey, checkStatus)
                    navController.popBackStack()
                }
            )
            Text(text = "Accept T&Cs?")
        }
    }
}

@Composable
fun termsAndConditionsAccepted(): Boolean {
    val preferencesManager = LocalPreferencesManager.current
    val myPreferenceKey = LocalContext.current.getString(R.string.my_preference_key)
    // Check and react to the permission state
    return preferencesManager.getBoolean(myPreferenceKey)
}