/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.database.DAOs

import androidx.lifecycle.LiveData
import androidx.room.*
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData

@Dao
interface ActivityDataDAO {
    ///////////////////// insertion /////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(activityData: ActivityData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg activityData: ActivityData)

    ///////////////////// searching /////////////////////
    @Query("SELECT * FROM ActivityData where timeInMsecs>= :time ORDER BY timeInMsecs LIMIT 1")
    fun getActivityAfter(time: Long): LiveData<ActivityData?>?

    @Query(
        "SELECT * FROM ActivityData where timeInMsecs>= :startTime and timeInMsecs<= :endTime  " +
                " ORDER BY timeInMsecs ASC,  transitionType DESC"
    )
    fun getActivitiesBetween(startTime: Long, endTime: Long): MutableList<ActivityData>

    ///////////////////// data sending queries /////////////////////
    @Query("Update ActivityData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @Query("SELECT * FROM ActivityData WHERE uploaded < 1 ORDER BY timeInMsecs ASC limit :limit")
    fun getUnsentActivities(limit: Int): List<ActivityData?>?

    @get:Query("SELECT * FROM ActivityData WHERE uploaded >= 1 ORDER BY timeInMsecs DESC limit 1")
    val lastSentActivity: ActivityData?

    ///////////////////// others /////////////////////
    @Query("SELECT COUNT(*) FROM ActivityData")
    fun howManyActivitiesInDB(): Int

    ///////////////////// deletion queries /////////////////////
    @Delete
    fun deleteAll(vararg activityData: ActivityData)

    @Delete
    fun delete(activityData: ActivityData)
}