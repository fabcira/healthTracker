/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.tracker.sensors.symptoms


import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import it.torino.tracker.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsertSymptomsDataAsync(context: Context?, symptomsDataList: List<SymptomsData>) {
    val TAG: String? = this::class.simpleName
    private val repositoryInstance: Repository? = Repository.getInstance(context)

    init {
        Log.i(TAG, "flushing symptoms to DB? ${symptomsDataList.isNotEmpty()}")
        if (symptomsDataList.isNotEmpty())
            repositoryInstance?.viewModelScope?.launch(Dispatchers.IO) {
                insertSymptoms(symptomsDataList)
            }
    }

    private suspend fun insertSymptoms(symptomsDataList: List<SymptomsData>) {
        withContext(Dispatchers.IO) {
            repositoryInstance?.dBSymptomsDao?.insertAll(*symptomsDataList.toTypedArray())
            Log.i(TAG, "Symptoms in repo:")
            for (sd in repositoryInstance!!.dBSymptomsDao!!.getUnsentSymptomsData(100)!!) {
                Log.i(TAG, sd!!.toString())
            }
            Log.i(TAG, "-----------")
        }
    }
}
