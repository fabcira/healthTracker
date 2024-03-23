/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker.sensors.location_recognition

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import it.torino.tracker.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsertLocationDataAsync(context: Context, val locationDataList: List<LocationData>) {
    val TAG: String? = this::class.simpleName
    private val repositoryInstance: Repository? = Repository.getInstance(context)

    init {
        Log.i(TAG, "flushing locations to DB? ${locationDataList.isNotEmpty()}")
        if (locationDataList.isNotEmpty())
            repositoryInstance?.viewModelScope?.launch(Dispatchers.IO) {
                insertLocations(locationDataList)
            }
    }

    private suspend fun insertLocations(locationDataList: List<LocationData>) {
        withContext(Dispatchers.IO) {
            repositoryInstance?.dBLocationDao?.insertAll(*locationDataList.toTypedArray())
            Result.success(true)
        }
    }

}