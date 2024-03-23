/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.tracker.sensors.heart_rate_monitor

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import it.torino.tracker.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InsertHeartRateDataAsync(context: Context, hrDataList: List<HeartRateData>) {
    private val repositoryInstance: Repository? = Repository.getInstance(context)
    val TAG: String? = this::class.simpleName
    companion object {
        private const val MINIMUM_INTERVAL = 10000

    }

    init {
        if (hrDataList.isNotEmpty()) {
            val finalList = removeIrrelevantReadings(hrDataList)
            if (finalList.isNotEmpty()) {
                repositoryInstance?.viewModelScope?.launch(Dispatchers.IO) {
                    insertHRs(finalList)
                }
            }
        }
    }

    private suspend fun insertHRs(hrDataList: List<HeartRateData>) {
        withContext(Dispatchers.IO) {
            repositoryInstance?.dBHeartRateDao?.insertAll(*hrDataList.toTypedArray())
            Result.success(true)
        }
    }

    /**
     * we pick up a reading a second but we do not need those that are too close
     *
     * @param hrEventList
     * @return
     */
    private fun removeIrrelevantReadings(hrEventList: List<HeartRateData>): List<HeartRateData> {
        val filteredList: MutableList<HeartRateData> = ArrayList()
        if (hrEventList.isEmpty()) return hrEventList
        var prevSensorEvent: HeartRateData = hrEventList[0]
        var base = 0
        var found = false
        for (index in 1 until hrEventList.size) {
            val sensorEvent = hrEventList[index]
            //Log.i(TAG, "current hr reading: "+ sensorEvent.toString());
            // as the list is reversed we have to do previous.time- this.time
            if (sufficientlyDistant(sensorEvent, prevSensorEvent)) {
                val selectedSensorEvent = selectBestReading(base, index, hrEventList)
                Log.i(
                    TAG,
                    "HR: selecting reading $sensorEvent out of ${index - base} readings)"
                )
                if (selectedSensorEvent != null) {
                    filteredList.add(selectedSensorEvent)
                    prevSensorEvent = selectedSensorEvent
                    base = index
                    found = true
                } else {
                    prevSensorEvent = hrEventList[index - 1]
                    base = index - 1
                }
            } else {
                Log.i(
                    TAG,
                    "HR: skipping reading $sensorEvent )"
                )
            }
        }
        // if we did not store any or we have a few left to consider we have not selected among
        if (!found || hrEventList.size - base > 4) {
            val sensorEvent = selectBestReading(base, hrEventList.size, hrEventList)
            Log.i(
                TAG,
                "HR: selecting reading $sensorEvent out of ${hrEventList.size} readings)"
            )
            sensorEvent.let{
                filteredList.add(sensorEvent!!)
            }
        }
        return filteredList
    }

    /**
     * it picks the median value among the selected ones
     * @param base the index of the first element in hrEventList to consider
     * @param top the index of the first  element in hrEventList NOT to consider
     * @param hrEventList the list of hrReadings
     * @return the median element of the lot - note that if the number of elements is even, then
     * the lower element of the couple who are candidates for the median has their HR value modified
     * to fit the median
     */
    private fun selectBestReading(
        base: Int,
        top: Int,
        hrEventList: List<HeartRateData>
    ): HeartRateData? {
        val partialList: MutableList<HeartRateData> = mutableListOf()
        for (index in base until top) {
            val event = hrEventList[index]
            if (event.heartRate > 0)
                partialList.add(event)
        }
        if (partialList.size == 0)
            return null
        partialList.sortBy { it.heartRate }
        val mid = partialList.size / 2
        val value = if (partialList.size % 2 != 0) {
            partialList[mid].heartRate
        } else {
            (partialList[mid - 1].heartRate + partialList[mid].heartRate) / 2
        }
        partialList[mid].heartRate = value
        return partialList[mid]
    }

    private fun sufficientlyDistant(
        sensorEvent: HeartRateData?,
        prevSensorEvent: HeartRateData
    ): Boolean {
        return kotlin.math.abs(sensorEvent!!.timeInMsecs - prevSensorEvent.timeInMsecs) > MINIMUM_INTERVAL
    }

}