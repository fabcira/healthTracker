/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.utils

import android.content.Context
import androidx.preference.PreferenceManager.getDefaultSharedPreferences

class PreferencesStore {


    fun setStringPreference(
        context: Context?,
        keyString: String,
        valueString: String
    ) {
        if (context == null) return
        val prefs = getDefaultSharedPreferences(context)
        with(prefs?.edit()) {
            this?.putString(keyString, valueString)
            this?.apply()
        }
    }

    fun getStringPreference(
        context: Context?,
        keyString: String,
        defaultValue: String
    ): String? {
        if (context == null) return ""
        val prefs = getDefaultSharedPreferences(context)
        return prefs?.getString(keyString, defaultValue)
    }

    fun setIntPreference(
        context: Context?,
        keyString: String,
        valueString: Int
    ) {
        if (context == null) return
        val prefs = getDefaultSharedPreferences(context)
        with(prefs?.edit()) {
            this?.putInt(keyString, valueString)
            this?.apply()
        }
    }

    fun getIntPreference(
        context: Context?,
        keyString: String,
        defaultValue: Int
    ): Int? {
        if (context == null) return 0
        val prefs = getDefaultSharedPreferences(context)
        return prefs?.getInt(keyString, defaultValue)
    }

    fun setBooleanPreference(
        context: Context?,
        keyString: String,
        valueBoolean: Boolean
    ) {
        if (context == null) return
        val prefs = getDefaultSharedPreferences(context)
        with(prefs?.edit()) {
            this?.putBoolean(keyString, valueBoolean)
            this?.apply()
        }
    }

    fun getBooleanPreference(
        context: Context?,
        keyString: String,
        defaultValue: Boolean
    ): Boolean? {
        if (context == null) return false
        val prefs = getDefaultSharedPreferences(context)
        return prefs?.getBoolean(keyString, defaultValue)
    }

    fun setLongPreference(context: Context?, keyString: String, valueLong: Long) {
        if (context == null) return
        val prefs = getDefaultSharedPreferences(context)
        with(prefs?.edit()) {
            this?.putLong(keyString, valueLong)
            this?.apply()
        }
    }

    fun getLongPreference(
        context: Context?,
        keyString: String,
        defaultValue: Long
    ): Long? {
        if (context == null) return defaultValue
        val prefs = getDefaultSharedPreferences(context)
        return prefs?.getLong(keyString, defaultValue)
    }

}