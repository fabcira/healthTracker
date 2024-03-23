/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */

package it.torino.tracker.database.DAOs

import androidx.room.*
import it.torino.tracker.tracker.sensors.symptoms.SymptomsData

@Dao
interface SymptomsDataDAO {
    ///////////////////// deletion queries /////////////////////
    @Delete
    fun delete(symptomsData: SymptomsData)

    @Query("SELECT * FROM symptomsData where timeInMsecs>= :startTime AND timeInMsecs<=:endTime ORDER BY timeInMsecs DESC LIMIT 500")
    fun deleteAll(startTime: Long, endTime: Long): Int

    @Delete
    fun deleteAll(vararg symptomsData: SymptomsData)

    ///////////////////// insertion queries /////////////////////
    @Insert
    fun insertAll(vararg symptomsData: SymptomsData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(symptomsData: SymptomsData)

    ///////////////////// search queries /////////////////////
    @Query("SELECT * FROM SymptomsData where timeInMsecs>= :startTime and timeInMsecs<=:endTime ORDER BY timeInMsecs DESC LIMIT 1000")
    fun getSymptomsBetween(startTime: Long, endTime: Long): MutableList<SymptomsData>?

    @Query("SELECT * FROM SymptomsData where timeInMsecs<= :time ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLastSymptom(time: Long): MutableList<SymptomsData?>?


    ///////////////////// data sending queries /////////////////////
    @Query("Update SymptomsData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @Query("SELECT * FROM SymptomsData where uploaded <1 ORDER BY timeInMsecs ASC LIMIT :limit")
    fun getUnsentSymptomsData(limit: Int): MutableList<SymptomsData?>?
}