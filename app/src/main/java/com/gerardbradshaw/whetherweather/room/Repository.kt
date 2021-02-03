package com.gerardbradshaw.whetherweather.room

import android.app.Application
import androidx.lifecycle.LiveData
import com.gerardbradshaw.whetherweather.application.annotations.IsTest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

class Repository @Inject constructor(application: Application, @IsTest val isTest: Boolean) {
  private val locationDao: LocationDao
  private val threadIo: CoroutineDispatcher = if (isTest) Main else IO

  init {
    val db = RoomDb.getDatabase(application, isTest)
    locationDao = db.getLocationDao()
  }

  fun getLiveLocations(): LiveData<List<LocationEntity>> {
    return locationDao.getLiveLocations()
  }

  fun saveLocation(location: LocationEntity) {
    CoroutineScope(Main).launch {
      withContext(threadIo) {
        locationDao.insertLocation(location)
      }
    }
  }

  fun deleteLocationData(location: LocationEntity) {
    CoroutineScope(Main).launch {
      withContext(threadIo) {
        locationDao.deleteLocation(location)
      }
    }
  }
}