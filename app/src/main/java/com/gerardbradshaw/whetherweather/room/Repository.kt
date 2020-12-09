package com.gerardbradshaw.whetherweather.room

import android.app.Application
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


  // ------------------------ INSERT ------------------------

  fun insertLocationData(locationData: LocationEntity) {
    CoroutineScope(Dispatchers.Main).launch {
      saveLocationToDb(locationData)
    }
  }

  private suspend fun saveLocationToDb(locationData: LocationEntity) {
    withContext(Dispatchers.IO) {
      locationDataDao.insertLocation(locationData)
    }
  }



  // ------------------------ DELETE ------------------------

  fun deleteLocationData(locationData: LocationEntity) {
    CoroutineScope(Dispatchers.Main).launch {
      deleteLocationFromDb(locationData)
    }
  }

  private suspend fun deleteLocationFromDb(locationData: LocationEntity) {
    withContext(Dispatchers.IO) {
      locationDataDao.deleteLocation(locationData)
    }
  }
}