package it.torino.mobin.utils

import android.content.Context
import com.google.android.gms.location.DetectedActivity
import it.torino.mobin.R

fun getIcon(activityType: Int): Int {
    return when (activityType) {
        DetectedActivity.STILL -> R.drawable.ic_walking
        DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> R.drawable.ic_walking

        DetectedActivity.RUNNING -> R.drawable.ic_walking
        DetectedActivity.ON_BICYCLE -> R.drawable.ic_cycling
        DetectedActivity.IN_VEHICLE -> R.drawable.ic_driving
        else -> R.drawable.ic_walking
    }
}

fun getName(context: Context, activityType: Int): String {
    return when (activityType) {
        DetectedActivity.STILL -> context.getString(R.string.still)
        DetectedActivity.WALKING, DetectedActivity.ON_FOOT -> context.getString(R.string.walking)
        DetectedActivity.RUNNING -> context.getString(R.string.running)
        DetectedActivity.ON_BICYCLE -> context.getString(R.string.cycling)
        DetectedActivity.IN_VEHICLE -> context.getString(R.string.vehicle)
        else -> context.getString(R.string.unknown)
    }
}
