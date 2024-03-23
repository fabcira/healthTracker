package it.torino.mobin.utils
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.staticCompositionLocalOf

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("mobin_preferences", Context.MODE_PRIVATE)

    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)
}


// Declaration of LocalPreferencesManager
val LocalPreferencesManager = staticCompositionLocalOf<PreferencesManager> {
    error("No PreferencesManager provided")
}