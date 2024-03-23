/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna 
 * All rights Reserved
 */
package it.torino.tracker.database.DAOs

import androidx.room.*
import it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateData

@Dao
interface HeartRateDAO {
    ///////////////////// deletion queries /////////////////////
    @Delete
    fun delete(heartRateData: HeartRateData)

    @Query("SELECT * FROM HeartRateData where timeInMsecs>= :startTime AND timeInMsecs<=:endTime ORDER BY timeInMsecs DESC LIMIT 500")
    fun deleteAll(startTime: Long, endTime: Long): Int

    @Delete
    fun deleteAll(vararg heartRateData: HeartRateData)

    ///////////////////// insertion queries /////////////////////
    @Insert
    fun insertAll(vararg heartRateData: HeartRateData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(heartRateData: HeartRateData)

    ///////////////////// search queries /////////////////////
    @Query("SELECT * FROM HeartRateData where timeInMsecs>= :startTime and timeInMsecs<=:endTime ORDER BY timeInMsecs DESC LIMIT 1000")
    fun getHeartRateBetween(startTime: Long, endTime: Long): MutableList<HeartRateData>?

    @Query("SELECT * FROM HeartRateData where timeInMsecs<= :time ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLastHeartRate(time: Long): MutableList<HeartRateData?>?


    ///////////////////// data sending queries /////////////////////
    @Query("Update HeartRateData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @Query("SELECT * FROM HeartRateData where uploaded <1 ORDER BY timeInMsecs ASC LIMIT :limit")
    fun getUnsentHeartRateData(limit: Int): MutableList<HeartRateData?>?
}