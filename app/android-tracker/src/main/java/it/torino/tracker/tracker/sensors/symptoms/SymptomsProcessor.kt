/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.tracker.sensors.symptoms

import android.content.Context
import android.util.Log
import it.torino.tracker.tracker.sensors.step_counting.InsertStepsDataAsync
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SymptomsProcessor {
    val _tag = this::class.java.simpleName
    var symptomsDataList: MutableList<SymptomsData> = mutableListOf()

    fun insertSymptom(symptomData: SymptomsData){
        Log.i(_tag, "Inserting symptom: $symptomData")
        symptomsDataList.add(symptomData)
    }
    fun storeSymptoms(context: Context?){
        CoroutineScope(Dispatchers.IO).launch {
            Log.i(_tag, "saving symptoms to db!")
            saveToDBCoroutine(context!!)
        }
    }

    private suspend fun saveToDBCoroutine(context: Context) {
        delay(1000L)
        InsertSymptomsDataAsync(context, symptomsDataList)
        symptomsDataList = mutableListOf()
        Log.i(_tag, "saved to db!")
    }


}