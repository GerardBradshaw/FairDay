package com.gerardbradshaw.whetherweather.room

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gerardbradshaw.weatherinfoview.datamodels.WeatherData
import com.gerardbradshaw.whetherweather.activities.detail.viewpager.DetailPagerItem
import com.gerardbradshaw.whetherweather.application.annotations.IsTest
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.*
import javax.inject.Inject
import kotlin.collections.LinkedHashMap

class Repository @Inject constructor(
  application: Application,
  @IsTest val isTest: Boolean
  ) {
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

  fun deleteLocation(location: LocationEntity) {
    CoroutineScope(Main).launch {
      withContext(threadIo) {
        locationDao.deleteLocation(location)
      }
    }
  }

  companion object {
    private const val TAG = "GGG Repository"
  }
}