/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.database.DAOs

import androidx.room.*
import it.torino.tracker.tracker.sensors.location_recognition.LocationData

@Dao
interface LocationDataDAO {
    ///////////////////// insertion queries /////////////////////
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(locationData: LocationData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg locationData: LocationData)

    ///////////////////// search queries ///////////////////// 
    @Query("SELECT * FROM LocationData where timeInMsecs> :timeInMsecs ORDER BY timeInMsecs ASC LIMIT 1")
    fun getLocationAfter(timeInMsecs: Long): LocationData?

    @Query("SELECT * FROM LocationData where timeInMsecs<= :timeInMsecs ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLocationAt(timeInMsecs: Long): LocationData?

    @Query("SELECT * FROM LocationData where timeInMsecs>= :starttimeInMsecs and timeInMsecs<= :endtimeInMsecs ORDER BY timeInMsecs ASC")
    fun getLocationsBetween(starttimeInMsecs: Long, endtimeInMsecs: Long): MutableList<LocationData>?

    @Query("SELECT * FROM LocationData where timeInMsecs>= :midnight and timeInMsecs<= :nextMidnight ORDER BY timeInMsecs DESC LIMIT 1")
    fun getLastLocation(midnight: Long, nextMidnight: Long): LocationData?

    ///////////////////// data sending queries ///////////////////// 
    @Query("SELECT * FROM LocationData where uploaded <1 ORDER BY timeInMsecs ASC LIMIT :limit")
    fun getUnsentLocations(limit: Int): List<LocationData?>?

    @Query("Update LocationData set uploaded = 1 where id in (:ids)")
    fun updateSentFlag(ids: List<Int?>?)

    @get:Query("SELECT * FROM LocationData where uploaded >= 1 ORDER BY timeInMsecs DESC LIMIT 1")
    val lastSentLocation: LocationData?

    ///////////////////// deletion queries /////////////////////
    @Delete
    fun delete(locationData: LocationData)

    @Delete
    fun deleteAll(vararg locationData: LocationData)

    ///////////////////// others ///////////////////// 
    @Query("SELECT COUNT(*) FROM LocationData")
    fun howManyLocationsIDB(): Int
}