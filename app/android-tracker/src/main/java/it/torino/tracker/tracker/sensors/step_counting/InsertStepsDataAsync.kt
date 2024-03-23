/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.tracker.sensors.step_counting


import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import it.torino.tracker.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsertStepsDataAsync(context: Context?, stepsDataList: List<StepsData>) {
    val TAG: String? = this::class.simpleName
    private val repositoryInstance: Repository? = Repository.getInstance(context)

    init {
        Log.i(TAG, "flushing steps to DB? ${stepsDataList.isNotEmpty()}")
        if (stepsDataList.isNotEmpty())
            repositoryInstance?.viewModelScope?.launch(Dispatchers.IO) {
                insertSteps(stepsDataList)
            }
    }

    private suspend fun insertSteps(stepsDataList: List<StepsData>) {
        withContext(Dispatchers.IO) {
            repositoryInstance?.dBStepsDao?.insertAll(*stepsDataList.toTypedArray())
        }
    }
}
