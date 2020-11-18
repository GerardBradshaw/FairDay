package com.gerardbradshaw.whetherweather.room

import android.app.Application
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(application: Application) {

  private val locationDataDao: LocationDataDao

  var locationDataSet: LiveData<List<LocationData>>
    private set

  init {
    val db = RoomDb.getDatabase(application)
    locationDataDao = db.getLocationDao()
    locationDataSet = locationDataDao.getLocationDataSet()
  }


  // ------------------------ INSERT ------------------------

  fun insertLocationData(locationData: LocationData) {
    CoroutineScope(Dispatchers.Main).launch {
      saveLocationToDb(locationData)
    }
  }

  private suspend fun saveLocationToDb(locationData: LocationData) {
    withContext(Dispatchers.IO) {
      locationDataDao.insertLocationData(locationData)
    }
  }


  // ------------------------ DELETE ------------------------

  fun deleteLocationData(locationData: LocationData) {
    CoroutineScope(Dispatchers.Main).launch {
      deleteLocationFromDb(locationData)
    }
  }

  private suspend fun deleteLocationFromDb(locationData: LocationData) {
    withContext(Dispatchers.IO) {
      locationDataDao.deleteLocation(locationData)
    }
  }
}