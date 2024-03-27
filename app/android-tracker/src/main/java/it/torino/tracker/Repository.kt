/*
 * Copyright  Fabio Ciravegna
 */
package it.torino.tracker

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.torino.tracker.database.DAOs.AccelerometerDataDAO
import it.torino.tracker.database.DAOs.ActivityDataDAO
import it.torino.tracker.database.DAOs.HeartRateDAO
import it.torino.tracker.database.DAOs.LocationDataDAO
import it.torino.tracker.database.DAOs.StepsDataDAO
import it.torino.tracker.database.DAOs.SymptomsDataDAO
import it.torino.tracker.database.MyRoomDatabase
import it.torino.tracker.retrieval.MobilityResultComputation
import it.torino.tracker.retrieval.data.TripData
import it.torino.tracker.tracker.TrackerService

class Repository(application: Context?) : ViewModel() {
    val dBStepsDao: StepsDataDAO?
    val dBActivityDao: ActivityDataDAO?
    val dBLocationDao: LocationDataDAO?
    val dBHeartRateDao: HeartRateDAO?
    val dBAccelerometerDao: AccelerometerDataDAO?
    val dBSymptomsDao: SymptomsDataDAO?
    val currentHeartRate: MutableLiveData<Int>
    val mobilityChart: MutableLiveData<MobilityResultComputation>?
    val tripsList: MutableLiveData<List<TripData>>?

    companion object {
        private var repositoryInstance: Repository? = null

        @Synchronized
        fun createInstance(application: Context?): Repository? {
            if (repositoryInstance == null) {
                repositoryInstance = Repository(application)
            }
            return repositoryInstance
        }

        val instance: Repository?
            get() {
                if (TrackerService.currentTracker != null) {
                    if (repositoryInstance == null) createInstance(TrackerService.currentTracker)
                }
                return repositoryInstance
            }

        fun getInstance(context: Context?): Repository? {
            if (repositoryInstance == null) createInstance(context)
            return repositoryInstance
        }
    }

    init {
        val db = MyRoomDatabase.getDatabase(application!!)
        repositoryInstance = this
        dBStepsDao = db!!.myStepDataDao()
        dBActivityDao = db.myActivityDataDao()
        dBLocationDao = db.myLocationDataDao()
        dBHeartRateDao = db.myHeartRateDataDao()
        dBAccelerometerDao= db.myAccelerometerDao()
        dBSymptomsDao = db.mySymptomsDataDao()

        currentHeartRate = MutableLiveData<Int>()

        mobilityChart = MutableLiveData()
        tripsList = MutableLiveData()
    }

}