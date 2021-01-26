package com.gerardbradshaw.whetherweather.room

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(application: Application) {

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
    CoroutineScope(Dispatchers.Main).launch {
      getLocationDataSetFromDb()
    }
  }

  private suspend fun getLocationDataSetFromDb() {
    withContext(Dispatchers.IO) {
      val locations = locationDataDao.getLocationDataSet()

      withContext(Dispatchers.Main) {
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
    CoroutineScope(Dispatchers.Main).launch {
      saveLocationToDb(location)
    }
  }

  private suspend fun saveLocationToDb(location: LocationEntity) {
    withContext(Dispatchers.IO) {
      locationDataDao.insertLocation(location)
    }
  }



  // ------------------------ DELETE ------------------------

  fun deleteLocationData(location: LocationEntity) {
    CoroutineScope(Dispatchers.Main).launch {
      deleteLocationFromDb(location)
    }
  }

  private suspend fun deleteLocationFromDb(location: LocationEntity) {
    withContext(Dispatchers.IO) {
      locationDataDao.deleteLocation(location)
    }
  }
}