/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import androidx.lifecycle.viewModelScope
import it.torino.tracker.utils.Globals
import it.torino.tracker.utils.PreferencesStore
import it.torino.tracker.view_model.MyViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL


class UserRegistration(val context: Context, val viewModel: MyViewModel) {
    val TAG = UserRegistration::class.java.simpleName
    init {
        viewModel.viewModelScope.launch(Dispatchers.IO) {
            register(context)
        }
    }

    /**
     * it gets the user's unique id from teh database and then stores it into the preferences under
     * Globals.USER_ID
     * @param context the calling context
     */
    private suspend fun register(context: Context) {
        withContext(Dispatchers.IO) {
            val data = getUserData(context)
            val url =
                URL(Globals.SERVER_URI + Globals.SERVER_PORT + Globals.SERVER_REGISTRATION_URL)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            try {
                urlConnection.doOutput = true
                urlConnection.setChunkedStreamingMode(0)
                urlConnection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                )
                urlConnection.setRequestProperty("Accept", "application/json")
                val out = BufferedOutputStream(urlConnection.outputStream)
                val writer = BufferedWriter(OutputStreamWriter(out, "UTF-8"))
                writer.write(data.toString())
                writer.flush()
                writer.close()
                urlConnection.connect()
                try {
                    val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                    val reader = BufferedReader(InputStreamReader(`in`))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    Log.d(TAG, "result from server: $result")
                    val responseObject = JSONObject(result.toString())
                    val id = responseObject.opt(Globals.USER_ID) as String
                    Log.i(TAG, "id: $id")
                    val userPreferences = PreferencesStore()
                    userPreferences.setStringPreference(context, Globals.USER_ID, id)
                    Result.success(true)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Result.failure<Exception>(e)
                }
            } catch (e: ConnectException) {
                Log.e(TAG, e.message!!)
                Result.failure<Exception>(e)
            } catch (e: Exception) {
                Log.e(TAG, e.message!!)
                Result.failure<Exception>(e)
            } finally {
                urlConnection.disconnect()
            }
        }
    }

    private fun getUserData(context: Context): JSONObject {
        val appVersion = getAppVersion(context)
        val androidVersion = getAndroidVersion()
        val phoneModel = getPhoneModel()
        val userObject = JSONObject()
        userObject.put(Globals.USER_ID, "000000")
        userObject.put(Globals.PHONE_MODEL, phoneModel)
        userObject.put(Globals.ANDROID_VERSION, androidVersion)
        userObject.put(Globals.APP_VERSION, appVersion)
        userObject.put(Globals.AGE, 18)
        userObject.put(Globals.HEIGHT, 170)
        userObject.put(Globals.WEIGHT, 60)
        userObject.put(Globals.PARTICIPANT_ID, "0000000")
        userObject.put(Globals.TIME_IN_MILLIS, System.currentTimeMillis())
        userObject.put(Globals.CREATED_AT, System.currentTimeMillis())

        return userObject
    }

    private fun getPhoneModel(): String {
        return Build.MODEL + " " + Build.PRODUCT + "" + Build.BOARD
    }

    private fun getAndroidVersion(): String {
        return ("Android:" + Build.VERSION.RELEASE + " " + Build.VERSION.INCREMENTAL)
    }

    private fun getAppVersion(context: Context): String {
        var versionName = ""
        val pInfo: PackageInfo?
        try {
            pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            versionName = "Android " + pInfo.versionName
        } catch (e: java.lang.Exception) {
            Log.e(TAG, e.message!!)
        }
        return versionName
    }
}