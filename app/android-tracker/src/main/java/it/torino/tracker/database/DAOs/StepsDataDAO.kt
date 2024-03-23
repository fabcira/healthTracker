/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.database.DAOs

import androidx.lifecycle.LiveData
import androidx.room.*
import it.torino.tracker.tracker.sensors.step_counting.StepsData

@Dao
interface StepsDataDAO {
    ///////////////////// insertion queries /////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stepsData: StepsData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg stepsData: StepsData)

    ///////////////////// selection queries /////////////////////
    @Query("SELECT * FROM StepsData WHERE timeInMsecs>= 0 limit 1")
    fun getFirstSteps(): StepsData?

    @Query("SELECT * FROM StepsData WHERE timeInMsecs>= :startTime and timeInMsecs<= :endTime ORDER BY timeInMsecs ASC")
    fun getStepsBetween(startTime: Long, endTime: Long): MutableList<StepsData>?

    @Query("SELECT * FROM StepsData WHERE timeInMsecs>= :time ORDER BY timeInMsecs ASC LIMIT 1")
    fun getStepsAfter(time: Long): LiveData<StepsData?>?

    ///////////////////// data sending queries /////////////////////

    @Query("SELECT COUNT(*) FROM StepsData")
    fun howManyElementsInDB(): Int

    @Query("SELECT * FROM StepsData WHERE uploaded <1 ORDER BY timeInMsecs ASC limit :limit")
    fun getUnsentSteps(limit: Int): List<StepsData?>?

    @Query("Update StepsData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @get:Query("SELECT * FROM StepsData WHERE uploaded >= 1 ORDER BY timeInMsecs DESC limit 1")
    val lastSentSteps: StepsData?

    ///////////////////// Deletion queries /////////////////////
    @Delete
    fun delete(stepsData: StepsData)

    @Delete
    fun deleteAll(vararg stepsData: StepsData)
}