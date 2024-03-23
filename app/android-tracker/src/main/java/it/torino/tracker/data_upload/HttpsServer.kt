/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.data_upload

import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class HttpsServer {
    val TAG: String? = this::class.simpleName
    fun sendToServer(urlx: String, data: JSONObject): JSONObject? {
        var responseObject: JSONObject? = null
        val url = URL(urlx)
        val urlConnection: HttpURLConnection = url.openConnection() as HttpsURLConnection
//        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        Log.i(TAG, "Sending data to $urlx")
        try {
            urlConnection.doOutput = true
            urlConnection.setChunkedStreamingMode(0)
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
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
                Log.d("test", "result from server: $result")
                responseObject = JSONObject(result.toString())
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d(TAG, "error in sending: {${e.printStackTrace()}}")
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            Log.d(TAG, "error in sending: {${e.printStackTrace()}}")
        } finally {
            urlConnection.disconnect()
        }
        return responseObject
    }

}