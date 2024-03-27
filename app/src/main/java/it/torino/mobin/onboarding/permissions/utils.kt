package it.torino.mobin.onboarding.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import checkBackgroundLocationPermissionGranted
import checkForegroundLocationPermissionGranted
import it.torino.mobin.utils.PreferencesManager

fun hasForegroundLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
fun hasBackgroundLocationPermission(context: Context): Boolean {
    // For Android 10 and above, check for background location permission.
    // For versions below Android 10, foreground permission suffices for background location access.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
    return hasForegroundLocationPermission(context)
}

fun arePermissionsToBeRequested(context: Context, preferencesManager: PreferencesManager): Boolean{
    return (!hasForegroundLocationPermission(context) || !hasBackgroundLocationPermission(context))

}
