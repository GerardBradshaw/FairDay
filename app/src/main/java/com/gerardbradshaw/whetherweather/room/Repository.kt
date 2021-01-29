package com.gerardbradshaw.whetherweather.room

import android.app.Application
import androidx.lifecycle.LiveData
import com.gerardbradshaw.whetherweather.di.annotations.ThreadDefault
import com.gerardbradshaw.whetherweather.di.annotations.ThreadIo
import com.gerardbradshaw.whetherweather.di.annotations.ThreadMain
import kotlinx.coroutines.*
import javax.inject.Inject

class Repository @Inject constructor(
  application: Application,
  @ThreadMain private val threadMain: CoroutineDispatcher,
  @ThreadDefault private val threadDefault: CoroutineDispatcher,
  @ThreadIo private val threadIo: CoroutineDispatcher
) {
  private val locationDataDao: LocationDataDao

  var locations: List<LocationEntity> = ArrayList()
    private set

  init {
    val db = RoomDb.getDatabase(application)
    locationDataDao = db.getLocationDao()
    setLocations()
  }


  // ------------------------ RETRIEVE ------------------------

  private fun setLocations() {
    CoroutineScope(threadMain).launch {
      getLocationDataSetFromDb()
    }
  }

  private suspend fun getLocationDataSetFromDb() {
    withContext(threadIo) {
      val locations = locationDataDao.getLocationDataSet()

      withContext(threadMain) {
        saveLocationsLocalCopy(locations)
      }
    }
  }

  private fun saveLocationsLocalCopy(locations: List<LocationEntity>) {
    this.locations = locations
  }

  fun getLiveLocations(): LiveData<List<LocationEntity>> {
    return locationDataDao.getLiveLocations()
  }


  // ------------------------ INSERT ------------------------

  fun saveLocation(location: LocationEntity) {
    CoroutineScope(threadMain).launch {
      saveLocationToDb(location)
    }
  }

  private suspend fun saveLocationToDb(location: LocationEntity) {
    withContext(threadIo) {
      locationDataDao.insertLocation(location)
    }
  }



  // ------------------------ DELETE ------------------------

  fun deleteLocationData(location: LocationEntity) {
    CoroutineScope(threadMain).launch {
      deleteLocationFromDb(location)
    }
  }

  private suspend fun deleteLocationFromDb(location: LocationEntity) {
    withContext(threadIo) {
      locationDataDao.deleteLocation(location)
    }
  }
}