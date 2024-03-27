package it.torino.tracker.database.DAOs
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.torino.tracker.tracker.sensors.accelerometer.AccelerometerData

@Dao
interface AccelerometerDataDAO {
    @Insert
    suspend fun insert(accelerometerData: AccelerometerData)

    @Insert
    suspend fun insertAll(accelerometerData: List<AccelerometerData>)

    @Query("SELECT * FROM AccelerometerData ORDER BY timestamp DESC")
    suspend fun getAllData(): List<AccelerometerData>
}