/*
 * Copyright (c) Code Developed by Prof. Fabio Ciravegna
 * All rights Reserved
 */


package it.torino.tracker.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import it.torino.tracker.database.DAOs.AccelerometerDataDAO
import it.torino.tracker.database.DAOs.ActivityDataDAO
import it.torino.tracker.database.DAOs.HeartRateDAO
import it.torino.tracker.database.DAOs.LocationDataDAO
import it.torino.tracker.database.DAOs.StepsDataDAO
import it.torino.tracker.database.DAOs.SymptomsDataDAO
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.sensors.accelerometer.AccelerometerData
import it.torino.tracker.tracker.sensors.activity_recognition.ActivityData
import it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateData
import it.torino.tracker.tracker.sensors.location_recognition.LocationData
import it.torino.tracker.tracker.sensors.step_counting.StepsData
import it.torino.tracker.tracker.sensors.symptoms.SymptomsData

@Database(
    entities = [StepsData::class, LocationData::class, ActivityData::class, TripData::class,
        HeartRateData::class, SymptomsData::class, AccelerometerData::class],
    version = 10,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 9, to = 10)
    ],

)
abstract class MyRoomDatabase : RoomDatabase() {
    abstract fun myStepDataDao(): StepsDataDAO?
    abstract fun myActivityDataDao(): ActivityDataDAO?
    abstract fun myLocationDataDao(): LocationDataDAO?
    abstract fun myHeartRateDataDao(): HeartRateDAO
    abstract fun myAccelerometerDao(): AccelerometerDataDAO

    abstract fun mySymptomsDataDao(): SymptomsDataDAO

    companion object {
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS TripData (" +
                            "id INTEGER NOT NULL," +
                            "startTime INTEGER NOT NULL," +
                            "endTime INTEGER NOT NULL," +
                            "activityType INTEGER NOT NULL," +
                            "radiusInMeters INTEGER NOT NULL," +
                            "steps INTEGER NOT NULL," +
                            "distanceInMeters INTEGER NOT NULL," +
                            "uploaded INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }
        private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS HeartRateData (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "timeInMsecs INTEGER NOT NULL, " +
                            "heartRate INTEGER NOT NULL, " +
                            "accuracy INTEGER NOT NULL, " +
                            "timeZone INTEGER NOT NULL, " +
                            "uploaded INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS SymptomsData (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "timeInMsecs INTEGER NOT NULL, " +
                            "type INTEGER NOT NULL, " +
                            "timeZone INTEGER NOT NULL, " +
                            "uploaded INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE  INDEX IF NOT EXISTS index_SymptomsData_timeInMsecs  ON SymptomsData (timeInMsecs) "
                )
            }
        }
        private val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_activities_timeInMillis_activityType` ON `activities` (`timeInMillis`, `activityType`)"
                )
            }
        }


        // marking the instance as volatile to ensure atomic access to the variable
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase? {
            if (INSTANCE == null) {
                synchronized(MyRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = androidx.room.Room.databaseBuilder(
                            context.applicationContext,
                            MyRoomDatabase::class.java, "mobility_database"
                        )
                            // how to add a migration
                            .addMigrations(MIGRATION_2_3, MIGRATION_4_5, MIGRATION_6_7, MIGRATION_8_9)
                            // Wipes and rebuilds instead of migrating if no Migration object.
                            .fallbackToDestructiveMigration(false)
                            .addCallback(roomDatabaseCallback)
                            .build()
                    }
                }
            }
            return INSTANCE
        }

        /**
         * Override the onOpen method to populate the database.
         * For this sample, we clear the database every time it is created or opened.
         * If you want to populate the database only when the database is created for the 1st time,
         * override MyRoomDatabase.Callback()#onCreate
         */
        private val roomDatabaseCallback: Callback =
            object : Callback() {
            }
    }
}
